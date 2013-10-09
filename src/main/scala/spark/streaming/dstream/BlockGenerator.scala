/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spark.streaming.dstream

import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.ArrayBlockingQueue
import com.goticks.blockrec.BlockReceiver
import com.goticks.blockgen.{DataMonitor, RecurringTimer, SystemClock, DataReceiver}

class BlockGenerator[T](receiver:BlockReceiver[T]) extends  DataReceiver[T] with Serializable {

    case class Block(id: String, buffer: ArrayBuffer[T])

    val clock = new SystemClock()
    val blockInterval = 200
    val blockIntervalTimer = new RecurringTimer(clock, blockInterval, updateCurrentBuffer(_))
    val reportingTimer = new RecurringTimer(clock, 1000, printQueueLength _ )
    val blocksForPushing = new ArrayBlockingQueue[Block](1000)
    val blockPushingThread = new Thread() { override def run() { keepPushingBlocks() } }

  def printQueueLength(time:Long) = println("Blocks for pushing: [%d]".format(blocksForPushing.size()))


    var currentBuffer = new ArrayBuffer[T]
    var monitors:List[DataMonitor] = Nil

    def start() {
      reportingTimer.start
      blockIntervalTimer.start()
      blockPushingThread.start()
      logInfo("Data handler started")
    }

    def logInfo(s:String) = println(s)

    def stop() {
      reportingTimer.stop
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
