package game2048

import com.nicta.rng.Rng
import game2048.Board.{Index, AdditionalScore}
import game2048.Tiles.{EmptyTile, NonEmptyTile}

import scalaz.NonEmptyList

object Board {
  type Index = Int
  type AdditionalScore = Int

  val rows = 4
  val cols = 4
  private val zero = Board((1 to rows).toList.map(_ => Row(List.fill(cols)(EmptyTile))))

  sealed trait Direction
  object Directions {
    case object Left extends Direction
    case object Right extends Direction
    case object Up extends Direction
    case object Down extends Direction
    case object None extends Direction
  }

  sealed trait GameStateAfterMove
  object GameStatesAfterMove{
    case class BoardChanged(additionalScore: AdditionalScore, board: Rng[Board]) extends GameStateAfterMove
    case object NothingChanged extends GameStateAfterMove
    /*fixme case object GameOver*/
  }

  def createBoardStartPosition: Rng[Board] = Board.zero.nextBoard.flatMap(_.nextBoard)
}

case class Board(rows: List[Row]) {
  import Board._

  def nextBoard: Rng[Board] = {
    val newTileValueRng = Rng.chooseint(0, 10).map { n => if (n > 2) 2 else 4 }
    for {
      rc <- emptyTileIndices
      (r, c) = rc
      newTile <- newTileValueRng
    } yield updateAt(r, c)(NonEmptyTile(newTile)(isNew = true, id = Tiles.incrAndGetCounter))
  }

  def moveAndCreateNewTile(d: Direction): GameStateAfterMove = {
    val (additionalScore, boardAfterMove) = move(d)
    if (boardAfterMove == this) GameStatesAfterMove.NothingChanged
    else                        GameStatesAfterMove.BoardChanged(additionalScore, boardAfterMove.nextBoard)
  }

  def move(d: Direction): (AdditionalScore, Board) = d match {
    case Directions.Left => moveLeft
    case Directions.Right => moveRight
    case Directions.Up => moveUp
    case Directions.Down => moveDown
    case Directions.None => (0, this)
  }

  private def emptyTileIndices: Rng[(Index, Index)] = {
    val nel = allEmptyIndices match {
      case h :: t => NonEmptyList.nel(h, t)
      case _ => throw new RuntimeException("you lost")
    }
    Rng.oneofL(nel)
  }

  private def allEmptyIndices: List[(Index, Index)] = for {
    (r, rowIndex) <- rows.zipWithIndex
    (f, columnIndex) <- r.tiles.zipWithIndex
    if f == EmptyTile
  } yield (rowIndex, columnIndex)

  private def moveLeft: (AdditionalScore, Board) = rowsToBoard(rows.map(_.shiftLeft))
  private def moveRight: (AdditionalScore, Board) = rowsToBoard(rows.map(_.shiftRight))
  private def moveUp: (AdditionalScore, Board) = {
    val (newScore, leftTransposed) = transpose.moveLeft
    (newScore, leftTransposed.transpose)
  }

  private def moveDown: (AdditionalScore, Board) = {
    val (newScore, rightTransposed) = transpose.moveRight
    (newScore, rightTransposed.transpose)
  }

  private def rowsToBoard(shiftedRows: List[(AdditionalScore, Row)]): (AdditionalScore, Board) = {
    val (score, movedRows) = shiftedRows.unzip
    (score.sum, Board(movedRows))
  }

  private def transpose: Board = Board(rows.map(_.tiles).transpose.map(Row.apply))

  private def updateAt(row: Index, column: Index)(f: Tile): Board = {
    this.copy(rows = rows.updated(row, Row(rows(row).tiles.updated(column, f))))
  }
}

sealed trait Tile {
  def value: Int
  def isNew: Boolean
  def asOld: Tile = this match {
    case tile: NonEmptyTile => NonEmptyTile(tile.value)(isNew = false, id=tile.id)
    case EmptyTile => EmptyTile
  }
  def isMerged: Boolean
  def id: Int
}

object Tiles {
  var counter = 0 /*shame on me*/
  def incrAndGetCounter = {
    counter += 1 
    counter
  }

  case class NonEmptyTile(override val value: Int)
                         (override val isNew: Boolean = false, override val isMerged: Boolean = false, override val id: Int) extends Tile
  case object EmptyTile extends Tile {
    override def value: Int = 0
    override def isNew: Boolean = false
    override def isMerged: Boolean = false
    override def id: Int = -1
  }
}

case class Row(tiles: List[Tile]) {

  private val tilesCount = tiles.size

  def shiftLeft: (AdditionalScore, Row) = {
    val (firstScore, firstRow) = this.slideLeft.mergeAt(0)
    val (scndScore, scndRow) = firstRow.mergeAt(1)
    val (thrdScore, thrdRow) = scndRow.mergeAt(2)
    (firstScore + scndScore + thrdScore, thrdRow.slideLeft)
  }

  def shiftRight: (AdditionalScore, Row) = {
    val (newScore, row) = this.reverse.shiftLeft
    (newScore, row.reverse)
  }

  private def slideLeft: Row = {
    val nonEmptyTiles = tiles.filterNot(_ == EmptyTile)
    Row(complementWithEmptyTiles(nonEmptyTiles))
  }

  private def complementWithEmptyTiles(nonEmptyTiles: List[Tile]): List[Tile] = {
    nonEmptyTiles ++ List.fill(tilesCount - nonEmptyTiles.size)(EmptyTile)
  }

  private def reverse = this.copy(tiles = this.tiles.reverse)

  private def mergeAt(i: Index): (AdditionalScore, Row) = {
    val (newScore, neighbours) = merge(this.tiles(i), this.tiles(i + 1))
    (newScore, Row(tiles.take(i) ++ neighbours.toList ++ tiles.drop(i + 2)))
  }

  private def merge(tile: Tile, neighbour: Tile): (AdditionalScore, (Tile, Tile)) = {
    val twiceTileValue = 2 * tile.value
    (tile, neighbour) match {
      case (a: NonEmptyTile, b: NonEmptyTile) if a == b => (twiceTileValue, (NonEmptyTile(twiceTileValue)(isMerged = true, id = a.id), EmptyTile))
      case _                                            => (0,              (tile.asOld, neighbour.asOld))
    }
  }

  implicit class RichTuple[A](t: (A, A)) {
    def toList = List(t._1, t._2)
  }
}