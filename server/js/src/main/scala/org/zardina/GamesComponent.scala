package org.zardina

import com.thoughtworks.binding.Binding.{ Var, Vars }
import com.thoughtworks.binding.{ Binding, dom }
import diode.ActionResult.{ EffectOnly, ModelUpdate }
import diode.{ Action, Circuit, Effect }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.zardina.ui.HtmlComponent

import scala.concurrent.{ ExecutionContext, Future }

case class GamesLoaded(games: Seq[Game], user: User) extends Action
case class LoadGames(id: Int) extends Action
case class LockItUp(gameId: String, userId: String, isHomeSelected: Boolean) extends Action
case class LockItUpResult(lock: Lock) extends Action

final case class InstancesModel(instances: Seq[GameProjection], user: Option[User])

case class GameProjection(game: Game, lock: Option[Lock])

class GamesCircuitTrait(api: LocksApiClient)(implicit ex: ExecutionContext) extends Circuit[InstancesModel] {
  override protected def initialModel: InstancesModel = InstancesModel(Seq.empty, None)

  override protected def actionHandler: HandlerFunction = {
    case (model: InstancesModel, action) =>
      action match {
        case LoadGames(week) =>
          val loadInstances = Effect(api.games(week).map(response => GamesLoaded(response.games, response.getUser)))
          Some(EffectOnly(loadInstances))

        case LockItUp(gameId, userId, isHomeSelected) =>
          val locksResult = Effect(api.createLock(gameId, isHomeSelected, userId)
            .map(response => LockItUpResult(response.createLock)))

          Some(EffectOnly(locksResult))

        case GamesLoaded(games, user) =>
          Some(ModelUpdate(model.copy(instances = games.map(g => GameProjection(g, None)), user = Some(user))))

        case LockItUpResult(lock: Lock) =>
          val updatedModel = model.instances.map {
            case GameProjection(game, _) if (game.id == lock.gameId) => GameProjection(game, Some(lock))
            case otherGameProjections => otherGameProjections
          }
          Some(ModelUpdate(model.copy(instances = updatedModel)))
        case other: Any =>
          println(s"unhandled action: $other")
          None
      }
  }

}

class GamesComponent(circuit: GamesCircuitTrait) extends HtmlComponent with Layout {

  val games: Vars[GameProjection] = Vars.empty
  val user: Var[Option[User]] = Var.apply(None)

  override def init: Unit = {
    circuit.subscribe(circuit.zoom(identity)) { model =>
      games.value.clear()
      games.value ++= model.value.instances
      user.value = model.value.user
    }

    circuit.dispatch(LoadGames(1))
  }

  def lockItUp(gameId: String, isHomeSelected: Boolean): Unit = {
    val userId = user.value.map(_.id).getOrElse("")
    circuit.dispatch(LockItUp(gameId, userId, isHomeSelected))
  }

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      {
        for (gameProjection <- games) yield {
          <tr>
            <td>
              { gameProjection.game.home }
              <button class="btn btn-default" onclick={ (_: Event) => lockItUp(gameProjection.game.id, true) }>Lock away</button>
            </td>
            -
            <td>
              { gameProjection.game.away }
              <button class="btn btn-default" onclick={ (_: Event) => lockItUp(gameProjection.game.id, false) }>Lock home</button>
            </td>
            <td>
              { gameProjection.lock.toString }
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

