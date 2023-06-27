import akka.http.scaladsl.Http
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

object HTTPServer {
  def main(args: Array[String]) = {
    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    implicit val executionContext = system.executionContext

    lazy val server = Http().newServerAt("localhost", 8080).bind(rootRoute)

    lazy val rootRoute: Route = concat(
      path("hello") {
        get {
          complete("Hello, World!")
        }
      }
    )
    
    server.map { _ =>
      println("Successfully started on localhost:9090 ")
    } recover {
      case ex =>
        println("Failed to start the server due to: "+ex.getMessage)
    }

  }
}
