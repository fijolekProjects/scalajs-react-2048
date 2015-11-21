package webapp

import java.util.Date

import game2048.Board.GameStatesAfterMove._
import game2048.Board.{AdditionalScore, Direction, Directions, Index}
import game2048.Tiles.NonEmptyTile
import game2048.{Board, Tile, Tiles}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.ext.{KeyCode, _}
import org.scalajs.dom.raw.KeyboardEvent

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object Game2048Webapp extends JSApp {
  object CssClasses {
    val mergedClass = "merged"
    val newClass = "new"
  }

  case class GameBoardState(board: Board, additionalScore: AdditionalScore, newGame: Boolean = false, isGameOver: Boolean = false)

  case class BoardBackend($: BackendScope[Unit, GameBoardState]) {
    def registerMoveBoardEventHandler() = Callback {
      dom.window.onkeydown = (e: KeyboardEvent) => onKeyDownHandler(e).runNow()
    }

    private def onKeyDownHandler(e: KeyboardEvent): Callback = {
      readDirection(e).map { dir =>
        $.modState { boardState =>
          moveBoard(dir, boardState.board)
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

    private def moveBoard(dir: Direction, board: Board): GameBoardState = board.moveAndCreateNewTile(dir) match {
      case BoardChanged(additionalScore, changedBoard) => GameBoardState(changedBoard.run.unsafePerformIO(), additionalScore)
      case NothingChanged                              => GameBoardState(board, 0)
      case GameOver(additionalScore, lastBoardState)   => GameBoardState(lastBoardState, additionalScore, isGameOver = true)
    }

    val gridContainer = (1 to Board.rows).map { _ => <.span((1 to Board.cols).map { _ => <.div(^.className := "grid-cell")}) }

    def render(boardState: GameBoardState) = {
      val boardTemplate = createBoardTemplate(boardState.board)
      val boardKey = if (boardState.newGame) Some(^.key := new Date().getTime) else None
      val gameOverMessage = if (boardState.isGameOver) List(<.div(^.className := "game-message game-over", <.p("Game over!"))) else Nil
      <.div(boardKey,
        scoreBoard(boardState.additionalScore),
        <.div(<.a(^.className := "restart-button", "New Game"), ^.onClick --> restartBoard),
        <.div(^.className := "board",
          gameOverMessage,
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
      val baseTileParams = List(^.className := s"tile tile-${tile.value} tile-position-col-$colIndex-row-$rowIndex", ^.key := s"${tile.id}")
      val tileInner = <.div(^.className := "tile-inner", tile.value)
      tile match {
        case f: NonEmptyTile if f.isNew     => Option(<.div(baseTileParams, ^.className := CssClasses.newClass, tileInner))
        case f: NonEmptyTile if f.isMerged  => Option(<.div(baseTileParams, ^.className := CssClasses.mergedClass, tileInner))
        case f: NonEmptyTile                => Option(<.div(baseTileParams, tileInner))
        case Tiles.EmptyTile                => None
      }
    }

    private def restartBoard() = $.setState($.getInitialState(()).copy(newGame = true))

    def removeMergeAndNewClasses() = Callback {
      org.scalajs.dom.setTimeout(() => {
        val node = ReactDOM.findDOMNode($)
        val nodeList = node.getElementsByClassName(CssClasses.mergedClass) ++ node.getElementsByClassName(CssClasses.newClass)
        val elements = nodeList.map(_.cast[dom.Element])
        elements.foreach { e =>
          e.classList.remove(CssClasses.mergedClass)
          e.classList.remove(CssClasses.newClass)
        }
      }, 250)
    }
  }

  val GameBoard = ReactComponentB[Unit]("GameBoard")
    .initialState(GameBoardState(Board.createBoardStartPosition.run.unsafePerformIO(), 0))
    .renderBackend[BoardBackend]
    .componentDidMount(_.backend.registerMoveBoardEventHandler())
    .componentDidUpdate(_.$.backend.removeMergeAndNewClasses())
    .buildU

  type Score = Int
  case class ScoreBackend($: BackendScope[Board.AdditionalScore, Score]) {
    def render(additionalScore: Board.AdditionalScore) = {
      val currentScore = $.state.runNow()
      val scoreContainer = <.div(^.className := "score-container", ^.key := new Date().getTime, s"SCORE: $currentScore") /*fixme component recreating by timestamp as key is lame*/
      if (additionalScore > 0) scoreContainer(<.div(^.className := "score-addition", s"+$additionalScore"))
      else                     scoreContainer(<.div(^.className := "score-addition"))
    }

    def incrementScore(additionalScore: Board.AdditionalScore) = $.modState(currentScore => currentScore + additionalScore)
  }

  val scoreBoard = ReactComponentB[Board.AdditionalScore]("Score")
    .initialState(0)
    .renderBackend[ScoreBackend]
    .componentWillReceiveProps { self => self.$.backend.incrementScore(self.nextProps) }
    .build

  @JSExport
  override def main(): Unit = {
    ReactDOM.render(GameBoard(), document.getElementById("game-board"))
  }

}
