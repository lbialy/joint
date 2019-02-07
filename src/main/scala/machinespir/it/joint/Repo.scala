package machinespir.it.joint

import doobie._
import doobie.util.composite.Composite
import fs2.Stream
import internal.Naming.generateEnglishTableName

abstract class Repo[E <: Product: Composite: ProductMeta] {

  protected implicit val lh: LogHandler = LogHandler.jdkLogHandler

  protected val fieldNames: Seq[String] = ProductMeta[E].fields
  protected val tableName: String       = generateEnglishTableName(ProductMeta[E].typeName)

  protected def allFields: String   = fieldNames.mkString(", ")
  protected def allHoles: String    = List.fill(fieldNames.size)("?").mkString(", ")
  protected def allSetHoles: String = fieldNames.map(s => s"$s = ?").mkString(", ")

  private[it] val findAllQuery = Query0[E](s"SELECT $allFields FROM $tableName", logHandler = lh)

  private[it] val insertQuery = Update[E](
    sql0        = s"INSERT INTO $tableName ($allFields) VALUES ($allHoles)",
    logHandler0 = lh
  )

  private[it] val updateQuery = Update[E](
    sql0 = s"UPDATE $tableName SET $allSetHoles"
  )

  def findAll: ConnectionIO[List[E]] = findAllQuery.to[List]

  def streamAll: Stream[ConnectionIO, E] = findAllQuery.stream

  def insert(entity: E): ConnectionIO[Int] = insertQuery.toUpdate0(entity).run

  def findBy[B: Composite](column: Column[E, B])(arg: B): ConnectionIO[Option[E]] =
    Query[B, E](s"SELECT $allFields FROM $tableName WHERE ${column.name} = ?").option(arg)

  def findById[B: Composite](id: B)(implicit idColumn: IdColumn[E, B]): ConnectionIO[Option[E]] =
    findBy[B](idColumn)(id)

  def updateBy[B: Composite](column: Column[E, B])(entity: E): ConnectionIO[Int] =
    (updateQuery.toFragment(entity) ++
      Query[B, Int](s" WHERE ${column.name} = ?").toFragment(column.get(entity)))
      .update(lh)
      .run

  def updateById[B](entity: E)(implicit idColumn: IdColumnAlt.Aux[E, B], C: Composite[B]): ConnectionIO[Unit] =
    (updateQuery.toFragment(entity) ++
      Query[B, Int](s" WHERE ${ idColumn.name} = ?").toFragment(idColumn.get(entity)))
      .update(lh)
      .run
      .map(_ => ())
}
