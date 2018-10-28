package org.zardina.respository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }
import org.zardina.repository.SlickGameRepository
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class GameRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val gameRepository = new SlickGameRepository(dataSource)(H2Profile, ExecutionContext.global)

  "Game repository" should "be able to create a game" in {
    gameRepository.createGame("gogo", "1", "2", 10.0)
  }

}
