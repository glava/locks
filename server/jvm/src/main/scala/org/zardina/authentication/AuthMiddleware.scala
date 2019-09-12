package org.zardina.authentication

import org.zardina.{ AuthorizationException, SangriaSchema }
import sangria.execution.{ FieldTag, Middleware, MiddlewareBeforeField, MiddlewareQueryContext }
import sangria.schema.Context

case object Authorized extends FieldTag

object AuthMiddleware extends Middleware[SangriaSchema.ApiContext] with MiddlewareBeforeField[SangriaSchema.ApiContext] {
  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[SangriaSchema.ApiContext, _, _]) = ()

  override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[SangriaSchema.ApiContext, _, _]) = ()

  override def beforeField(queryVal: QueryVal, mctx: MiddlewareQueryContext[SangriaSchema.ApiContext, _, _], ctx: Context[SangriaSchema.ApiContext, _]) = {
    val requireAuth = ctx.field.tags contains Authorized

    //if (requireAuth) throw new AuthorizationException("Hello!")

    continue
  }
}