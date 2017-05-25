import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import akka.testkit.TestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import scala.concurrent.duration.Duration;

import static akka.testkit.JavaTestKit.duration;
import static org.junit.gen5.api.Assertions.assertEquals;

public class TestKitSampleTests {

    public static class SomeActor extends AbstractActor {
        ActorRef target = null;

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals("hello", message -> {
                        getSender().tell("world", getSelf());
                        if (target != null) target.forward(message, getContext());
                    })
                    .match(ActorRef.class, actorRef -> {
                        target = actorRef;
                        getSender().tell("done", getSelf());
                    })
                    .build();
        }
    }

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system, Duration.apply(10, "seconds"), false);
        system = null;
    }

    @Test
    public void testIt() {
    /*
     * Wrap the whole test procedure within a testkit constructor 
     * if you want to receive actor replies or use Within(), etc.
     */
        new TestKit(system) {{
            final Props props = Props.create(SomeActor.class);
            final ActorRef subject = system.actorOf(props);

            // can also use JavaTestKit “from the outside”
            final TestKit probe = new TestKit(system);
            // “inject” the probe by passing it to the test subject
            // like a real resource would be passed in production
            //subject.tell(probe.getRef(), getRef());
            // await the correct response
            expectMsg(duration("1 second"), "done");

            // the run() method needs to finish within 3 seconds
            within(duration("3 seconds"), () -> {
                //subject.tell("hello", getRef());

                // This is a demo: would normally use expectMsgEquals().
                // Wait time is bounded by 3-second deadline above.
                //awaitCond(probe::msgAvailable);

                // response must have been enqueued to us before probe
                expectMsg(Duration.Zero(), "world");
                // check that the probe we injected earlier got the msg
                probe.expectMsg(Duration.Zero(), "hello");
                //assertEquals(getRef(), probe.getLastSender());

                // Will wait for the rest of the 3 seconds
                expectNoMsg();
                return null;
            });
        }};
    }

}