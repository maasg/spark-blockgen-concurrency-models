package com.goticks.blockgen

import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.ArrayBlockingQueue
import com.goticks.blockrec.BlockReceiver

class BlockGenerator[T](receiver:BlockReceiver[T]) extends  DataReceiver[T] with Serializable {

    case class Block(id: String, buffer: ArrayBuffer[T])

    val clock = new SystemClock()
    val blockInterval = 200
    val blockIntervalTimer = new RecurringTimer(clock, blockInterval, updateCurrentBuffer(_))
    val blocksForPushing = new ArrayBlockingQueue[Block](1000)
    val blockPushingThread = new Thread() { override def run() { keepPushingBlocks() } }

    var currentBuffer = new ArrayBuffer[T]
    var monitors:List[DataMonitor] = Nil

    def start() {
      blockIntervalTimer.start()
      blockPushingThread.start()
      logInfo("Data handler started")
    }

    def logInfo(s:String) = println(s)

    def stop() {
      blockIntervalTimer.stop()
      blockPushingThread.interrupt()
      logInfo("Data handler stopped")
    }

    def += (obj: T) {
      // currentBuffer.synchronized{
        currentBuffer += obj
      // }
    }

    def receive(t:T) = this += t

    private def updateCurrentBuffer(time: Long) {
      try {
        val newBlockBuffer = currentBuffer
        currentBuffer = new ArrayBuffer[T]
        if (newBlockBuffer.size > 0) {
          val blockId = "input-" + (time - blockInterval)
          val newBlock = new Block(blockId, newBlockBuffer)
          blocksForPushing.add(newBlock)
        }
      } catch {
        case ie: InterruptedException =>
          logInfo("Block interval timer thread interrupted")
        case e: Exception =>
          logInfo("failed. Reason:" + e)
      }
    }

    private def keepPushingBlocks() {
      logInfo("Block pushing thread started")
      try {
        while(true) {
          val block = blocksForPushing.take()
          receiver.pushBlock(block.id, block.buffer)
        }
      } catch {
        case ie: InterruptedException =>
          logInfo("Block pushing thread interrupted")
        case e: Exception =>
          logInfo("Block pushing failed: "+e)
      }
    }

}
