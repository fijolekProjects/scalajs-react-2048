package webapp

import game2048.{Tiles, Board}
import game2048.Board.Directions
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.raw.{NodeList, KeyboardEvent}

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object Game2048Webapp extends JSApp {
  type Score = Int

  case class BoardScalaBackend($: BackendScope[Unit, (Score, Board)]) {
    def registerMoveRowByKeyEventHandler() = Callback {
      dom.window.onkeydown = (e: KeyboardEvent) => onKeyDownHandler(e).runNow()
    }

    private def onKeyDownHandler(e: KeyboardEvent): Callback = {
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
      val boardTemplate = board.rows.zip(Stream.from(1)).flatMap { case (row, rowIndex) =>
        val rowTemplate = row.tiles.zip(Stream.from(1)).flatMap { case (tile, colIndex) =>
          val baseTileParams = List(^.className := s"tile tile-${tile.value} tile-position-row-$rowIndex-col-$colIndex", ^.key := s"${tile.id}")
          val tileInner = <.div(^.className := "tile-inner", tile.value)
          tile match {
            case f: Tiles.NonEmptyTile if f.isNew     => Option(<.div(baseTileParams, ^.className := "new", tileInner))
            case f: Tiles.NonEmptyTile if f.isMerged  => Option(<.div(baseTileParams, ^.className := "merged", tileInner))
            case f: Tiles.NonEmptyTile                => Option(<.div(baseTileParams, tileInner))
            case Tiles.EmptyTile                      => None
          }
        }
        rowTemplate
      }
      val gridContainer = (1 to 4).map { _ => <.span((1 to 4).map { _ => <.div(^.className := "grid-cell")}) }

      <.div(
        <.p(^.className := "score", s"SCORE: $score"),
        <.div(^.className := "board",
          <.div(^.className := "grid-container", gridContainer),
          <.div(^.className := "tile-container", boardTemplate)
        )
      )
    }

    import org.scalajs.dom.ext._
    def removeMergeAndNewClasses() = Callback {
      org.scalajs.dom.setTimeout(() => {
        val node = ReactDOM.findDOMNode($)
        val nodeList = node.getElementsByClassName("merged") ++ node.getElementsByClassName("new")
        val elements = nodeList.map(_.cast[dom.Element])
        elements.foreach { e =>
          e.classList.remove("merged")
          e.classList.remove("new")
        }
      }, 250)

    }
  }

  val BoardScala = ReactComponentB[Unit]("BoardScala")
    .initialState((0, Board.zero.nextBoard.run.unsafePerformIO().nextBoard.run.unsafePerformIO()))
    .renderBackend[BoardScalaBackend]
    .componentDidMount(_.backend.registerMoveRowByKeyEventHandler())
    .componentDidUpdate(_.$.backend.removeMergeAndNewClasses())
    .buildU

  @JSExport
  override def main(): Unit = {
    ReactDOM.render(BoardScala(), document.getElementById("board-scala"))
  }

}
