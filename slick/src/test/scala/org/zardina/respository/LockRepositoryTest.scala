package org.zardina.respository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }
import org.zardina.repository.SlickLockRepository
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class LockRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val lockRepository = new SlickLockRepository(dataSource)(H2Profile, ExecutionContext.global)

  "Lock repository" should "be able to create a game" in {
    lockRepository.createLock("gogo", "1", "2", 10.0)
  }

}
