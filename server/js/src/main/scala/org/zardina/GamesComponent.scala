package org.zardina

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{ Binding, dom }
import diode.ActionResult.{ EffectOnly, ModelUpdate }
import diode.{ Action, Circuit, Effect }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.zardina.ui.HtmlComponent

import scala.concurrent.{ ExecutionContext, Future }

case class FoundInstances(instances: Seq[Games]) extends Action
case class LoadWeek(id: Int) extends Action

final case class InstancesModel(instances: Seq[Games])

class GamesCircuitTrait(api: LocksApiClient)(implicit ex: ExecutionContext) extends Circuit[InstancesModel] {
  override protected def initialModel: InstancesModel = InstancesModel(Seq.empty)

  override protected def actionHandler: HandlerFunction = {
    case (model: InstancesModel, action) =>
      action match {
        case LoadWeek(gameWeek) =>
          val loadInstances = Effect(api.getGames(gameWeek).map(list => FoundInstances(list.games)))
          Some(EffectOnly(loadInstances))

        case FoundInstances(newInstances) =>
          Some(ModelUpdate(model.copy(instances = newInstances)))

        case other: Any =>
          println(s"unhandled action: $other")
          None
      }
  }

}

class GamesComponent(circuit: GamesCircuitTrait) extends HtmlComponent with Layout {

  val instances: Vars[Games] = Vars.empty

  override def init: Unit = {
    circuit.subscribe(circuit.zoom(identity)) { model =>
      instances.value.clear()
      instances.value ++= model.value.instances
    }

    circuit.dispatch(LoadWeek(1))
  }

  def fillTemplate(isHomeSelected: Boolean): Future[Boolean] = {
    Future.successful(true)
  }

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      {
        for (contact <- instances) yield {
          <tr>
            <td>
              { contact.home }
              <button class="btn btn-default" onclick={ (_: Event) => fillTemplate(true) }>Bla</button>
            </td>
            -
            <td>
              { contact.away }
              <button class="btn btn-default" onclick={ (_: Event) => fillTemplate(false) }>Blac</button>
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

