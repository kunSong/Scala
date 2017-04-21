/**
  * Created by songkun on 2017/4/13.
  */
class Rocket {
  import Rocket._
  private def canGoHome = fuel > 20
}

object Rocket {
  private def fuel = 10
  def chooseStrategy(rocket: Rocket): Unit ={
    if(rocket.canGoHome){
      goHome()
    } else {
      pickAStar()
    }
  }
  def goHome() {}
  def pickAStar() {}
}
