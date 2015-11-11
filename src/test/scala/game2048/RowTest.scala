package game2048

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, FlatSpec}

class RowTest extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  def intToField(i: Int): Tile = {
    if (i == 0) Tiles.EmptyTile
    else Tiles.NonEmptyTile(i)()
  }
  implicit def listOfIntToListOfField(ints: List[Int]): List[Tile] = {
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
      Row(beforeShift).shiftRight._2.tiles.map(_.value) shouldBe Row(expectedAfterRightShift).tiles.map(_.value)
      Row(beforeShift).shiftLeft._2.tiles.map(_.value) shouldBe Row(expectedAfterLeftShift).tiles.map(_.value)
    }
  }
}
