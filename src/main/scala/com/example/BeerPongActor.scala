package com.example

import akka.actor.{Props, Actor, ActorLogging}
import akka.event.LoggingReceive
import com.example.BeerPongProtocol._
import com.example.PingPongProtocol.Join

/**
 * Created by tombray on 5/11/15.
 */
object BeerPongActor {
  def props() = Props(new BeerPongActor)
}

class BeerPongActor extends Actor with ActorLogging with HitOrMiss {

  def receive: Receive = idle

  def idle: Receive = {
    case Start => context.become(playing(10))
  }

  def playing( cups: Int ): Receive = {
    case Serve(opponent) => opponent ! hitOrMiss(cups)
    case Hit => drink(cups)
    case Miss => sendHitOrMiss(cups)
    case YouWin =>
      log.info("Woohoo! I win")
      context.become(idle)
  }

  private def drink( cups: Int) = {
    log.info(s"drink(${cups})")
    cups match {
      case 1 =>
        sender() ! YouWin
        context.become(idle)
      case _ => sendHitOrMiss(cups - 1)
    }
  }

  private def sendHitOrMiss( cups: Int ) = {
    log.info(s"sendHitOrMiss(${cups})")
    sender() ! hitOrMiss(cups)
    context.become(playing(cups))
  }

}
