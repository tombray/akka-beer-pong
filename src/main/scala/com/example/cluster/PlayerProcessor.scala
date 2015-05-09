package com.example.cluster

import akka.actor.{ActorRef, ActorLogging, Props}
import akka.contrib.pattern.DistributedPubSubMediator.{SubscribeAck, Subscribe}
import akka.contrib.pattern.{DistributedPubSubExtension, ShardRegion}
import akka.persistence.{RecoveryCompleted, PersistentActor}
import akka.testkit.ImplicitSender
import com.example.PingPongProtocol
import com.example.PingPongProtocol._

/**
 * Created by tombray on 5/7/15.
 */
object PlayerProcessor {
  def props() = Props(new PlayerProcessor())

  val idExtractor: ShardRegion.IdExtractor = {
    case cmd: PlayerCmd => (cmd.playerId.toString, cmd)
  }

  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case cmd: PlayerCmd => (math.abs(cmd.playerId.hashCode) % 10).toString
  }

  val shardName: String = "PlayerProcessor"
}

class PlayerProcessor extends PersistentActor with ActorLogging{

  val mediator = DistributedPubSubExtension(context.system).mediator

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  def receiveRecover: Receive = {
    case RecoveryCompleted =>
    case x => log.info(s"received recover msg ${x.toString}")
  }

  def receiveCommand: Receive = {
    case CreatePlayerCmd(playerId) =>
      persist(PlayerCreatedEvt(playerId)) { evt =>
        mediator ! Subscribe("matches", self)
      }

    case SubscribeAck(Subscribe("matches", None, `self`)) =>
      context.become(ready)
      unstashAll()
    case _ => log.info("wasn't ready"); stash()
  }

  def ready: Receive = {
    case MatchStarting(matchActor: ActorRef) => matchActor ! Join(self)
    case Joined(matchActor: ActorRef) => context.become(joinedMatch(matchActor))
  }

  def joinedMatch(matchActor: ActorRef): Receive = {
    case Serve(otherPlayer) => otherPlayer ! Ping; log.info(s"serving to ${otherPlayer.path.name}")
    case Ping => log.info(s"received Ping from ${sender().path.name}")
  }
}
