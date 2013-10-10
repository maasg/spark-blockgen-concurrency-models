package com.goticks

import com.goticks.blockgen.DataReceiver
import akka.actor.ActorRef

object DataReceiverActorFactory {
  def mkDataReceiver[T](rcvActor: ActorRef):DataReceiver[T] =  {
    new DataReceiver[T] {
      def receive(t: T): Unit = rcvActor ! DataPoint(t)
    }
  }


}
