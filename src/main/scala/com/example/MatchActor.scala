package com.example

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.contrib.pattern.DistributedPubSubMediator.Publish
import com.example.MatchActor.{Score, Initialize}
import com.example.PingPongProtocol._

/**
 * Created by tombray on 5/6/15.
 */
object MatchActor {
  def props(mediator: ActorRef) = Props(new MatchActor(mediator))
  case object Initialize
  case class Score(playerOneScore: Int, playerTwoScore: Int)
}

class MatchActor(mediator: ActorRef) extends Actor with ActorLogging {


  override def preStart()=
    super.preStart()
    self ! Initialize

  override def receive() = initial

  def initial: Receive = {
    case Initialize =>
      log.info("initializing")
      mediator ! Publish("matches", MatchStarting(self))
      context.become(waitingForPlayerOne)
  }

  def waitingForPlayerOne: Receive = {
    case Join(playerOne: ActorRef) =>
      sender() ! Joined(playerOne)
      context.become(waitingForPlayerTwo(playerOne))
  }

  def waitingForPlayerTwo( playerOne: ActorRef ): Receive = {
    case Join(playerTwo: ActorRef) =>
      sender() ! Joined(playerTwo)
      startMatch(playerOne, playerTwo)

  }

  def started( playerOne: ActorRef, playerTwo: ActorRef, score: Score): Receive = rejectThirdPlayer orElse {
    case GetMatchState => sender() ! MatchStarted
    case Miss(playerWhoMissed: ActorRef) =>

      if (playerWhoMissed == playerOne) {
        println("player one missed " + score.toString)
        val updatedScore = score.copy(playerTwoScore = score.playerTwoScore + 1)
        playerWhoMissed ! updatedScore

        if (updatedScore.playerTwoScore == 11) {
          playerOne ! YouLose
          context.become(matchOver(playerOne, playerTwo, updatedScore))
        } else {
          context.become(started(playerOne, playerTwo, updatedScore))
        }

      } else {
        val updatedScore = score.copy(playerOneScore = score.playerOneScore + 1)
        playerWhoMissed ! updatedScore

        if (updatedScore.playerOneScore == 11) {
          playerTwo ! YouLose
          context.become(matchOver(playerOne, playerTwo, updatedScore))
        } else {
          context.become(started(playerOne, playerTwo, updatedScore))
        }


    }
  }

  def matchOver( playerOne: ActorRef, playerTwo: ActorRef, score: Score): Receive = {
    case GetMatchState => sender() ! MatchOver
  }

  def rejectThirdPlayer: Receive = {
    case Join(rejectedPlayer: ActorRef) => sender() ! Rejected(rejectedPlayer)
  }

  def startMatch(playerOne: ActorRef, playerTwo: ActorRef) = {
    playerOne ! Serve(playerTwo)
    context.become(started(playerOne, playerTwo, Score(0,0)))
  }

}
