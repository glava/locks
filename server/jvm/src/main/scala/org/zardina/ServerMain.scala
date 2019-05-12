package org.zardina

import com.twitter.finagle.Http
import com.twitter.finagle.http.Response
import com.twitter.util.{ Await, Future }
import io.finch.syntax.{ get, post }
import io.finch.{ Endpoint, Ok }
import org.zardina.graphql._
import com.twitter.finagle.http.Status

import scala.concurrent.ExecutionContext

object ServerMain extends App
  with StaticResourceSupport
  with GraphQlRequestDecoders
  with GraphQlEncoders {

  DbMigration.updateDatabase(DataSource.mysqlConnection)

  case class Picture(width: Int, height: Int, url: Option[String])

  import sangria.schema._

  import sangria.macros.derive._

  implicit val PictureType = deriveObjectType[Unit, Picture](
    ObjectTypeDescription("The product picture"),
    DocumentField("url", "Picture CDN URL"))

  trait Identifiable {
    def id: String
  }

  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",

    fields[Unit, Identifiable](
      Field("id", StringType, resolve = _.value.id)))

  case class Product(id: String, name: String, description: String) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
  }

  val ProductType = deriveObjectType[Unit, Product](
    Interfaces(IdentifiableType),
    IncludeMethods("picture"))

  class ProductRepo {
    private val Products = List(
      Product("1", "Cheesecake", "Tasty"),
      Product("2", "Health Potion", "+50 HP"))

    def product(id: String): Option[Product] =
      Products find (_.id == id)

    def products: List[Product] = Products
  }

  val Id = Argument("id", StringType)

  val QueryType = ObjectType("Query", fields[ProductRepo, Unit](
    Field("product", OptionType(ProductType),
      description = Some("Returns a product with specific `id`."),
      arguments = Id :: Nil,
      resolve = c ⇒ c.ctx.product(c arg Id)),

    Field("products", ListType(ProductType),
      description = Some("Returns a list of all available products."),
      resolve = _.ctx.products)))

  val schema = Schema(QueryType)

  val index: Endpoint[Response] = get("index") {
    getResource("index.html", "text/html")
  }

  import io.circe._

  import io.finch.{ Endpoint, _ }

  val executor = GraphQlQueryExecutor.executor(schema, new ProductRepo, maxQueryDepth = 10)

  def api: Endpoint[Json] =
    post("api" :: jsonBody[GraphQlQuery]) { query: GraphQlQuery =>
      executeQuery(query)
    }

  private def executeQuery(query: GraphQlQuery): Future[Output[Json]] = {
    val operationName = query.operationName.getOrElse("unnamed_operation")
    runQuery(query)
  }

  private def runQuery(query: GraphQlQuery): Future[Output[Json]] = {
    val result = executor.execute(query)(ExecutionContext.global)
    import io.circe._, io.circe.generic.semiauto._
    // Do our best to map the type of error back to a HTTP status code
    result.map {
      case SuccessfulGraphQlResult(json, _) => Output.payload(json, Status.Ok)
      case ClientErrorGraphQlResult(json, _, _) => Output.payload(json, Status.BadRequest)
      case BackendErrorGraphQlResult(json, _, _) => Output.payload(json, Status.InternalServerError)
    }
  }

  val games: Endpoint[List[Game]] = get("games") {
    Ok(List(Game(Team("Vikings"), Team("Ravens")), Game(Team("Vikings"), Team("Ravens"))))
  }

  def httpGetResource(resource: String, contentType: String) = get(resource) {
    getResource(resource, contentType)
  }

  val resources = {
    httpGetResource("jquery.min.js", "application/javascript") :+:
      httpGetResource("react.min.js", "application/javascript") :+:
      httpGetResource("fetch.min.js", "application/javascript") :+:
      httpGetResource("react-dom.min.js", "application/javascript") :+:
      httpGetResource("es6-promise.auto.min.js", "application/javascript") :+:
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
      httpGetResource("style.css", "text/css") :+:
      httpGetResource("graphiql.html", "text/html") :+:
      httpGetResource("index.html", "text/html") :+:
      httpGetResource("graphiql.min.css", "text/css") :+:
      httpGetResource("graphiql.min.js", "application/javascript")
  }

  try {
    Await.ready(Http.server.serve(":8081", (index :+:   resources :+: api).toService))
  } catch {
    case e: Exception => println(e.getLocalizedMessage)
  }
}
