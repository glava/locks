package org.zardina

import com.thoughtworks.binding.Binding.{ Var, Vars }
import com.thoughtworks.binding.{ Binding, dom }
import diode.ActionResult.{ EffectOnly, ModelUpdate }
import diode.{ Action, Circuit, Effect }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div

import scala.concurrent.{ ExecutionContext, Future }
import scala.xml.Elem

case class GamesLoaded(games: Seq[Game], locks: Seq[Lock], user: User) extends Action

case class LoadGames(id: Int) extends Action

case class LockItUp(gameId: String, userId: String, isHomeSelected: Boolean) extends Action

case class LockItUpResult(lock: Lock) extends Action

final case class InstancesModel(instances: Seq[GameProjection], user: Option[User])

case class GameProjection(game: Game, lock: Option[Lock])

class GamesCircuit(api: LocksApiClient, userId: String, val week: Int = 1)(implicit ex: ExecutionContext) extends Circuit[InstancesModel] {
  override protected def initialModel: InstancesModel = InstancesModel(Seq.empty, None)

  override protected def actionHandler: HandlerFunction = {
    case (model: InstancesModel, action) =>
      action match {
        case LoadGames(week) =>

          val loadInstances = Effect(
            for {
              userResult <- api.user(userId)
              games <- api.games(week, userResult.user.id).map(response => GamesLoaded(response.games, response.locks, userResult.user))
            } yield games)
          Some(EffectOnly(loadInstances))

        case LockItUp(gameId, userId, isHomeSelected) =>
          val locksResult = Effect(api.createLock(gameId, isHomeSelected, userId)
            .map(response => LockItUpResult(response.createLock)))

          Some(EffectOnly(locksResult))

        case GamesLoaded(games, locks, user) =>
          val gameProjection = games.map { g => GameProjection(g, locks.find(_.gameId == g.id)) }
          Some(ModelUpdate(model.copy(instances = gameProjection, user = Some(user))))

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

class GamesComponent(circuit: GamesCircuit) extends Layout {

  val games: Vars[GameProjection] = Vars.empty
  val user: Var[Option[User]] = Var.apply(None)

  circuit.subscribe(circuit.zoom(identity)) { model =>
    games.value.clear()
    games.value ++= model.value.instances
    user.value = model.value.user
  }
  circuit.dispatch(LoadGames(circuit.week))

  def lockItUp(gameId: String, isHomeSelected: Boolean): Unit = {
    val userId = user.value.map(_.id).getOrElse("")
    circuit.dispatch(LockItUp(gameId, userId, isHomeSelected))
  }

  @dom
  override def view =
    <div id="flow">
      {
        for (gameProjection <- games) yield {
          <tr>
            <td>
              <img class="logo" alt={ gameProjection.game.home } src={ s"https://a1.espncdn.com/combiner/i?img=/i/teamlogos/nfl/500/scoreboard/${gameProjection.game.home.toLowerCase()}.png" }></img>
              <button style="font-size: 24px;" class="btn btn-default" onclick={ (_: Event) => lockItUp(gameProjection.game.id, true) }>🔒</button>
            </td>
            -
            <td>
              <img class="logo" alt={ gameProjection.game.away } src={ s"https://a1.espncdn.com/combiner/i?img=/i/teamlogos/nfl/500/scoreboard/${gameProjection.game.away.toLowerCase()}.png" }></img>
              <button style="font-size: 24px;" class="btn btn-default" onclick={ (_: Event) => lockItUp(gameProjection.game.id, false) }>🔒</button>
            </td>
            <td>
              <div style="font-size:16px;">
                <button style="font-size: 24px;" class="btn btn-default">
                  { gameProjection.lock.map(_ => "\n\uD83C\uDFC8").getOrElse("\uD83D\uDC40") }
                </button>
              </div>
            </td>
          </tr>
        }
      }
    </div>
}

