import scala.collection.mutable.ArrayBuffer

/**
  * Created by songkun on 2017/4/11.
  */

object TestClient extends App {
  val queue1 = new BasicIntQueue with Incrementing with Doubling
  queue1.put(2)
  println(queue1.get())

  val queue2 = new BasicIntQueue with Doubling with Incrementing
  queue2.put(2)
  println(queue2.get())
}

abstract class IntQueue {
  def get(): Int
  def put(x: Int)
}

class BasicIntQueue extends IntQueue {
  private val buf = new ArrayBuffer[Int]

  override def get(): Int = buf.remove(0)
  override def put(x: Int) { buf += x }
}

trait Doubling extends IntQueue {
  abstract override def put(x: Int) = { super.put(2 * x) }
}

trait Filtering extends IntQueue {
  abstract override def put(x: Int) = { if(x > 0) super.put(x) }
}

trait Incrementing extends IntQueue {
  abstract override def put(x: Int) = { super.put(x + 1) }
}



