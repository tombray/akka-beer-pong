package com.example

import akka.actor.ActorSystem
import akka.contrib.pattern.ClusterSharding
import com.example.BoringPlayer.{Pong, Ping}
import com.example.PingPongProtocol.CreatePlayerCmd
import com.example.cluster.PlayerProcessor
import com.typesafe.config.ConfigFactory

object ApplicationMain extends App {
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

  val playerProcessorRegion = ClusterSharding(clusterSystem).start(
    typeName = PlayerProcessor.shardName,
    entryProps = Some(PlayerProcessor.props()),
    idExtractor = PlayerProcessor.idExtractor,
    shardResolver = PlayerProcessor.shardResolver)

  (1 to 100000) foreach { x =>
    playerProcessorRegion ! CreatePlayerCmd(s"player$x")
  }

  //(3 to 100000) foreach { x => system.actorOf(BoringPlayer.props, s"player${x}")}
  // This example app will ping pong 3 times and thereafter terminate the ActorSystem - 
  // see counter logic in PingActor
  clusterSystem.awaitTermination()
}