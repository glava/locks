package org.zardina.repository

import org.zardina.Team

import scala.concurrent.Future

trait TeamRepository {

  def createTeam(name: String, acronym: String): Future[Int]

  def team(acronym: String): Future[Team]
}
