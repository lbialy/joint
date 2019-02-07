package machinespir.it

import adversaria.FindMetadata
import doobie.syntax.SqlInterpolator
import doobie.syntax.string._
import magnolia.{CaseClass, Magnolia}

import scala.annotation.{StaticAnnotation, implicitNotFound}
import scala.language.experimental.macros
import scala.language.implicitConversions

package object joint {

  final class pk() extends StaticAnnotation

  type PrimaryKey[A, R] = FindMetadata[pk, A] { type Return = R }

  object implicits {

    implicit def strToSqlInterpolator(s: String): SqlInterpolator = new StringContext(s)

    implicit def idColumnAlt[A <: Product, B](implicit PK: PrimaryKey[A, B]): IdColumnAlt[A] { type R = B } =
      new IdColumnAlt[A] {
        type R = B
        def name: String = PK.parameter.fieldName
        def get(a: A): R = PK.get(a)
      }

    implicit def idColumn[A <: Product, B](implicit PK: PrimaryKey[A, B]): IdColumn[A, B] =
      new IdColumn[A, B] {
        def name: String = PK.parameter.fieldName
        def get(a: A): B = PK.get(a)
      }

    implicit class ColumnSelector[A <: Product](val a: A) extends AnyVal {
      def column[B](selector: A => B): Column[A, B] = macro internal.DbBlackboxMacros.columnGenImpl[A, B]
    }
  }

  def column[A <: Product, B](selector: A => B): Column[A, B] = macro internal.DbBlackboxMacros.columnGenImpl[A, B]

  @implicitNotFound(
    "IdColumn instance could not be derived. Make sure your entity case class\nhas a primary key field annotated with integr8.db.pk() annotation.\nAlso make sure you have `import integr8.db.implicits._` in scope!"
  )
  trait IdColumn[-A <: Product, +B] extends Column[A, B]

  @implicitNotFound(
    "IdColumnAccessor instance could not be derived. Make sure your entity case class\nhas a primary key field annotated with integr8.db.pk() annotation.\nAlso make sure you have `import integr8.db.implicits._` in scope!"
  )
  trait IdColumnAlt[-A <: Product] {
    type R
    def name: String
    def get(a: A): R
  }

  object IdColumnAlt {
    type Aux[-A <: Product, B] = IdColumnAlt[A] { type R = B }
  }

  trait Column[-A <: Product, +B] {
    def name: String
    def get(a: A): B
  }

  trait ProductMeta[A] {
    def fields: Seq[String]
    def typeName: String
  }

  sealed trait LowPriorityProductMetaPrimitiveDerivation {
    private val p = "p"
    implicit def primitiveDerive[A]: ProductMeta[A] = new ProductMeta[A] {
      def fields           = Seq.empty[String]
      def typeName: String = p
    }
  }

  object ProductMeta extends LowPriorityProductMetaPrimitiveDerivation {
    type Typeclass[A] = ProductMeta[A]

    def combine[A](ctx: CaseClass[ProductMeta, A]): Typeclass[A] = new ProductMeta[A] {
      def fields: Seq[String] = ctx.parameters.map(_.label)
      def typeName: String    = ctx.typeName.short
    }

    implicit def gen[A <: Product]: Typeclass[A] = macro Magnolia.gen[A]

    def apply[A](implicit I: ProductMeta[A]): ProductMeta[A] = I

  }

}
