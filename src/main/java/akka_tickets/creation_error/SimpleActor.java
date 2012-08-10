package akka_tickets.creation_error;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class SimpleActor extends UntypedActor {

   private final ActorRef master;

   public SimpleActor(ActorRef master) {
      this.master = master;
   }

   @Override
   public void preStart() {
      master.tell(new READY(), getSelf());
   }

   @Override
   public void onReceive(Object arg0) throws Exception {
   }

   public static class READY {
   }
}
