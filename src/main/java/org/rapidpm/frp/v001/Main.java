package org.rapidpm.frp.v001;

import java.time.LocalDateTime;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;

/**
 *
 */
public class Main {

  public static void main(String[] args) throws InterruptedException {


//    final Flow.Publisher<String> publisher = new SubmissionPublisher<>();
    //TODO how to write own Publisher ?
    final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();


    publisher.subscribe(new Flow.Subscriber<>() {
      private Flow.Subscription subscription;
      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        System.out.println("onSubscribe:subscription = " + subscription);
        System.out.flush();
        this.subscription = subscription;
        subscription.request(1);
      }

      @Override
      public void onNext(String item) {
        System.out.println("onNext:item = " + item);
        System.out.flush();
        subscription.request(1);
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("onError:throwable = " + throwable);
        System.out.flush();
      }

      @Override
      public void onComplete() {
        System.out.println("onComplete = " + LocalDateTime.now());
        System.out.flush();
        subscription.cancel();
      }
    });

    IntStream
        .range(0 , 10)
        .mapToObj(String::valueOf)
        .forEachOrdered(publisher::submit);

    System.out.println("published all the numbers");
    Thread.sleep(100 + publisher.estimateMaximumLag());


  }


}
