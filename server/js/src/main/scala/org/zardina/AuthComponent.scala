package org.zardina

import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div

object AuthComponent extends Layout {
  @dom
  def element: Binding[Div] =
    <div id="flow">empty</div>
}
