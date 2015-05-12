package com.example

import akka.actor.{Props, Actor, ActorLogging}
import com.example.BeerPongProtocol._
import scala.concurrent.duration._

/**
 * Created by tombray on 5/11/15.
 */
object BeerPongActor {
  def props() = Props(new BeerPongActor)
}

class BeerPongActor extends Actor with ActorLogging with HitOrMiss {

  implicit val ec = context.dispatcher

  def receive: Receive = idle(0)

  def idle(totalBeersConsumed: Int): Receive = {
    case Start =>
      log.info(s"totalBeersConsumed: ${totalBeersConsumed}")
      context.become(playing(10)(totalBeersConsumed))
  }

  def playing( cups: Int )(implicit totalBeersConsumed: Int): Receive = {

    case Serve(opponent) => opponent ! hitOrMiss(cups, totalBeersConsumed)

    case Hit =>
      log.info(s"drink! ${cups}")
      cups match {
        case 1 =>
          sender() ! YouWin
          becomeIdle(cups - 1)
        case _ => sendHitOrMiss(cups - 1)
      }

    case Miss => sendHitOrMiss(cups)

    case YouWin =>
      log.info("Woohoo! I win")
      becomeIdle(cups)
  }

  private def sendHitOrMiss( cups: Int )(implicit totalBeersConsumed: Int) = {
    log.info(s"sendHitOrMiss(${cups})")
    delayedReply( hitOrMiss(cups, totalBeersConsumed) )
    context.become(playing(cups))
  }

  private def becomeIdle(cupsRemaining: Int)( implicit totalBeersConsumed: Int) = {
    context.become(idle(totalBeersConsumed + (10 - cupsRemaining)))
  }

  private def delayedReply(msg: Any) = {
    context.system.scheduler.scheduleOnce(1 second, sender(), msg)
  }
}
