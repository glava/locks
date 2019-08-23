package org.zardina.respository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }
import org.zardina.repository.SlickGameRepository
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class GameRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val gameRepository = new SlickGameRepository(dataSource)(H2Profile, ExecutionContext.global)

  "GameRepository" should "be able to create a week" in {
    gameRepository.createGame("CAR", "AR", 1).futureValue should be(1)
  }

}
