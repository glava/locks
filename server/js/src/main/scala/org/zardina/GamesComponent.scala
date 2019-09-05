package org.zardina

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{ Binding, dom }
import diode.ActionResult.{ EffectOnly, ModelUpdate }
import diode.{ Action, Circuit, Effect }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.zardina.ui.HtmlComponent

import scala.concurrent.{ ExecutionContext, Future }

case class FoundInstances(instances: Seq[Game]) extends Action
case class LoadWeek(id: Int) extends Action
case class LockItUp(gameId: String, isHomeSelected: Boolean) extends Action
case class LockItUpResult(lock: Lock) extends Action

final case class InstancesModel(instances: Seq[Game])

class GamesCircuitTrait(api: LocksApiClient)(implicit ex: ExecutionContext) extends Circuit[InstancesModel] {
  override protected def initialModel: InstancesModel = InstancesModel(Seq.empty)

  override protected def actionHandler: HandlerFunction = {
    case (model: InstancesModel, action) =>
      action match {
        case LoadWeek(gameWeek) =>
          val loadInstances = Effect(api.games(gameWeek).map(response => FoundInstances(response.games)))
          Some(EffectOnly(loadInstances))

        case LockItUp(gameId, isHomeSelected) =>
          val locksResult = Effect(api.createLock(gameId, isHomeSelected, "gogo")
            .map(response => LockItUpResult(response.createLock)))

          Some(EffectOnly(locksResult))

        case FoundInstances(newInstances) =>
          Some(ModelUpdate(model.copy(instances = newInstances)))

        case other: Any =>
          println(s"unhandled action: $other")
          None
      }
  }

}

class GamesComponent(circuit: GamesCircuitTrait) extends HtmlComponent with Layout {

  val games: Vars[Game] = Vars.empty

  override def init: Unit = {
    circuit.subscribe(circuit.zoom(identity)) { model =>
      games.value.clear()
      games.value ++= model.value.instances
    }

    circuit.dispatch(LoadWeek(1))
  }

  def lockItUp(gameId: String, isHomeSelected: Boolean): Unit = {
    circuit.dispatch(LockItUp(gameId, isHomeSelected))
  }

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      {
        for (game <- games) yield {
          <tr>
            <td>
              { game.home }
              <button class="btn btn-default" onclick={ (_: Event) => lockItUp(game.id, true) }>Lock away</button>
            </td>
            -
            <td>
              { game.away }
              <button class="btn btn-default" onclick={ (_: Event) => lockItUp(game.id, false) }>Lock home</button>
            </td>
          </tr>
        }
      }
    </div>
}

class EmptyComponent extends HtmlComponent with Layout {

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      Hello
    </div>
}

