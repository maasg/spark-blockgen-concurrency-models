package com.goticks

import akka.actor.{ActorRef, ActorLogging, Actor}
import scala.collection.mutable.ArrayBuffer
import com.goticks.blockrec.Block

case class DataPoint(data:Any)
case class Tick(time:Long)
import scala.concurrent.duration._



class BlockGenActor(recv:ActorRef) extends Actor with ActorLogging {
  var currentBuffer = new ArrayBuffer[Any]

  override def preStart(): Unit = {

  }

  def receive = {
    case DataPoint(data) => currentBuffer += data
    case Tick(time) => deliverData(time)
    case _ => println("what?")
  }

  def deliverData(time:Long) = {
    //switch buckets - thread safe with the actor boundaries
    val dispatchBuffer = currentBuffer
    currentBuffer = new ArrayBuffer[Any]
    recv ! Block("block_"+time,dispatchBuffer)
  }



}
