package org.zardina

import com.thoughtworks.binding.Binding.{ Var, Vars }
import com.thoughtworks.binding.{ Binding, dom }
import diode.ActionResult.{ EffectOnly, ModelUpdate }
import diode.{ Action, Circuit, Effect }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div

import scala.concurrent.{ ExecutionContext, Future }
import scala.xml.Elem

case class UsersModel(users: Seq[User])

case class LoadedUsers(users: Seq[User]) extends Action

case object LoadUsers extends Action

class UsersCircuit(api: LocksApiClient)(implicit ex: ExecutionContext) extends Circuit[UsersModel] {
  override protected def initialModel: UsersModel = UsersModel(Seq.empty)

  override protected def actionHandler: HandlerFunction = {
    case (model: UsersModel, action) =>
      action match {
        case LoadUsers =>
          Some(EffectOnly(Effect(
            api.users().map {
              users => LoadedUsers(users)
            })))
        case LoadedUsers(users) =>
          Some(ModelUpdate(model.copy(users = users)))
      }
  }

}

class UsersComponent(circuit: UsersCircuit) extends Layout {

  val users: Vars[User] = Vars.empty

  circuit.subscribe(circuit.zoom(identity)) { model =>
    users.value.clear()
    users.value ++= model.value.users
  }
  circuit.dispatch(LoadUsers)

  @dom
  override def view =
    <div id="flow">
      {
        for (user <- users) yield {
          <tr>
            <td>
              <a href={ Routes.Games.url(user.id, "games") }>
                { user.nick }
              </a>
            </td>
          </tr>
        }
      }
    </div>
}

