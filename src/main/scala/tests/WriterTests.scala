package tests

import scalaz._
import Scalaz._
import scalaz.effect.IO

object WriterTests {

  def main (args: Array[String]){
    val multRest = for {
      a <- logNumber(3)
      b <- logNumber(5)
      c <- logNumber(7)
    } yield a * b * c

    val prg = for {
      _ <- IO.putStrLn(multRest.run.toString())
    } yield ()
    prg.unsafePerformIO()
  }


  def logNumber(x: Int): Writer[List[Int], Int] = {
    x.set(List(x))
  }
}
