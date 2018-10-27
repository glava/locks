package org.zardina

import javax.sql.DataSource
import liquibase.{ Contexts, Liquibase }
import liquibase.database.DatabaseConnection
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

import scala.util.Try

object DbMigration {

  def createLiquibase(databaseConnection: DatabaseConnection) = {
    new Liquibase("db/db.changelog.xml", new ClassLoaderResourceAccessor(), databaseConnection)
  }

  def updateDatabase(dataSource: DataSource): Unit =
    try {
      val liquibase = createLiquibase(new JdbcConnection(dataSource.getConnection))
      liquibase.update(new Contexts(""))
    } catch {
      case e: Exception => println(e.getLocalizedMessage)
    }

}
