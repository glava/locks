package org.zardina


import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom.ext.Ajax

import scala.concurrent.{ExecutionContext, Future}

class LocksApiClient(implicit ex: ExecutionContext) {

  def games(): Future[List[Game]] = {
    Ajax.get("/games").flatMap(response => decode[List[Game]](response.responseText) match {
      case Left(r) => Future.failed(r)
      case Right(r) =>
        println(r)
        Future.successful(r)
    })
  }

  def selection(): Future[Boolean] = {
    Ajax.post("/selection").flatMap(response => decode[Boolean](response.responseText) match {
      case Left(r) => Future.failed(r)
      case Right(r) =>
        println(r)
        Future.successful(r)
    })
  }
}
