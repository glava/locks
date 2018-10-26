import com.thoughtworks.binding.Binding.{ Var, Vars }
import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import ui._

import scala.concurrent.{ ExecutionContext, Future }

class GamesComponent(apiClient: LocksApiClient) extends HtmlComponent with Layout {

  val instances: Vars[Game] = Vars.empty

  override def init: Unit = {
    apiClient.games().onComplete { games =>
      instances.value ++= games.getOrElse(List.empty)
    }(ExecutionContext.global)
  }

  def fillTemplate(isHomeSelected: Boolean): Future[Boolean] = {
    apiClient.selection()
  }

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      {
        for (contact <- instances) yield {
          <tr>
            <td>
              { contact.homeTeam.name }
              <button class="btn btn-default" onclick={ (_: Event) => fillTemplate(true) }>Select</button>
            </td>
            -
            <td>
              { contact.awayTeam.name }
              <button class="btn btn-default" onclick={ (_: Event) => fillTemplate(false) }>Select</button>
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

