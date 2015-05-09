package com.example

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import BoringPlayer._
import scala.concurrent.duration._
/**
 * Created by tombray on 4/29/15.
 */
object BoringPlayer {
  def props = Props(new BoringPlayer)
  case object Ping
  case object Pong
  case object Beer

  val Sober = 3
  val Buzzed = 5
  val Drunk = 10
  val Hammered = 30
}

class BoringPlayer extends Actor with ActorLogging{
  implicit val ec = context.dispatcher

  override def preStart() = {
    super.preStart();
    println(self.path.name);
  }

  val rnd = new scala.util.Random

  //idle, playing, passedOut

  override def receive: Receive = accuracy(Sober, 0)

  def accuracy(ac: Int, drinkCount: Int):Receive = {
    case Pong => log.info("received pong"); randomReply(ac)
    case Beer =>
      val level = drinkCount match {
        case x if x < 2             => Sober
        case x if x >= 2 && x < 4   => Buzzed
        case x if x >= 4 && x < 8   => Drunk
        case _                      => Hammered
      }
      println(level)
      context.become(accuracy(level, drinkCount + 1))
      randomReply(level)
  }

  def randomReply(accuracy: Int) = {
    rnd.nextInt(accuracy) match {
      case 0 => delayedReply(Beer)
      case _ => delayedReply(Pong)
    }
  }

  def delayedReply(msg: Any) = {
    context.system.scheduler.scheduleOnce(1500 millis, sender(), msg)
  }
}
