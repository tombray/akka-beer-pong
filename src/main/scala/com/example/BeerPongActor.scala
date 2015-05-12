package com.example

import akka.actor.{Props, Actor, ActorLogging}
import com.example.BeerPongProtocol._
import BeerPongActor._
/**
 * Created by tombray on 5/11/15.
 */
object BeerPongActor {
  def props() = Props(new BeerPongActor)

  case class PlayerState(totalBeersConsumed: Int, cupsRemaining: Int = 0)
}

class BeerPongActor extends Actor with ActorLogging with HitOrMiss with DelayedReply {

  def receive: Receive = idle(PlayerState(0))

  def idle(state: PlayerState): Receive = {
    case Start =>
      log.info(s"state: ${state}")
      context.become(playing(state.copy(cupsRemaining = 10)))
  }

  def playing(implicit state: PlayerState): Receive = {

    case Serve(opponent) => opponent ! hitOrMiss

    case Hit =>
      log.info("I have to drink!")
      state.cupsRemaining match {
        case 1 =>
          sender() ! YouWin
          becomeIdle(oneLessCup)
        case _ => sendHitOrMiss(oneLessCup)
      }

    case Miss => sendHitOrMiss(state)

    case YouWin =>
      log.info("Woohoo! I win")
      becomeIdle(state)
  }

  private def sendHitOrMiss(state: PlayerState) = {
    log.info(s"sendHitOrMiss(${state})")
    delayedReply( hitOrMiss(state) )
    context.become(playing(state))
  }

  private def becomeIdle(state: PlayerState) = {
    context.become(idle(state.copy(totalBeersConsumed = state.totalBeersConsumed + (10 - state.cupsRemaining))))
  }

  private def oneLessCup(implicit state: PlayerState) = state.copy(cupsRemaining = state.cupsRemaining - 1)
}
