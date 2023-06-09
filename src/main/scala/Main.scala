package kaze.web.scrape


import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok

object HttpClientSingleRequest {

  val headers: List[HttpHeader] = List()
  def parseHeader(header: String, value: String): List[HttpHeader] = {
    HttpHeader.parse(header, value) match
      case Ok(header, errors) => headers :+ header
      case akka.http.scaladsl.model.HttpHeader.ParsingResult.Error(error) =>
        println(error)
        sys.error("something wrong")
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val headers: List[HttpHeader] = List()

    // parseHeader("User-Agent", "PostmanRuntime/7.30.0")
    // parseHeader("Accept", "*/*")
    // parseHeader("Accept-Encoding", "gzip, deflate, br")
    // parseHeader("Connection", "keep-alive")
    // parseHeader("Cookie", "session_tracker=jXHTJp1S7a7zMnnRHc.0.1686292337964.Z0FBQUFBQmtnc2R5WUFFdWJhc3hJS0tLbThIR2pKb2ZXRExieFRXeFRueGY3cGQxR2ZfaHQtNkFmZy1GSnRjSkRTdDdnSWlpOE1pUmtNSzZ4ejVGcWlPVzBjM09xT293NjU0TXhTLWhsQjhzMmpvMVRXTjlETFlkb3RUc2xfMFM0enZUdktIOWRYUEI; Path=/; Domain=reddit.com; Secure; Expires=Fri, 09 Jun 2023 08:32:17 GMT;")

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://www.reddit.com/r/scala.rss"))

    responseFuture
      .onComplete {
        case Success(res) => 
          println(res)
          // Get data from response
          res.entity.dataBytes.runForeach { chunk =>
            println(chunk.utf8String)
          }

        case Failure(_)   => sys.error("something wrong")
      }
  }
}