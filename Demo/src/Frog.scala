/**
  * Created by songkun on 2017/4/11.
  */
class Animal {

}

class Frog extends Animal with Philosophical with HasLegs {
  override def toString = "green"
}

trait HasLegs {

}
