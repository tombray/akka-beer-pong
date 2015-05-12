package com.example

import akka.actor.ActorSystem
import com.example.BeerPongProtocol.{Serve, Start}
import com.typesafe.config.ConfigFactory

/**
 * Created by tombray on 5/11/15.
 */
object BeerPongApp extends App{

  implicit val system = ActorSystem("BeerPong", ConfigFactory.load())

  val player1 = system.actorOf(BeerPongActor.props(), "player1")
  val player2 = system.actorOf(BeerPongActor.props(), "player2")

  player1 ! Start
  player2 ! Start

  player1 ! Serve(player2)
}
