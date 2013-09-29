package com.goticks.blockgen

trait DataMonitor {

  def report(records: Int, ms: Long):Unit

}

class SimpleDataMonitor(source:String) extends DataMonitor {
  var totalRecords:Long = 0
  var totalTime:Long = 0

  def reset =  {totalRecords = 0; totalTime=0 }

  def report(records:Int, time:Long) = this.synchronized {
    totalRecords += records
    totalTime += time
    println("%s reports: records=%d, time=%d, rec/ms=%.2f".format(source, records, time, records.toDouble/time))
  }
  def totals = println("%s Totals: records=%d, time=%d, rec/ms=%.2f".format(source, totalRecords, totalTime, totalRecords.toDouble/totalTime))
}
