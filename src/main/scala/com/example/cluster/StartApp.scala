package com.example.cluster

import akka.actor.{Props, ActorSystem}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Publish
import com.example.MatchActor
import com.example.PingPongProtocol.{MatchStarting, CreatePlayerCmd}
import com.typesafe.config.ConfigFactory

object StartApp extends App {
  val conf =
    """ akka.remote.netty.tcp.hostname="%hostname%"
      |akka.remote.netty.tcp.port=%port%
    """.stripMargin

  val argumentsError = """
   Please run the service with the required arguments: <hostIpAddress> <port> """

  assert(args.length == 2, argumentsError)

  val hostname = args(0)
  val port = args(1).toInt
  val config =
    ConfigFactory.parseString(conf.replaceAll("%hostname%", hostname)
      .replaceAll("%port%", port.toString)).withFallback(ConfigFactory.load())

  // Create an Akka system
  implicit val clusterSystem = ActorSystem("PingPong", config)
  val mediator = DistributedPubSubExtension(clusterSystem).mediator

  val playerProcessorRegion = ClusterBoot.boot(false, port)(clusterSystem)
//
  (1 to 1000) foreach { x =>
    playerProcessorRegion ! CreatePlayerCmd(s"player$x")
  }


  Thread.sleep(5000)

  (1 to 500) foreach { x =>
    clusterSystem.actorOf(Props(classOf[MatchActor], mediator))
  }



  //mediator ! Publish("matches", MatchStarting(matchActor))

  /*
  Player becomes idle. Tells local match maker that he wants to play.
  Local match maker knows what players and matches are available.

  Player switches to idle and registers for a new match.
  Player broadcasts availability, Matches receive
  LocalPlayerRegistry
    Players become available and register here
  Match gets first player who is drunk, needs to find another player who is drunk
  MatchRegistry
    A match gets created and
   */

  //(3 to 100000) foreach { x => system.actorOf(BoringPlayer.props, s"player${x}")}
  // This example app will ping pong 3 times and thereafter terminate the ActorSystem - 
  // see counter logic in PingActor
  clusterSystem.awaitTermination()
}