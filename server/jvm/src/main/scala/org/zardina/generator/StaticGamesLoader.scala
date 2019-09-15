package org.zardina.generator

import java.time.ZoneId

import scala.io.Source
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser.decode

/*
 Loads games from resource folder
*/
trait StaticGamesLoader {

  import java.text.SimpleDateFormat

  val format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
  def staticGameDetails: List[GameDetailsInstance] = decode[List[GameDetailsInstance]](json).right.get

  def json = Source.fromResource("season.json").getLines().mkString("\n")

  case class GameDetailsInstance(gameDate: String, homeTeamAbbr: String, visitorTeamAbbr: String, week: Int, gameTimeEastern: String, isoTime: Long) {
    lazy val utcTime = format.parse(s"$gameDate $gameTimeEastern").toInstant.atZone(ZoneId.of("GMT+8")).toLocalDateTime
  }

  implicit val gameDetailsInstanceDecoder: Decoder[GameDetailsInstance] = deriveDecoder[GameDetailsInstance]
  implicit val gameDetailsInstanceEncoder: Encoder[GameDetailsInstance] = deriveEncoder[GameDetailsInstance]

}

object LoadGames extends App with StaticGamesLoader {
  val x = staticGameDetails.filter(_.week == 2)
  x.sortBy(_.isoTime).foreach { x => println(s"${x.utcTime} ${x.gameTimeEastern}") }

}
