package com.goticks


import akka.actor.Props
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }
import akka.actor.ReceiveTimeout
import com.goticks.blockrec.{Total, BlockReceiverActor}
import com.goticks.blockgen.{SimpleDataMonitor, TimeBoxedSeqDataGen}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext


object Main {
  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val system = ActorSystem("BlockGenActor", ConfigFactory.empty())

    val blockGenRcv = system.actorOf(Props[BlockReceiverActor], "blockRcv")
    val blockGenProps = Props(classOf[BlockGenActor], blockGenRcv)
    val blockGenActor = system.actorOf(blockGenProps, "blockGen")
    import ExecutionContext.Implicits.global
    val dataRcv = DataReceiverActorFactory.mkDataReceiver[Int](blockGenActor)
    val dataGens = (1 to 3).map(x=>new TimeBoxedSeqDataGen(dataRcv,100000))
    system.scheduler.schedule(500 millisecond , 200 milliseconds, blockGenActor, Tick(500))
    system.scheduler.schedule(5000 millisecond , 1000 milliseconds, blockGenRcv , Total(0))
    val monitor = new  SimpleDataMonitor(" Actor Gen ")
    dataGens.foreach( dataGen => {
      dataGen.addMonitor(monitor)
      dataGen.start()
    })
    println("Main is done")

  }

}
