package game2048

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, FlatSpec}

class RowTest extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  def intToField(i: Int): Field = {
    if (i == 0) Fields.EmptyField
    else Fields.NonEmptyField(i)()
  }
  implicit def listOfIntToListOfField(ints: List[Int]): List[Field] = {
    ints.map(intToField)
  }

  it should "shift row to the left and right" in {
    val rows = Table(
      ("before shifting", "after right shift", "after left shift"),
      (List(0, 0, 0, 0), List(0, 0, 0, 0), List(0, 0, 0, 0)),
      (List(0, 0, 2, 0), List(0, 0, 0, 2), List(2, 0, 0, 0)),
      (List(0, 0, 2, 2), List(0, 0, 0, 4), List(4, 0, 0, 0)),
      (List(2, 2, 2, 2), List(0, 0, 4, 4), List(4, 4, 0, 0)),
      (List(0, 2, 2, 2), List(0, 0, 2, 4), List(4, 2, 0, 0)),
      (List(2, 0, 0, 2), List(0, 0, 0, 4), List(4, 0, 0, 0)),
      (List(0, 2, 2, 0), List(0, 0, 0, 4), List(4, 0, 0, 0)),
      (List(4, 2, 2, 0), List(0, 0, 4, 4), List(4, 4, 0, 0)),
      (List(4, 0, 4, 2), List(0, 0, 8, 2), List(8, 2, 0, 0))
    )
    forAll(rows) { (beforeShift, expectedAfterRightShift, expectedAfterLeftShift) =>
      Row(beforeShift).shiftRight._2.fields.map(_.value) shouldBe Row(expectedAfterRightShift).fields.map(_.value)
      Row(beforeShift).shiftLeft._2.fields.map(_.value) shouldBe Row(expectedAfterLeftShift).fields.map(_.value)
    }
  }
}
