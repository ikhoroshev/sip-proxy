package net.khoroshev.sip.proxy.deprecated;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.MaterializationContext;
import akka.stream.Materializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import scala.collection.Iterable;
import scala.collection.immutable.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by sbt-khoroshev-iv on 01/06/17.
 */
public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ActorSystem system = ActorSystem.create("test");
        Materializer mat = ActorMaterializer.create(system);

        final Source<Integer, NotUsed> source =
                Source.from(IntStream.range(1, 10000000).boxed().collect(Collectors.toList()));
        // note that the Future is scala.concurrent.Future
        /*final Sink<Integer, CompletionStage<Integer>> sink =
                Sink.<Integer, Integer> fold(0, (aggr, next) -> aggr + next);*/
        final Sink<Integer, CompletionStage<List<Double>>> sink = Sink.fold(new ArrayList<Double>(), (list, i) -> {list.add(Math.sqrt(i)); return list;});

        // connect the Source to the Sink, obtaining a RunnableFlow
        final RunnableGraph<CompletionStage<List<Double>>> runnable =
                source.toMat(sink, Keep.right());

        // materialize the flow
        final CompletionStage<List<Double>> r = runnable.run(mat);
        List<Double> result = r.toCompletableFuture().get();
        System.out.println(result.size());
    }
}
