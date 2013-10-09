package com.goticks


import akka.actor.Props
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }
import akka.actor.ReceiveTimeout
import com.goticks.blockrec.BlockReceiverActor


object Main extends App {
  val config = ConfigFactory.load()
  val system = ActorSystem("BlockGenActor", ConfigFactory.empty())
  val blockGenRcv = system.actorOf(Props[BlockReceiverActor], "blockRcv")
  val blockGenProps = Props(classOf[BlockGenActor], blockGenRcv)
  val blockGenActor = system.actorOf(, "blockGen")
  blockGenActor ! DataPoint(5)

}
