import com.twitter.finagle.Http
import com.twitter.finagle.http.Response
import com.twitter.util.Await
import io.finch.syntax._
import io.finch._

object ServerMain extends App with StaticResourceSupport {

  val index: Endpoint[Response] = get("index") {
    getResource("index.html", "text/html")
  }

  def httpGetResource(resource: String, contentType: String) = get(resource) {
    getResource(resource, contentType)
  }

  val resources = {
    httpGetResource("jquery.min.js", "application/javascript") :+:
      httpGetResource("bootstrap.min.js", "application/javascript") :+:
      httpGetResource("toastr.min.js", "application/javascript") :+:
      httpGetResource("ace.js", "application/javascript") :+:
      httpGetResource("theme-textmate.js", "application/javascript") :+:
      httpGetResource("mode-json.js", "application/javascript") :+:
      httpGetResource("worker-json.js", "application/javascript") :+:
      httpGetResource("locks-ui.js", "application/javascript") :+:
      httpGetResource("locks-ui.js.map", "application/json") :+:
      get("index") {
        getResource("index.html", "text/html")
      }
  }

  try {
    Await.ready(Http.server.serve(":8081", resources.toServiceAs[Text.Html]))
  } catch {
    case e: Exception => println(e.getLocalizedMessage)
  }
}