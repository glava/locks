package org.zardina

import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div

trait Layout {
  @dom
  def view: Binding[Div] = <div></div>
}

object EmptyLayout extends Layout {
  @dom
  override def view: Binding[Div] = <div></div>
}
