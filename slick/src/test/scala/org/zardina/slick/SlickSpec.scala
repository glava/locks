package org.zardina.slick

import java.util.UUID

import org.zardina.DbMigration
import slick.jdbc.DriverDataSource

trait SlickSpec {

  def dataSource = {
    Class.forName(classOf[org.h2.Driver].getName)
    val h2DataSource = new DriverDataSource(
      url = s"jdbc:h2:./target/${UUID.randomUUID().toString}", // wait for VM to die for closing in-memory db
      user = "sa",
      password = "")

    DbMigration.updateDatabase(h2DataSource)

    h2DataSource
  }
}