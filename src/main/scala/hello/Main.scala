package hello

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpResponse
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext }
import scala.util.Failure
import scala.util.Success

import java.time.Duration

object QuickstartApp {
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val routes = new UserRoutes("/data/message.txt")(context.system)
      startHttpServer(routes.userRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
}

class UserRoutes(messageFilepath: String)(implicit val system: ActorSystem[_]) {

import akka.http.scaladsl.model.StatusCodes
  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(Duration.ofSeconds(5))
  private implicit val executionContext = ExecutionContext.global

  val userRoutes: Route =
    path("") {
      get {
        val f: Future[String] = Future {
          val source = scala.io.Source.fromFile(messageFilepath)
          val lines = try source.mkString finally source.close()
          lines
        }
        onComplete(f) {
          case Success(lines) => complete(lines)
          case Failure(t) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "An error has occurred: " + t.getMessage))
        }
      }
    }
}