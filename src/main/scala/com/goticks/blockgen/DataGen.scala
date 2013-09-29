package com.goticks.blockgen

import scala.collection.mutable.ArrayBuffer
import scala.annotation.tailrec
import scala.concurrent.SyncVar
import scala.concurrent._
import ExecutionContext.Implicits.global


abstract class DataGen(rcv:DataReceiver[Int]) extends Monitorable {
  val active = {val v = new SyncVar[Boolean]; v.put(false); v}
  var monitors:List[DataMonitor] = Nil
  var total:Long = 0

  def start():Unit = {
    total = 0
    active.take()
    active.put(true)
    val res = future{gen()}
  }

  def addMonitor(mon:DataMonitor) : Unit = monitors = mon::monitors
  def removeMonitor(mon: DataMonitor) :Unit = monitors = monitors.filterNot(_ ==  mon)
  def report(records:Int, time:Long) = monitors.foreach(mon=> mon.report(records, time))

  @tailrec private def gen():Unit = {
    val (records,timed) = time {generate(rcv)}
    total += records
    report(records, (timed/1e6).toLong)
    if (active.get) gen()
  }

  def totalRecords = total

  def stop():Unit = {
    active.take()
    active.put(false)
  }

  def time[T](b: => T):(T, Long) = {
    val t0 = System.nanoTime()
    val res = b
    val t = System.nanoTime() - t0
    (res,t )
  }

  def generate(rcv:DataReceiver[Int]):Int

}

/**
 *
 */
class TimeBoxedSeqDataGen(rcv:DataReceiver[Int], dps:Int) extends DataGen(rcv) {

  val blockCount = aproxSquareDiv(dps)
  val blockUnit = dps/blockCount

  def delay(ns:Long) = ((1000/blockCount) -(ns.toDouble/1e6)).toInt

  def generate(rcv:DataReceiver[Int]) : Int = {
    (0 until blockCount).foreach(c => {
      val (_,dt) = time((0 until blockUnit).foreach(u => rcv.receive(c*blockCount+u)))
      val del = delay(dt)
      if (del>0) {
        Thread.sleep(del)
      }
    })
    dps
  }

  def aproxSquareDiv(n:Int):Int = {
    val sqrt = math.sqrt(n).toInt
    @tailrec def aprox(i:Int):Int = {
      if (n%i==0) i else aprox(i-1)
    }
    aprox(sqrt)
  }


}

class UnboundedDataGen(rcv:DataReceiver[Int], blockSize: Int) extends DataGen(rcv) {
  var index = 0
  def next():Int = if (index<Int.MaxValue) {index+=1;index} else {index=0;index}
  def generate(rcv:DataReceiver[Int]):Int =  {
    (0 until blockSize).foreach( x=> rcv.receive(next()))
    blockSize
  }
}

class TimedDataGen(rcv:DataReceiver[Int], ms: Int) extends DataGen(rcv) {
  var index = 0
  def next():Int = if (index<Int.MaxValue) {index+=1;index} else {index=0;index}
  def generate(rcv:DataReceiver[Int]):Int =  {
    @tailrec
    def generateUntil(t:Long, count:Int):Int = {
      rcv.receive(next())
      if (System.currentTimeMillis()<t) generateUntil(t,count+1) else count
    }
    generateUntil(System.currentTimeMillis()+ms,0)
  }
}




class CountingDataRecv extends DataReceiver[Int] {
  var total = 0;
  def receive(i:Int){ total += 1 }
  def count = total
}