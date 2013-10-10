package com.goticks.blockrec

import scala.collection.mutable.ArrayBuffer
import akka.actor.{ActorLogging, Actor}

case class Block[T](blockId: String, data: ArrayBuffer[T])
trait Stats
case class  Max(value:Int) extends Stats
case class  Min(value:Int) extends Stats
case class  Total(value:Int) extends Stats


class BlockReceiverActor extends Actor with ActorLogging {

    var totalBlocks = 0
    var totalRecords = 0
    var currentCount = 0
    val blockRecord = new ArrayBuffer[Int]()
    def now() = System.currentTimeMillis()
    var last = now()

  def receive = {
    case Block(id,data) => {

      totalBlocks += 1
      val len = data.length
      totalRecords += len
      blockRecord += len
    }
    case Max(_) => println("Max throughput:" + max)
    case Min(_) => sender ! Min(min)
    case Total(_) => {
      val recs = totalRecords-currentCount
      println("records:" + recs + " records/ms:" + (recs*1.0/(now()-last)))
      last= now()
      currentCount = totalRecords
    }
    case _ => println(" Are you nuts?")
  }

    def blocks = totalBlocks

    def max = blockRecord.max
    def min = blockRecord.min

}
