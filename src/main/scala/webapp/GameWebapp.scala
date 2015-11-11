package webapp

import game2048.{Tiles, Board}
import game2048.Board.Directions
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.KeyboardEvent

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object GameWebapp extends JSApp {
  type Score = Int

  case class BoardScalaBackend($: BackendScope[Unit, (Score, Board)]) {
    def moveRowKeyEventHandler = Callback {
      dom.window.onkeydown = (e: KeyboardEvent) => onKeyDownHandler(e).runNow()
    }

    private def onKeyDownHandler(e: KeyboardEvent) = {
      val direction = readDirection(e)
      direction.map { dir =>
        $.modState { case (score, board) =>
          val (additionalScore, newBoard) = board.move(dir)
          val boardWithNewTile = newBoard.nextBoard.run.unsafePerformIO()
          (score + additionalScore, boardWithNewTile)
        }
      }.getOrElse(Callback.empty)
    }

    private def readDirection(e: KeyboardEvent): Option[Board.Direction] = e.keyCode match {
      case (KeyCode.A | KeyCode.Left)   => Option(Directions.Left)
      case (KeyCode.D | KeyCode.Right)  => Option(Directions.Right)
      case (KeyCode.W | KeyCode.Up)     => Option(Directions.Up)
      case (KeyCode.S | KeyCode.Down)   => Option(Directions.Down)
      case _                            => None
    }

    def render(scoreBoard: (Score, Board)) = {
      val (score, board) = scoreBoard
      val boardTemplate = board.rows.zipWithIndex.map { case (row, rowIndex) =>
        val rowTemplate = row.tiles.zipWithIndex.map { case (tile, colIndex) =>
          val baseTileParams = List(^.className := s"tile tile-${tile.value}", ^.key := colIndex)
          tile match {
            case f: Tiles.NonEmptyTile if f.isNew     => <.div(baseTileParams, tile.value, ^.className := "new")
            case f: Tiles.NonEmptyTile if f.isMerged  => <.div(baseTileParams, tile.value, ^.className := "merged")
            case f: Tiles.NonEmptyTile                => <.div(baseTileParams, tile.value)
            case Tiles.EmptyTile                      => <.div(baseTileParams)
          }
        }
        <.span(rowTemplate)
      }
      <.div(
        <.p(^.className := "score", s"SCORE: $score"),
        <.div(^.className := "board", boardTemplate)
      )
    }
  }

  val BoardScala = ReactComponentB[Unit]("BoardScala")
    .initialState((0, Board.zero.nextBoard.run.unsafePerformIO().nextBoard.run.unsafePerformIO()))
    .renderBackend[BoardScalaBackend]
    .componentDidMount(_.backend.moveRowKeyEventHandler)
    .buildU

  @JSExport
  override def main(): Unit = {
    ReactDOM.render(BoardScala(), document.getElementById("boardScala"))
  }

}
