package webapp

import game2048.Board.GameStatesAfterMove._
import game2048.Board.{Index, Direction, Directions}
import game2048.Tiles.NonEmptyTile
import game2048.{Board, Tile, Tiles}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import org.scalajs.dom.ext.{KeyCode, _}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.KeyboardEvent
import org.scalajs.dom.{document, html}

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object Game2048Webapp extends JSApp {
  type Score = Int

  case class BoardScalaBackend($: BackendScope[Unit, (Score, Board)]) {
    def registerMoveBoardEventHandler() = Callback {
      dom.window.onkeydown = (e: KeyboardEvent) => onKeyDownHandler(e).runNow()
    }

    private def onKeyDownHandler(e: KeyboardEvent): Callback = {
      readDirection(e).map { dir =>
        $.modState { case (score, board) => moveBoard(dir, score, board) }
      }.getOrElse(Callback.empty)
    }

    private def readDirection(e: KeyboardEvent): Option[Board.Direction] = e.keyCode match {
      case (KeyCode.A | KeyCode.Left)   => Option(Directions.Left)
      case (KeyCode.D | KeyCode.Right)  => Option(Directions.Right)
      case (KeyCode.W | KeyCode.Up)     => Option(Directions.Up)
      case (KeyCode.S | KeyCode.Down)   => Option(Directions.Down)
      case _                            => None
    }

    private def moveBoard(dir: Direction, score: Score, board: Board): (Score, Board) = board.moveAndCreateNewTile(dir) match {
      case BoardChanged(additionalScore, changedBoard) => (score + additionalScore, changedBoard.run.unsafePerformIO())
      case NothingChanged                              => (score + 0,               board)
    }

    val gridContainer = (1 to Board.rows).map { _ => <.span((1 to Board.cols).map { _ => <.div(^.className := "grid-cell")}) }
    def render(scoreBoard: (Score, Board)) = {
      val (score, board) = scoreBoard
      val boardTemplate = createBoardTemplate(board)
      <.div(
        <.p(^.className := "score", s"SCORE: $score"),
        <.div(^.className := "board",
          <.div(^.className := "grid-container", gridContainer),
          <.div(^.className := "tile-container", boardTemplate)
        )
      )
    }

    private def createBoardTemplate(board: Board): List[ReactTagOf[dom.Element]] = {
      for {
        (row, rowIndex)   <- board.rows.zip(Stream.from(1))
        (tile, colIndex)  <- row.tiles.zip(Stream.from(1))
      } yield createTile(tile, rowIndex, colIndex)
    }.flatten

    private def createTile(tile: Tile, rowIndex: Index, colIndex: Index): Option[ReactTagOf[dom.Element]] = {
      val baseTileParams = List(^.className := s"tile tile-${tile.value} tile-position-row-$rowIndex-col-$colIndex", ^.key := s"${tile.id}")
      val tileInner = <.div(^.className := "tile-inner", tile.value)
      tile match {
        case f: NonEmptyTile if f.isNew     => Option(<.div(baseTileParams, ^.className := "new", tileInner))
        case f: NonEmptyTile if f.isMerged  => Option(<.div(baseTileParams, ^.className := "merged", tileInner))
        case f: NonEmptyTile                => Option(<.div(baseTileParams, tileInner))
        case Tiles.EmptyTile                => None
      }
    }

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

  val GameBoard = ReactComponentB[Unit]("GameBoard")
    .initialState((0, Board.createBoardStartPosition.run.unsafePerformIO()))
    .renderBackend[BoardScalaBackend]
    .componentDidMount(_.backend.registerMoveBoardEventHandler())
    .componentDidUpdate(_.$.backend.removeMergeAndNewClasses())
    .buildU

  @JSExport
  override def main(): Unit = {
    ReactDOM.render(GameBoard(), document.getElementById("game-board"))
  }

}
