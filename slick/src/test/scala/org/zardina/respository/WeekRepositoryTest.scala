package org.zardina.respository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, Matchers }
import org.zardina.repository.SlickWeekRepository
import org.zardina.slick.SlickSpec
import slick.jdbc.H2Profile

import scala.concurrent.ExecutionContext

class WeekRepositoryTest extends FlatSpec with SlickSpec with Matchers with ScalaFutures {

  lazy val weekRepository = new SlickWeekRepository(dataSource)(H2Profile, ExecutionContext.global)

  "WeekRepository" should "be able to create a week" in {
    weekRepository.createWeek("CAR", "AR", 1).futureValue should be(1)
  }

}
