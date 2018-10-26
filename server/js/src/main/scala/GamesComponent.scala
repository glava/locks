import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div
import ui._

class GamesComponent extends HtmlComponent with Layout {

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      Well now hello
    </div>
}

class EmptyComponent extends HtmlComponent with Layout {

  @dom
  override def element: Binding[Div] =
    <div id="flow">
      Hello
    </div>
}

