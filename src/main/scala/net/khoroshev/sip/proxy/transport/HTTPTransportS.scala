package net.khoroshev.sip.proxy.transport

/**
  * Created by sbt-khoroshev-iv on 30/05/17.
  */
import java.io.InputStream

import akka.actor.{ActorSystem, ExtensionId, Props}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.{Config, ConfigFactory}
import net.khoroshev.sip.proxy.deprecated.SipMessageActor
import akka.actor.{ActorSystem, Props}
import akka.http.javadsl.Http
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives.{entity, _}

import scala.io.StdIn

object HTTPTransportS {

  def readFile(name: String): String = {
    val stream: InputStream = getClass.getResourceAsStream(name)
    return scala.io.Source.fromInputStream(stream).mkString
  }

/*  def getDefaultPage: String = {
    return "index.html"
  }*/


  /*def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    def flow: Flow[Message, Message, Any] = {
      val client = system.actorOf(Props(classOf[Echo]))
      val in = Sink.actorRef(client, 'sinkclose)
      val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { a ⇒
        client ! ('income → a)
        a
      }
      Flow.fromSinkAndSource(in, out)
    }

    def sipFlow: Flow[Message, Message, Any] = {
      val client = system.actorOf(Props(classOf[SipMessageActor], "sas"))
      val in = Sink.actorRef(client, 'sinkclose)
      val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { a ⇒
        client ! ('income → a)
        a
      }
      Flow.fromSinkAndSource(in, out)
    }

    val route =
      logRequestResult("my-micro-http-server") {
        get {
          path("form") {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
              readFile("index.html")
            ))
          } ~
            path("ws") {
              handleWebSocketMessages(flow)
            } ~
            path("sip") {
              handleWebSocketMessagesForOptionalProtocol(sipFlow, Some("sip"))
            } ~
            entity(as[HttpRequest]) { requestData =>
              complete {
                val fullPath = requestData.uri.path.toString match {
                  case "/" => getDefaultPage
                  case "" => getDefaultPage
                  case _ => requestData.uri.path.toString
                }
                val content = readFile(fullPath)
                HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, content))
              }
            }
        }
      }


    val config = ConfigFactory.load()
    val host = config.getString("system.transport.websocket.http.bindAddress")
    val port = config.getInt("system.transport.websocket.http.bindPort")
    val bindingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }*/
}

class HTTPTransportS(implicit system: ActorSystem, conf: Config){
  //implicit val system = system
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  def flow: Flow[Message, Message, Any] = {
    val client = system.actorOf(Props(classOf[Echo]))
    val in = Sink.actorRef(client, 'sinkclose)
    val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { a ⇒
      client ! ('income → a)
      a
    }
    Flow.fromSinkAndSource(in, out)
  }


  def getDefaultPage: String = {
    return "index.html"
  }

  def sipFlow: Flow[Message, Message, Any] = {
    val client = system.actorOf(Props(classOf[SipMessageActor], "sas"))
    val in = Sink.actorRef(client, 'sinkclose)
    val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { a ⇒
      client ! ('income → a)
      a
    }
    Flow.fromSinkAndSource(in, out)
  }
  /*val route: Flow[HttpRequest, HttpResponse, Any] =
    logRequestResult("my-micro-http-server") {
      get {
        path("form") {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
            readFile("index.html")
          ))
        } ~
          path("ws") {
            handleWebSocketMessages(flow)
          } ~
          path("sip") {
            handleWebSocketMessagesForOptionalProtocol(sipFlow, Some("sip"))
          } ~
          entity(as[HttpRequest]) { requestData =>
            complete {
              val fullPath = requestData.uri.path.toString match {
                case "/" => getDefaultPage
                case "" => getDefaultPage
                case _ => requestData.uri.path.toString
              }
              val content = readFile(fullPath)
              HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, content))
            }
          }
      }
    }*/
/*  val host = conf.getString("system.transport.websocket.http.bindAddress")
  val port = conf.getInt("system.transport.websocket.http.bindPort")

  val http: HttpExt = Http(system)

  val route: _root_.akka.stream.scaladsl.Flow[_root_.akka.http.scaladsl.model.HttpRequest, _root_.akka.http.scaladsl.model.HttpResponse, Any]
    = logRequestResult("my-micro-http-server"){
    get {
      path("form") {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
          HTTPTransportS.readFile("index.html")
        ))
      }
  }

  //source.
  val bindingFuture = http.bindAndHandle(route, host, port)*/

}