import com.twitter.finagle.Http
import com.twitter.finagle.http.Response
import com.twitter.util.Await
import io.finch.syntax._
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

object ServerMain extends App with StaticResourceSupport {

  val index: Endpoint[Response] = get("index") {
    getResource("index.html", "text/html")
  }

  val selection: Endpoint[Boolean] = post("selection") {
    Ok(true)
  }

  val games: Endpoint[List[Game]] = get("games") {
    Ok(List(Game(Team("Vikings"), Team("Ravens")), Game(Team("Vikings"), Team("Ravens"))))
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
      httpGetResource("toastr.min.css", "text/css") :+:
      httpGetResource("bootstrap.min.css", "text/css") :+:
      httpGetResource("bootswatch.lumen.min.css", "text/css") :+:
      httpGetResource("fontawesome-all.css", "text/css") :+:
      httpGetResource("style.css", "text/css") :+:
      httpGetResource("style.css", "text/css")
  }

  try {
    Await.ready(Http.server.serve(":8081", (index :+: resources :+: games :+: selection).toServiceAs[Application.Json]))
  } catch {
    case e: Exception => println(e.getLocalizedMessage)
  }
}