package org.zardina

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div
import org.scalajs.dom._
import org.scalajs.dom.EventTarget
import com.thoughtworks.binding.dom._
import trail.Param
import trail._

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

object LocksUI extends App with Layout {
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  val currentView: Var[Layout] = Var(EmptyLayout)

  val api = new LocksApiClient()

  def redirect(url: String): Unit = {
    val layout: Layout = url match {
      case Routes.Games(userId, _) =>
        new GamesComponent(new GamesCircuit(api, userId))
      case _ =>
        new UsersComponent(new UsersCircuit(api))
    }
    currentView.value = layout
    render(window.document.getElementById("locks-app"), appView)
  }

  @dom
  def appView: Binding[Div] = {
    <div id="app-view">
      { currentView.value.view.bind }
    </div>
  }

  window.addEventListener("click", { (clickEvent: Event) =>
    // The user may click a child within <a>, traverse parents too
    def handle: EventTarget => Unit = {
      case null =>
      case a: html.Anchor if a.href.startsWith(window.location.origin.get) =>
        if (a.onclick == null) { // We have not defined any custom behaviour
          clickEvent.preventDefault()
          window.history.pushState("", "", a.href)
          redirect(a.href)
        }
      case n: Node => handle(n.parentNode)
    }

    handle(clickEvent.target)
  })

  redirect(window.location.href)
}

object Routes {
  val Games = Root / "index" & Param[String]("user_id") & Param[String](name = "view")

}