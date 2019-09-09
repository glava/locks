package org.zardina.respository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }
import org.zardina.repository.Dao
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class LockRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val lockRepository = new Dao(dataSource)(H2Profile, ExecutionContext.global)

  "Lock repository" should "be able to create a game" in {
    val team = lockRepository.createTeam("CAR", "Carolina Panthers").futureValue should be(1)
    val user = lockRepository.createUser("gogo_botafogo", "gogili@golo.com", "avaala").futureValue
    val game = lockRepository.createGame("CAR", "AR", 1).futureValue
    lockRepository.createLock(user.id, game.home, "2", 10.0)
  }

}
