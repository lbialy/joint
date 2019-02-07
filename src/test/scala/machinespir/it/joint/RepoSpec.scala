package machinespir.it.joint

import cats.effect.Effect
import doobie.scalatest.Checker
import doobie.util.transactor.Transactor
import internal.Naming.generateEnglishTableName
import org.scalatest.FlatSpec

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.util.control.NonFatal

abstract class RepoSpec[M[_]: Effect, E <: Product: TypeTag: ProductMeta, R <: Repo[E]: TypeTag: ClassTag]
    extends FlatSpec
    with Checker[M] {

  def M: Effect[M] = Effect[M]
  def repo: R

  def repoName: String = implicitly[ClassTag[R]].runtimeClass.getSimpleName

  implicit def transactor: Transactor[M]

  repoName should "typecheck on built-in all queries" in {
    try check(repo.findAllQuery)
    catch { case NonFatal(ex) => println("CAUGHT"); throw ex } // todo add DDL generation
  }

  def generateDDL(implicit PM: ProductMeta[E]): String = {
    PM.fields // todo finish this... somehow
    s"""CREATE TABLE ${generateEnglishTableName(PM.typeName)} (
       |
       |)""".stripMargin
  }

}
