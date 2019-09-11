package org.zardina.graphql

import com.twitter.util.Future
import io.circe.Json
import sangria.execution._
import sangria.marshalling.circe.{ CirceInputUnmarshaller, CirceResultMarshaller }
import sangria.schema.Schema
import io.circe.parser._
import scala.concurrent.ExecutionContext
import org.zardina._
trait GraphQlQueryExecutor {
  /**
   * Executes a GraphQL query, or mutation.
   *
   * The resulting `Future` will contain either:
   *
   * - `Return` - Indicates a (best guess) successful execution of a query. Will contain a subtype of `GraphQlResult`,
   * representing whether this was a) `SuccessfulGraphQlResult` a successful result, b) `ClientErrorGraphQlResult` a
   * client error (e.g. bad query) or b) `ServerErrorGraphQlResult` - an internal error (e.g. malformed GraphQL schema).
   * - `Throw` - Indicates a catastrphic failure, that a caller should not be expected to handle.
   *
   * Note. There is still a possibility in the success case that the query could "fail", for example if a downstream
   * service returns an error, and that error is handled by the `exceptionHandler` (see below), the query will be
   * considered a success. The `errors` key within the returns `Json` instance will be non-empty however.
   *
   * More information is in this thread: https://gitter.im/sangria-graphql/sangria?at=57e1e94933c63ba01a1c91e5
   */
  def execute(q: GraphQlQuery)(implicit ec: ExecutionContext): Future[GraphQlResult]

  /**
   * A reference to the original context associated with the GraphQL query.
   *
   * @return The context
   */
  def context: Any
}

object GraphQlQueryExecutor {
  val ExecutionPrefix = "graphql_execution"

  def executor[C](schema: Schema[C, Unit], rootContext: C, maxQueryDepth: Int, exceptionHandler: ExceptionHandler, middleware: Middleware[C]): GraphQlQueryExecutor =
    new GraphQlQueryExecutor_(schema, rootContext, maxQueryDepth, exceptionHandler, middleware)
}

private final class GraphQlQueryExecutor_[C](schema: Schema[C, Unit], rootContext: C, maxQueryDepth: Int, exceptionHandler: ExceptionHandler, middleware: Middleware[C])
  extends GraphQlQueryExecutor {
  private val resultMarshaller = CirceResultMarshaller
  private val inputMarshaller = CirceInputUnmarshaller
  private val executionScheme = ExecutionScheme.Extended

  override def execute(q: GraphQlQuery)(implicit ec: ExecutionContext): Future[GraphQlResult] = {
    val result = runQuery(q)(ec)
    handleErrors(result.asTwitter(ec))
  }

  override def context =
    rootContext

  private def runQuery(q: GraphQlQuery)(implicit ec: ExecutionContext) = {
    Executor.execute[C, Unit, Json](
      schema = schema,
      queryAst = q.document,
      userContext = rootContext,
      operationName = q.operationName,
      middleware = List(middleware),
      variables = q.variables.getOrElse(parse("{}").right.get),
      exceptionHandler = exceptionHandler,
      maxQueryDepth = Some(maxQueryDepth))(
        executionContext = ec,
        marshaller = resultMarshaller,
        um = inputMarshaller,
        scheme = executionScheme)
  }

  private def handleErrors(result: Future[ExecutionResult[C, Json]]) =
    result.map { er =>
      if (er.errors.isEmpty) {
        SuccessfulGraphQlResult(er.result, Some(er.ctx.asInstanceOf[Any]))
      } else {
        BackendErrorGraphQlResult(er.result, er.errors, Some(er.ctx.asInstanceOf[Any]))
      }
    }
}