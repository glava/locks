package org.zardina

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.html.Div

trait Layout {

  @dom
  def layout(content: Div): Binding[Div] =
    <div id="layout">
      <div class="container">
        { content }
      </div>
    </div>

}
