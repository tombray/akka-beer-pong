package com.example

import akka.actor.ActorRef

/**
 * Created by tombray on 5/6/15.
 */
object PingPongProtocol {
  case object RequestForPlayers
  case class Join(player: ActorRef)
  case class Joined(player: ActorRef)
  case class Rejected(player: ActorRef)
  case class Serve(opponent: ActorRef)
  case class Miss(playerWhoMissed: ActorRef)
  case object YouLose
  case object GetMatchState
  case object MatchOver
  case object MatchStarted

  sealed trait PlayerMsg {
    val playerId: String
  }
  sealed trait PlayerCmd extends PlayerMsg
  case class CreatePlayerCmd(playerId: String) extends PlayerCmd

  sealed trait PlayerEvt extends PlayerMsg
  case class PlayerCreatedEvt(playerId: String) extends PlayerEvt


  case object Ping

  case class MatchStarting(matchActor: ActorRef)
}
