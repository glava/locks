package org.zardina

import slick.jdbc.DriverDataSource

object DataSource {

  lazy val h2Connection = new DriverDataSource(
    url = "jdbc:h2:mem:locks;DB_CLOSE_DELAY=-1",
    user = "locker",
    password = "",
    driverClassName = classOf[org.h2.Driver].getName)

  lazy val mysqlConnection = new DriverDataSource(
    url = "jdbc:mysql://localhost:3306/locks?useServerPrepStmts=false",
    user = "root",
    password = "password",
    driverClassName = classOf[com.mysql.jdbc.Driver].getName)
}
