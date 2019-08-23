package org.zardina

import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.zardina.ui.HtmlComponent

import scala.concurrent.{ ExecutionContext, Future }

class TopComponent(apiClient: LocksApiClient) extends HtmlComponent with Layout {

  val instances: Vars[Game] = Vars.empty

  override def init: Unit = {

  }

  def fillTemplate(isHomeSelected: Boolean): Future[Boolean] = {
    apiClient.selection()
  }

  @dom
  override def element: Binding[Div] =
    <div>
      <form>
        <div class="form-row">
          <div class="col">
            <input type="text" class="form-control" placeholder="First name"/>
          </div>
          <div class="col">
            <input type="text" class="form-control" placeholder="Last name"/>
          </div>
        </div>
      </form>
    </div>
}
