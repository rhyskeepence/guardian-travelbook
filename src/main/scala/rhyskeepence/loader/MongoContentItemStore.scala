package rhyskeepence.loader

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import rhyskeepence.MongoStorage
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.query.GeoCoords
import com.mongodb.casbah.Imports._

class MongoContentItemStore(mongoStorage: MongoStorage) {

  val initialDate = new DateTime(2008, 10, 1, 0, 0, 0, 0)

  def write(items: List[ContentItem]) {
    println("MongoWriter: Writing %d items to mongo..." format items.size)
    items foreach {
      write(_)
    }
  }

  def write(item: ContentItem) = {
    val contentItemBuilder = MongoDBObject.newBuilder
    contentItemBuilder += "_id" -> item.id
    contentItemBuilder += "headline" -> item.headline
    contentItemBuilder += "url" -> item.url
    contentItemBuilder += "trailText" -> item.trailText
    contentItemBuilder += "thumbnail" -> item.thumbnail
    contentItemBuilder += "lastModified" -> item.lastModified

    item.geolocation.foreach {
      loc =>
        val geolocation = MongoDBObject.newBuilder
        geolocation += "lat" -> loc.lat
        geolocation += "lon" -> loc.lon
        contentItemBuilder += "geolocation" -> geolocation.result
    }

    withContent {
      _ += contentItemBuilder.result
    }
  }

  def mostRecentLastModified = {
    withContent {
      _.find()
        .sort(MongoDBObject("lastModified" -> -1))
        .limit(1)
        .toList
        .headOption
        .map(_.get("lastModified").asInstanceOf[DateTime])
        .getOrElse(initialDate)

    }
  }

  def itemsNear(lat: Double, lon: Double, limit: Int, skip: Int) = {
    withContent {
      contentCollection =>

        contentCollection
          .find("geolocation" $near GeoCoords(lat, lon))
          .skip(skip)
          .limit(limit)
          .toList.map { toContentItem }

    }
  }

  private def toContentItem: (DBObject) => ContentItem = {
    dbObject =>
      ContentItem(
        dbObject.getAsOrElse[String]("_id", "unknown"),
        dbObject.getAsOrElse[String]("url", "unknown"),
        dbObject.getAsOrElse[String]("headline", "unknown"),
        dbObject.getAs[String]("trailText"),
        dbObject.getAs[String]("thumbnail"),
        Some(Geolocation(
          dbObject.getAs[BasicDBObject]("geolocation").get.getAsOrElse[Double]("lat", 0),
          dbObject.getAs[BasicDBObject]("geolocation").get.getAsOrElse[Double]("lon", 0)
        )),
        dbObject.getAsOrElse[DateTime]("lastModified", initialDate)
      )
  }

  private def withContent[T](doWithContent: MongoCollection => T) = {
    mongoStorage.withCollection("content")(doWithContent)
  }

}