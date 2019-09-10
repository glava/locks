package org.zardina

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div
import org.scalajs.dom.window
import org.zardina.ui.HtmlComponent
import pages.DomView
import pages.Page.{ Routing, page }

import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor }

object LocksUI extends App with Layout {

  val currentView: Var[Option[HtmlComponent]] = Var(None)

  implicit val excutionContext: ExecutionContextExecutor = ExecutionContext.global

  @dom
  def appView: Binding[Div] = {
    <div id="app-view">
      {
        currentView.bind match {
          case Some(view) =>
            val viewElement = view.element.bind
            view.init
            layout(viewElement).bind
          case None => <!-- no view -->
        }
      }
    </div>
  }

  val domView = new DomView[Binding[Div]]({
    case html: HtmlComponent => currentView.value = Some(html)
    case _ =>
  })
  val routes: Routing[Binding[Div]] =
    page[Binding[Div]]("/games", _ => {
      new GamesComponent(new GamesCircuit(new LocksApiClient()))
    })
      //.page("/top", _ => { new TopComponent(new LocksApiClient()) })
      .otherwise(_ => new GamesComponent(new GamesCircuit(new LocksApiClient())))

  com.thoughtworks.binding.dom.render(window.document.getElementById("locks-app"), appView)

  routes.view(domView)
}
