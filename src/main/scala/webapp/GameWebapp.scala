package webapp

import game2048.Board
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

  case class BoardScalaBackend($: BackendScope[Unit, (Score, Board, (Int, Int))]) {
    def moveRowKeyEventHandler = Callback {
      dom.window.onkeydown = (e: KeyboardEvent) => onKeyDownHandler(e).runNow()
    }

    private def onKeyDownHandler(e: KeyboardEvent) = {
      val direction = readDirection(e)
      direction.map { dir =>
        $.modState { case (score, board, _) =>
          val (additionalScore, newBoard) = board.move(dir)
          val (newFieldIndex, boardWithNewField) = newBoard.nextBoard.run.unsafePerformIO()
          (score + additionalScore, boardWithNewField, newFieldIndex)
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

    def render(scoreBoard: (Score, Board, (Int, Int))) = {
      val (score, board, newFieldIndex) = scoreBoard
      val boardTemplate = board.rows.zipWithIndex.map { case (row, rowIndex) =>
        val rowTemplate = row.fields.zipWithIndex.map { case (field, colIndex) =>
          val baseFieldParams = List(^.className := s"field field-$field", ^.key := colIndex)
          if (field != 0 && (rowIndex, colIndex) == newFieldIndex) {
            <.div(baseFieldParams, field, ^.className := "new")
          } else if (field != 0) {
            <.div(baseFieldParams, field)
          }
          else {
            <.div(baseFieldParams)
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
    .initialState( {
      val (newFieldIndex, board) = Board.zero.nextBoard.run.unsafePerformIO()
      (0, board, newFieldIndex)
    })
    .renderBackend[BoardScalaBackend]
    .componentDidMount(_.backend.moveRowKeyEventHandler)
    .buildU

  @JSExport
  override def main(): Unit = {
    ReactDOM.render(BoardScala(), document.getElementById("boardScala"))
  }

}
