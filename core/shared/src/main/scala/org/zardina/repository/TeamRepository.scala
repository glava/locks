package org.zardina.repository

import scala.concurrent.Future

trait TeamRepository {

  def createTeam(id: String, name: String): Future[Int]
}
