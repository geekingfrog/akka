package akka_tickets.creation_error;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.util.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import akka.dispatch.Futures;
import akka.routing.RoundRobinRouter;
import akka.testkit.TestProbe;
import akka_tickets.creation_error.SimpleActor.READY;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TimeoutAssertCreate {

   private static final Config config = ConfigFactory.parseString("akka.loglevel = INFO \n" + "akka.actor.debug.lifecycle = on\n" + "unhandled = on\n"
         + "akka.event-handlers = [akka.testkit.TestEventListener]\n" + "akka.test.filter-leeway = 5000");
   private static ActorSystem system;
   private static final int nbrRoutee = 50;

   @BeforeClass
   public static void setup() {
      system = ActorSystem.create("testSystem", config);
   }

   @AfterClass
   public static void teardown() {
      system.shutdown();
   }

   /**
    * Sometimes this test shows on the stackTrace 
    * [ERROR] [08/10/2012 12:05:02.414] [testSystem-akka.actor.default-dispatcher-5] [akka://testSystem/user/router] exception during creation
    * 313ccb6e-d7c4-445b-9c43-8ce235fc2ac0akka.actor.ActorInitializationException: exception during creation
    * 
    * This does not happen everytime so one might have to run the test multiple time
    * The test case does not check the log errors so it will always pass, this is just a very basic sample.
    * 
    * @throws TimeoutException
    */
   @Test
   public void createActor() throws TimeoutException {
      // probe to check the ready messages
      final TestProbe probe = new TestProbe(system);

      // create the router with the actors
      Props props = new Props(new UntypedActorFactory() {

         public Actor create() {
            return new SimpleActor(probe.ref());
         }
      });
      ActorRef router = system.actorOf(props.withRouter(new RoundRobinRouter(nbrRoutee)), "router");

      Future<Integer> routeeCreated = Futures.future(new Callable<Integer>() {

         public Integer call() throws Exception {
            for(int i=0 ; i<nbrRoutee ; ++i)
               probe.expectMsgClass(READY.class);
            return 42;
         }
      }, system.dispatcher());

      
      //wait for the routee to be created
      Await.ready(routeeCreated, Duration.create(10, TimeUnit.SECONDS));
   }
}
