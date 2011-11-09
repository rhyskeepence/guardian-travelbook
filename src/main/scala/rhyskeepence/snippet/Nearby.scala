package rhyskeepence.snippet

import rhyskeepence.MongoStorage
import net.liftweb.util.Helpers._
import xml.{Unparsed, NodeSeq}
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.SHtml
import net.liftweb.http.js.jquery.JqJsCmds.JqSetHtml
import rhyskeepence.loader.MongoContentItemStore

class Nearby {
  val contentItemStore = new MongoContentItemStore(new MongoStorage)

  def updatePosition(pos: Any): JsCmd = {
    pos match {
      case p: Map[String, Double] =>

        val itemsNearby = contentItemStore.itemsNear(p("lat"), p("lon"), 32, 0)

        JqSetHtml("content",
          itemsNearby.zipWithIndex.flatMap { case (item,index) =>
              <article class="article">
                <div class="thumb">
                    <img src={item.thumbnail.getOrElse("images/spacer.gif")}/>
                </div>
                <h2>
                  <a href="#" reveal={"popup" + index}>
                    {item.headline}
                  </a>
                </h2>
                <p class="body">
                  {Unparsed(item.trailText.getOrElse("unknown"))}
                </p>
                <div id={"popup" + index} class="actions">
                  <a href={item.url} class="nice large radius black button">Read article</a>
                  <a href={"http://maps.google.com/maps?q=" + item.geolocation.get.lat + ",+" + item.geolocation.get.lon +"+(" + item.headline+ ")&iwloc=A&hl=en"} class="nice large radius black button">Show location</a>
                </div>
              </article>
          }
        )

      case _ =>
        Alert("Sorry, your browser has sent us something strange. Please try again.")
        printf("Unexpected request from browser: %s", pos)
    }
  }

  def update(in: NodeSeq): NodeSeq = {
    Script(JsRaw(
      """loc.onUpdate = function(lat, lon) {
      """ + SHtml.jsonCall(JsObj("lat" -> JsVar("lat"), "lon" -> JsVar("lon")), updatePosition _)._2.toJsCmd + """;
    }"""
    ))
  }
}