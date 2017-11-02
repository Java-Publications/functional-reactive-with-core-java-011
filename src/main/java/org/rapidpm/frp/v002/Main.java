package org.rapidpm.frp.v002;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 *
 */
public class Main {


  public static class StepA<A, B> extends SubmissionPublisher<B> implements Flow.Processor<A, B> {

    private Flow.Subscription subscription;
    private Function<A, B> transformation;

    public StepA(Function<A, B> transformation) {
      this.transformation = transformation;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      this.subscription = subscription;
      subscription.request(1);
    }

    @Override
    public void onNext(A item) {
      //consume the next item
      final B value = transformation.apply(item);
      //distribute new Value
      submit(value);
      //after all is done.. request one more
      subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
      //push exception to separate channel -> external logging ?

    }

    @Override
    public void onComplete() {
      close();
    }
  }


  public static class IntegerSubscriber implements Flow.Subscriber<Integer> {

    private Consumer<Integer> consumer = System.out::println;
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
      this.subscription = subscription;
      subscription.request(1);
    }

    @Override
    public void onNext(Integer item) {
      consumer.accept(item);
      subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
      System.out.println("throwable = " + throwable);
      //demo
    }

    @Override
    public void onComplete() {

    }

  }

  public static void main(String[] args) throws InterruptedException {

    final StepA<String, Integer> stepA = new StepA<>(Integer::parseInt);

    stepA.subscribe(new IntegerSubscriber());


    final SubmissionPublisher<String> dataSourcePublisher = new SubmissionPublisher<>();
    dataSourcePublisher.subscribe(stepA);

    IntStream
        .range(0 , 10)
        .mapToObj(String::valueOf)
        .forEachOrdered(dataSourcePublisher::submit);


    System.out.println("published all the numbers");
    Thread.sleep(1000 + dataSourcePublisher.estimateMaximumLag());

  }
}
