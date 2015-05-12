package com.example

import akka.actor.Actor
import scala.concurrent.duration._

/**
 * Created by tombray on 5/11/15.
 */
trait DelayedReply {
  this: Actor =>

  implicit val ec = context.dispatcher

  def delayedReply(msg: Any) = {
    context.system.scheduler.scheduleOnce(250 milliseconds, sender(), msg)
  }

}
