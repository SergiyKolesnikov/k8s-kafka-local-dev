package hello

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{StatusCodes, HttpResponse}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.blocking
import scala.concurrent.{Future, ExecutionContext}
import scala.util.Failure
import scala.util.Success
import scala.language.postfixOps

object QuickstartApp {

  def main(args: Array[String]): Unit = {
    val httpServer = new UserRoutes()
    httpServer.start()
    println("HTTP Server is starting!")
    sys.ShutdownHookThread {
      httpServer.stop()
    }
  }
}

class UserRoutes(
  val host: String = "0.0.0.0",
  val port: Int = 8080,
  val messageFilepath: String = "/data/message.txt"
) {

  private var bindingFuture: Future[Http.ServerBinding] = null

  private implicit val system = ActorSystem("HelloWorldHttpServer")
  private implicit val executionContext = system.dispatcher

  val userRoutes: Route =
    concat(
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
      },
      path("sleep-short") {
        get {
          // The Future will prevent sleep() from blocking the server thread.
          val f: Future[String] = Future {
            s"I am awake after ${sleep(1000)}"
          }
          onComplete(f) {
            case Success(lines) => complete(lines)
            case Failure(t) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "An error has occurred: " + t.getMessage))
          }
        }
      },
      path("sleep-long") {
        get {
          // The Future will prevent sleep() from blocking the server thread.
          // So, you can run /sleep-long endpoint and then /sleep-short. The
          // /sleep-short will return much faster, as expected, because the two
          // will run in two different threads and none of them will block the
          // server thread.
          val f: Future[String] = Future {
            s"I am awake after ${sleep(15000)}"
          }
          onComplete(f) {
            case Success(lines) => complete(lines)
            case Failure(t) => complete(HttpResponse(StatusCodes.InternalServerError, entity = "An error has occurred: " + t.getMessage))
          }
        }
      }
    )

  def sleep(sleep_time: Long): Long = {
    Thread.sleep(sleep_time)
    return sleep_time
  }

  def start() {
    bindingFuture = Http().bindAndHandle(
      userRoutes,
      host,
      port
    )
  }

  def stop() {
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}
