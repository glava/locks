package org.zardina

object Teams {



}

trait Team {
  val id: String
  val name: String
  val numOfWins: Int
  val numOfLoses: Int
  val numOfDraws: Int
  val created: Long
  val updated: Long
}