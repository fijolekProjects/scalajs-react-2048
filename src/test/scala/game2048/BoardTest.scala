package game2048

import game2048.Board.Directions
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}


class BoardTest extends FlatSpec with Matchers with TableDrivenPropertyChecks{
  import scala.language.implicitConversions
  implicit def intToField(i: Int): Tile = {
    if (i == 0) Tiles.EmptyTile
    else Tiles.NonEmptyTile(i)(id = 0)
  }

  it should "move board in different directions" in {
    val rowsBeforeMove = List(
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 2, 0)),
      Row(List(0, 0, 2, 2)),
      Row(List(2, 2, 2, 2))
    )
    val rowsAfterLeftMove = List(
      Row(List(0, 0, 0, 0)),
      Row(List(2, 0, 0, 0)),
      Row(List(4, 0, 0, 0)),
      Row(List(4, 4, 0, 0))
    )
    val rowsAfterRightMove = List(
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 0, 2)),
      Row(List(0, 0, 0, 4)),
      Row(List(0, 0, 4, 4))
    )
    val rowsAfterUpMove = List(
      Row(List(2, 2, 4, 4)),
      Row(List(0, 0, 2, 0)),
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 0, 0))
    )
    val rowsAfterMoveDown = List(
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 2, 0)),
      Row(List(2, 2, 4, 4))
    )

    Board(rowsBeforeMove).move(Directions.Left)._2 shouldBe Board(rowsAfterLeftMove)
    Board(rowsBeforeMove).move(Directions.Right)._2 shouldBe Board(rowsAfterRightMove)
    Board(rowsBeforeMove).move(Directions.Up)._2 shouldBe Board(rowsAfterUpMove)
    Board(rowsBeforeMove).move(Directions.Down)._2 shouldBe Board(rowsAfterMoveDown)

    val anotherRowsBeforeMove = List(
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 2, 0)),
      Row(List(0, 0, 2, 0))
    )

    val anoptherRowsAfterUpMove = List(
      Row(List(0, 0, 4, 0)),
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 0, 0)),
      Row(List(0, 0, 0, 0))
    )

    Board(anotherRowsBeforeMove).move(Directions.Up)._2 shouldBe Board(anoptherRowsAfterUpMove)
  }

}
