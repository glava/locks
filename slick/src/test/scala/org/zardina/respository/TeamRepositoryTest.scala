package org.zardina.respository

import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.concurrent.ScalaFutures
import org.zardina.repository.{ SlickTeamRepository }
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class TeamRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val teamRepository = new SlickTeamRepository(dataSource)(H2Profile, ExecutionContext.global)

  "TeamRepository" should "be able to create a team" in {
    teamRepository.createTeam("CAR", "Carolina Panthers").futureValue should be(1)
  }

}
