import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div
import org.scalajs.dom.window
import pages.DomView
import pages.Page.{ Routing, page }
import ui._

object LocksUI extends App with Layout {

  val currentView: Var[Option[HtmlComponent]] = Var(None)

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
    page[Binding[Div]]("/flows", _ => {
      println("hello")
      new GamesComponent()
    }).otherwise(_ => new EmptyComponent())

  com.thoughtworks.binding.dom.render(window.document.getElementById("locks-app"), appView)

  routes.view(domView)
}
