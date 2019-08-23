package org.zardina.respository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }
import org.zardina.repository.SlickUserRepository
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class UserRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val userRepository = new SlickUserRepository(dataSource)(H2Profile, ExecutionContext.global)

  "UserRepository" should "be able to create an user" in {
    userRepository.createUser("gogo_botafogo", "gogili@golo.com", "avaala").futureValue.email should be("gogili@golo.com")
  }

}