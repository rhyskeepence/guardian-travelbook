package rhyskeepence.loader

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import com.gu.openplatform.contentapi.Api
import com.gu.openplatform.contentapi.connection.JavaNetHttp
import com.gu.openplatform.contentapi.model.ItemResponse
import net.liftweb.util.Props

object ApiReader {

  object Api extends Api with JavaNetHttp {
    apiKey = Props.get("guardian.api.key")

    override def GET(urlString: String, headers: Iterable[(String, String)]) = {
      println("get ::: " + urlString)
      super.GET(urlString, headers);
    }
  }

  def fetchContent(dt: DateTime, currentPage: Int, results: List[ContentItem]): List[ContentItem] = {
    val response: ItemResponse = Api.item.itemId("travel/travel").fromDate(dt)
      .showFields("latitude,longitude,lastModified,trailText,thumbnail,headline")
      .orderBy("oldest").page(currentPage).pageSize(50).response

    val pages: Int = response.pages.getOrElse(1)

    val newResults = results ++ response.results.map { content =>
      val fields = content.fields.get
      val lastMod = fields.get("lastModified").get

      ContentItem(
        id = content.id,
        headline = fields.get("headline").getOrElse(content.webUrl),
        url = content.webUrl,
        trailText = fields.get("trailText"),
        thumbnail = fields.get("thumbnail"),
        geolocation = locationFrom(fields.get("latitude"), fields.get("longitude")),
        lastModified = ISODateTimeFormat.dateTimeParser().parseDateTime(lastMod)
      )
    }

    if (pages > currentPage) {
      fetchContent(dt, currentPage + 1, newResults)
    } else {
      newResults
    }

  }

  def contentModifiedSince(dt: DateTime) = {
    fetchContent(dt, 1, List())
  }

  def locationFrom(latitude: Option[String], longitude: Option[String]) = {
    if (latitude.isDefined && longitude.isDefined)
      Some(Geolocation(
        latitude.get.toDouble,
        longitude.get.toDouble
      ))
    else None
  }

}