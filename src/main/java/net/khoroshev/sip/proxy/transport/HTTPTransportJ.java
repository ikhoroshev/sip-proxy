package net.khoroshev.sip.proxy.transport;

import akka.NotUsed;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.typesafe.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class HTTPTransportJ extends AbstractActor implements Transport{
    private final List<ActorRef> nextActors;

    private HTTPTransportJ(List<ActorRef> nextActors) {
        this.nextActors = nextActors;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
        .matchEquals(NewChanellReq.class, r -> {
            //TODO
            System.out.println(r);
        }).build();
    }

    public static ActorRef getInstance(ActorSystem system, Config conf) {
        HTTPTransportJ instance;
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are define.
        App app = new App(system, conf);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(conf.getString("http.bindAddress"), conf.getInt("http.bindPort")), materializer);

        return system.actorOf(Props.create(HTTPTransportJ.class, Arrays.asList(system.actorOf(Props.create(Echo.class)))), conf.getString("name")) ;
    }
    private static class App extends AllDirectives {
        private final ActorSystem system;
        private final Config conf;

        private App(ActorSystem system, Config conf) {
            this.system = system;
            this.conf = conf;
        }

        public Route createRoute() {
            return route(
                path("hello", () ->
                        get(() ->
                                complete("<h1>Say hello to akka-http</h1>"))),
                path("sip", () -> this.<NotUsed>handleWebSocketMessagesForOptionalProtocol(sipFlow(), Optional.of("sip"))),

                /*entity(as[HttpRequest]) { requestData =>
                complete {
                    val fullPath = requestData.uri.path.toString match {
                        case "/"=> getDefaultPage
                        case "" => getDefaultPage
                        case _ => requestData.uri.path.toString
                    }
                    val content = readFile(fullPath)
                    HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, content))
                }*/
                getFromResourceDirectory("web")
            );
        }

        private Flow<Message, Message, NotUsed> sipFlow() {
            ActorRef client = system.actorOf(Props.create(Echo.class));//(Props(classOf[Echo]));
            Sink<Message, NotUsed> in = Sink.actorRef(client, "sinkclose");
            Source<Message, ActorRef> actorSource = Source.actorRef(8, OverflowStrategy.fail());
            Source<Message, ActorRef> out = actorSource.mapMaterializedValue((a) -> {
                client.tell(TextMessage.create("income"), a);
                return a;
            });
            return Flow.fromSinkAndSource(in, out);
        }
    }
}
