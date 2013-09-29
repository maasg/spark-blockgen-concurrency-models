package com.goticks.blockgen

trait DataReceiver[T] {

  def receive(t:T):Unit

}
