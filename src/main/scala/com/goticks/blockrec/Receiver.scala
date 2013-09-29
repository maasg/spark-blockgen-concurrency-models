package com.goticks.blockrec

import scala.collection.mutable.ArrayBuffer

trait BlockReceiver[T] {

  def pushBlock(blockId: String, arrayBuffer: ArrayBuffer[T])

}

class BlockCounter[T] extends BlockReceiver[T] {

  var totalBlocks = 0
  var totalRecords = 0
  val t0 = now
  val blockRecord = new ArrayBuffer[Int]()
  def now = System.currentTimeMillis()

  override def pushBlock(blockId: String, arrayBuffer: ArrayBuffer[T]) {
    totalBlocks += 1
    val len = arrayBuffer.length
    totalRecords += len
    blockRecord += len
  }
  def blocks = totalBlocks
  def recordsPerSecond = totalRecords*1d/((now-t0)/1000)
  def max = blockRecord.max
  def min = blockRecord.min



}
