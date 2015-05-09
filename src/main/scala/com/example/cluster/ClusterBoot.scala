package com.example.cluster

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern._

/**
 * Created by tombray on 5/8/15.
 */
object ClusterBoot {

  def boot(proxyOnly: Boolean = false, port: Int, storeActorPathOption: Option[String] = None)(clusterSystem: ActorSystem): (ActorRef) = {

    val storeActorPath = storeActorPathOption.getOrElse("akka.tcp://PingPong@127.0.0.1:2553/user/store")
    startupSharedJournal(clusterSystem, (port == 2553), ActorPath.fromString(storeActorPath))

    def playerEntryProps(proxyOnly: Boolean) = if (proxyOnly) None else Some(PlayerProcessor.props())

    val playerProcessorRegion = ClusterSharding(clusterSystem).start(
      typeName = PlayerProcessor.shardName,
      entryProps = playerEntryProps(proxyOnly),
      idExtractor = PlayerProcessor.idExtractor,
      shardResolver = PlayerProcessor.shardResolver)

    (playerProcessorRegion)
  }

    private def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore) {
      val store = system.actorOf(Props[SharedLeveldbStore], "store")
      println(s"Store started at ${store.path}")
    }
    // register the shared journal
    import system.dispatcher
    implicit val timeout = Timeout(15.seconds)
    val f = (system.actorSelection(path) ? Identify(None))
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.shutdown()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.shutdown()
    }
  }

}
