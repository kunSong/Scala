/**
  * Created by songkun on 2017/3/10.
  */
class Person {
  private val age: Int = 0

  def testPerson(other: Person) {
    println(other.age)
  }

}

object Test extends App {

  val person: Person = new Person
  val person1: Person =  new Person
  
  person.testPerson(person1)

  val array1 = Array("F", "U", "C", "K")
  val array2 = Array("f", "u", "c", "k")

  def main(args: String*): Unit = {
    //for(arg <- args)
    //  println(arg)
  }

  println(divid(2))

  def error(message: String): Nothing =
    throw new RuntimeException(message)

  def divid(x: Int): Int = {
    if (x != 0)
      x / 2
    else
      error("can not divide 0")
  }

  //val funValue = nested _
  //def nested(x: Int) =
  //  if(x != 0) { println(x); funValue(3: Int)}

  //main(array1: _*)
  //args: Array[String] = Array("F", "U", "C", "K")
  //main("a", "b")
}
