package webapp

import game2048.{Fields, Board}
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
          val boardWithNewField = newBoard.nextBoard.run.unsafePerformIO()
          (score + additionalScore, boardWithNewField)
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
        val rowTemplate = row.fields.zipWithIndex.map { case (field, colIndex) =>
          val baseFieldParams = List(^.className := s"field field-${field.value}", ^.key := colIndex)
          field match {
            case f: Fields.NonEmptyField if f.isNew => <.div(baseFieldParams, field.value, ^.className := "new")
            case f: Fields.NonEmptyField if f.isMerged => <.div(baseFieldParams, field.value, ^.className := "merged")
            case f: Fields.NonEmptyField            => <.div(baseFieldParams, field.value)
            case Fields.EmptyField                  => <.div(baseFieldParams)
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
