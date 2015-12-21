package pl.yourcode.photocomparer.web

import com.paypal.cascade.json.JsonUtil
import pl.yourcode.photocomparer.exceptions.NoSuchJobException
import spray.http.StatusCodes._
import spray.http.{HttpEntity, MediaTypes, StatusCode}
import spray.routing.{Directive0, ExceptionHandler, HttpServiceBase, RequestContext}

import scala.util.control.NonFatal

object ExceptionsHandler {
  private case class Error(uri: String, method: String, body: Option[String], code: Int, message: Option[String])
}

trait ExceptionsHandler extends HttpServiceBase {

  import ExceptionsHandler._

  def handleExceptionsFilter: Directive0 = handleExceptions(eh)

  private def eh: ExceptionHandler = ExceptionHandler {
    case NonFatal(ex: NoSuchJobException) => ctx => createErrorResponse(ctx, NotFound, ex)
    case NonFatal(ex) => ctx => createErrorResponse(ctx, InternalServerError, ex)
  }

  private def createErrorResponse(ctx: RequestContext, code: StatusCode, ex: Throwable): Unit = {
    val uri = ctx.request.uri.toString()
    val method = ctx.request.method.name
    val body = ctx.request.entity.toOption.map { entity => entity.asString }
    val error = Error(uri, method, body, code.intValue, Option(ex.getMessage))
    val httpEntity = HttpEntity(MediaTypes.`application/json`, JsonUtil.toJson(error).get)
    ctx.complete(code, httpEntity)
  }
}
