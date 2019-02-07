package machinespir.it.joint

import scala.language.experimental.macros

package object internal {

  object Naming {
    import org.atteo.evo.inflector.English

    def generateEnglishTableName(entity: String): String = English.plural(entity).toLowerCase
  }

  import scala.reflect.macros.blackbox

  class DbBlackboxMacros(val c: blackbox.Context) {
    import c.universe._

    def columnGenImpl[Entity: c.WeakTypeTag, Field: c.WeakTypeTag](selector: c.Tree): c.Tree = {
      val Entity = weakTypeOf[Entity]
      val Field  = weakTypeOf[Field]
      selector match {
        case q"(${vd: ValDef}) => ${idt: Ident}.${fieldName: Name}" if vd.name == idt.name =>
          q"""
              new _root_.integr8.db.Column[$Entity, $Field] {
                def name: String = ${fieldName.toString()}
                def get(a: $Entity): $Field = $selector(a)
              }
            """
        case _ =>
          c.abort(
            c.enclosingPosition,
            "Lambda expression passed to column method has to return one of the fields of the case class."
          )
      }
    }
  }

}
