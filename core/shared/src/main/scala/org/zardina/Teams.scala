package org.zardina

case class Team(
  name: String,
  acronym: String,
  numOfWins: Int,
  numOfLoses: Int,
  numOfDraws: Int,
  created: Long,
  updated: Long)