package com.example

import akka.actor.ActorRef

/**
 * Created by tombray on 5/11/15.
 */
object BeerPongProtocol {

  sealed trait BeerPongMsg
  case object Start extends BeerPongMsg
  case object Hit extends BeerPongMsg
  case object Miss extends BeerPongMsg
  case object Drink extends BeerPongMsg
  case object YouWin extends BeerPongMsg
  case class Serve(opponent: ActorRef) extends BeerPongMsg
}
