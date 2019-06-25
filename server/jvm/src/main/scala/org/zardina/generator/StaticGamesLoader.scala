package org.zardina.generator

import scala.io.Source
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser.decode

/*
 Loads games from resource folder
*/
trait StaticGamesLoader {

  def staticGameDetails = decode[List[GameDetailsInstance]](json).right.get

  lazy val json = Source.fromResource("season.json").getLines().mkString("\n")

  case class GameDetailsInstance(gameDate: String, homeTeamAbbr: String, visitorTeamAbbr: String, week: Int)

  implicit val gameDetailsInstanceDecoder: Decoder[GameDetailsInstance] = deriveDecoder[GameDetailsInstance]
  implicit val gameDetailsInstanceEncoder: Encoder[GameDetailsInstance] = deriveEncoder[GameDetailsInstance]

}
