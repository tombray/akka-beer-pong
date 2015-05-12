package com.example.cluster

import akka.actor.ActorSystem
import akka.contrib.pattern.ClusterSharding
import com.example.PingPongProtocol.CreatePlayerCmd
import com.typesafe.config.ConfigFactory

object ClusterNodeApp extends App {
  val conf =
    """ akka.remote.netty.tcp.hostname="%hostname%"
      |akka.remote.netty.tcp.port=%port%
    """.stripMargin

  val argumentsError = """
   Please run the service with the required arguments: <hostIpAddress> <port> """

  assert(args.length == 2, argumentsError)

  val hostname = args(0)
  val port = args(1).toInt
  val baseConfig = ConfigFactory.load()
  val clusterConfig = baseConfig.getConfig("clusterApp").withFallback(baseConfig)

  val config =
    ConfigFactory.parseString(conf.replaceAll("%hostname%", hostname)
      .replaceAll("%port%", port.toString)).withFallback(clusterConfig)

  // Create an Akka system
  implicit val clusterSystem = ActorSystem("PingPong", config)

  val playerProcessorRegion = ClusterBoot.boot(false, port)(clusterSystem)
//
//  (1 to 100000) foreach { x =>
//    playerProcessorRegion ! CreatePlayerCmd(s"player$x")
//  }

  //(3 to 100000) foreach { x => system.actorOf(BoringPlayer.props, s"player${x}")}
  // This example app will ping pong 3 times and thereafter terminate the ActorSystem - 
  // see counter logic in PingActor
  clusterSystem.awaitTermination()
}