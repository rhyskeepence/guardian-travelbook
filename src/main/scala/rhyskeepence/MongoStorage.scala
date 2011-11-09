package rhyskeepence

import net.liftweb.util.Props
import com.mongodb.casbah.{MongoCollection, MongoConnection}

class MongoStorage {
  com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers()

  private lazy val host = Props.get("mongo.host", "localhost")
  private lazy val port = Props.getInt("mongo.port", 27017)

  private lazy val username = Props.get("mongo.user", "")
  private lazy val password = Props.get("mongo.pass", "")

  def withCollection[T](collectionName: String)(block: MongoCollection => T) = {
    val mongo = MongoConnection(host, port)
    val travelbook = mongo("travelbook")

    try {
      if (travelbook.authenticate(username, password)) {
        val audits = travelbook(collectionName)
        block(audits)
      } else {
        throw new Exception("Cannot authenticate. Login failed.")
      }

    } finally {
      mongo.close()
    }
  }
}