package kaze.web.scrape

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok

import scala.xml._
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Await

object RSS {
  val headers: List[HttpHeader] = List()
  def parseHeader(header: String, value: String): List[HttpHeader] = {
    HttpHeader.parse(header, value) match {
      case Ok(header, errors) => headers :+ header
      case akka.http.scaladsl.model.HttpHeader.ParsingResult.Error(error) =>
        println(error)
        sys.error("something wrong")
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val responseFuture: Future[HttpResponse] = Http().singleRequest(
      HttpRequest(uri = "https://www.reddit.com/r/scala.rss")
    )

    val response = Await.result(
      responseFuture.flatMap(resp => Unmarshal(resp.entity).to[String]),
      scala.concurrent.duration.Duration.Inf
    )

    // println(response)

    val xml = XML.loadString(response)

    val entries = xml \\ "entry"

    /* 
      Each entry follows the following format
      <entry>
        <author>
          <name>...</name>
          <uri>...</uri>
        </author>
        <category />
        <content>...</content>
        <id>...</id>
        <link href="..." />
        <updated>...</updated>
        <published>...</published>
        <title>...</title>
      </entry>
    */

    case class Entry(
      authorName: String,
      authorUri: String,
      category: String,
      content: String,
      id: String,
      link: String,
      updated: String,
      published: String,
      title: String
    )


    val data = entries.map { entry =>
      Entry(
        (entry \ "author" \ "name").text,
        (entry \ "author" \ "uri").text,
        (entry \ "category").text,
        (entry \ "content").text,
        (entry \ "id").text,
        (entry \ "link" \ "@href").text,
        (entry \ "updated").text,
        (entry \ "published").text,
        (entry \ "title").text
      )
    }
  }
}
