package com.goticks.blockgen

/**
 *
 */
trait Monitorable {

  def addMonitor(mon:DataMonitor) : Unit
  def removeMonitor(mon: DataMonitor) :Unit

}
