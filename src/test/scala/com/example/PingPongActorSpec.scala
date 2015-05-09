package com.example

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{TestProbe, TestActors, TestKit, ImplicitSender}
import com.example.MatchActor.Score
import com.example.PingPongProtocol._
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration._
 
class PingPongActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("MySpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "A Ping actor" must {
    "send back a ping on a pong" in {
      val pingActor = system.actorOf(PingActor.props)
      pingActor ! PongActor.PongMessage("pong")
      expectMsg(PingActor.PingMessage("ping"))
    }
  }

  "A Pong actor" must {
    "send back a pong on a ping" in {
      val pongActor = system.actorOf(PongActor.props)
      pongActor ! PingActor.PingMessage("ping")
      expectMsg(PongActor.PongMessage("pong"))
    }
  }

  "A Match actor" must {
    "send a RequestForPlayers" in {
      val mediatorProbe = TestProbe()
      val matchActor = system.actorOf(MatchActor.props(mediatorProbe.ref))
      mediatorProbe.expectMsg(PingPongProtocol.RequestForPlayers)
    }

    "accept two players, reject third" in {
      val mediatorProbe = TestProbe()
      val matchActor = system.actorOf(MatchActor.props(mediatorProbe.ref))
      mediatorProbe.expectMsg(PingPongProtocol.RequestForPlayers)

      val playerOneProbe = TestProbe()
      matchActor ! Join(playerOneProbe.ref)
      expectMsg(Joined(playerOneProbe.ref))

      val playerTwoProbe = TestProbe()
      matchActor ! Join(playerTwoProbe.ref)
      expectMsg(Joined(playerTwoProbe.ref))

      val playerThreeProbe = TestProbe()
      matchActor ! Join(playerThreeProbe.ref)
      expectMsg(Rejected(playerThreeProbe.ref))
    }

    "accept two players, tell first one to Serve" in {
      val mediatorProbe = TestProbe()
      val matchActor = system.actorOf(MatchActor.props(mediatorProbe.ref))
      mediatorProbe.expectMsg(PingPongProtocol.RequestForPlayers)

      val playerOneProbe = TestProbe()
      matchActor ! Join(playerOneProbe.ref)
      expectMsg(Joined(playerOneProbe.ref))

      val playerTwoProbe = TestProbe()
      matchActor ! Join(playerTwoProbe.ref)
      expectMsg(Joined(playerTwoProbe.ref))

      playerOneProbe.expectMsg(Serve(playerTwoProbe.ref))

    }

    "score one for the opponent when a player misses" in {
      val mediatorProbe = TestProbe()
      val matchActor = system.actorOf(MatchActor.props(mediatorProbe.ref))
      mediatorProbe.expectMsg(PingPongProtocol.RequestForPlayers)

      val playerOneProbe = TestProbe()
      matchActor ! Join(playerOneProbe.ref)
      expectMsg(Joined(playerOneProbe.ref))

      val playerTwoProbe = TestProbe()
      matchActor ! Join(playerTwoProbe.ref)
      expectMsg(Joined(playerTwoProbe.ref))

      playerOneProbe.expectMsg(Serve(playerTwoProbe.ref))

      matchActor ! Miss(playerOneProbe.ref)
      playerOneProbe.expectMsg(Score(0,1))

      matchActor ! Miss(playerOneProbe.ref)
      playerOneProbe.expectMsg(Score(0,2))

      matchActor ! Miss(playerTwoProbe.ref)
      playerTwoProbe.expectMsg(Score(1,2))

      (1 to 9) foreach { _ =>
        matchActor ! Miss(playerOneProbe.ref)
        playerOneProbe.ignoreMsg { case Score(_,_) => true }
      }

      awaitAssert {
        within(3 seconds) {
          playerOneProbe.expectMsg(YouLose)
        }
      }
    }

    "player 2 loses" in {
      val mediatorProbe = TestProbe()
      val matchActor = system.actorOf(MatchActor.props(mediatorProbe.ref))
      mediatorProbe.expectMsg(PingPongProtocol.RequestForPlayers)

      val playerOneProbe = TestProbe()
      matchActor ! Join(playerOneProbe.ref)
      expectMsg(Joined(playerOneProbe.ref))

      val playerTwoProbe = TestProbe()
      matchActor ! Join(playerTwoProbe.ref)
      expectMsg(Joined(playerTwoProbe.ref))

      playerOneProbe.expectMsg(Serve(playerTwoProbe.ref))

      (1 to 11) foreach { _ =>
        matchActor ! Miss(playerTwoProbe.ref)
        playerTwoProbe.ignoreMsg { case Score(_,_) => true }
      }

      awaitAssert {
        within(3 seconds) {
          playerTwoProbe.expectMsg(YouLose)
        }
      }

      matchActor ! GetMatchState
      expectMsg(MatchOver)
    }
  }

}
