package rhyskeepence.loader

import org.joda.time.DateTime

case class ContentItem(
  id: String,
  url: String,
  headline: String,
  trailText: Option[String],
  thumbnail: Option[String],
  geolocation: Option[Geolocation],
  lastModified: DateTime) {

  def hasGeo = geolocation.isDefined
  def isInteresting = hasGeo && trailText.isDefined
}

case class Geolocation(lat: Double, lon: Double)
