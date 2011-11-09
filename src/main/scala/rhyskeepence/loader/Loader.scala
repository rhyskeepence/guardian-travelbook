package rhyskeepence.loader

import org.joda.time.DateTime
import com.mongodb.casbah.commons.Logging
import akka.actor.Actor._
import akka.actor.{Scheduler, Actor}
import java.util.concurrent.TimeUnit
import rhyskeepence.MongoStorage

class Loader extends Actor with Logging {
  val contentItemStore = new MongoContentItemStore(new MongoStorage)
  var latestContentProcessed: Option[DateTime] = None

  def receive = {
    case "poll" =>
      val processFrom = latestContentProcessed getOrElse contentItemStore.mostRecentLastModified

      println("Loader: reading content modified since " + processFrom)
      val content = ApiReader.contentModifiedSince(processFrom)

      contentItemStore.write(content filter { _.isInteresting } )

      latestContentProcessed = content.lastOption.map(_.lastModified) orElse latestContentProcessed
  }

}

object Loader {

  val loader = actorOf[Loader].start()

  def start() = {
    Scheduler.schedule(loader, "poll", 0, 60, TimeUnit.MINUTES)
  }

  def shutdown() { Scheduler.shutdown() }

}