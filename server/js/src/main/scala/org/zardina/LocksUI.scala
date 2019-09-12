package org.zardina

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div
import org.scalajs.dom.window

import scala.concurrent.{ Await, ExecutionContext, ExecutionContextExecutor }

object LocksUI extends App with Layout {
  implicit val excutionContext: ExecutionContextExecutor = ExecutionContext.global

  val currentView: Var[Option[Layout]] = Var(None)
  val gamesComponent = new GamesComponent(new GamesCircuit(new LocksApiClient()))

  @dom
  def appView: Binding[Div] = {
    <div id="app-view">
      { gamesComponent.element.bind }
    </div>
  }

  com.thoughtworks.binding.dom.render(window.document.getElementById("locks-app"), appView)
}
