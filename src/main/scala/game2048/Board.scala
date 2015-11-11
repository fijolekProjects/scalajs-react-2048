package game2048

import com.nicta.rng.Rng
import game2048.Board.{Index, AdditionalScore}
import game2048.Fields.{EmptyField, NonEmptyField}

import scalaz.NonEmptyList

object Board {
  type Index = Int
  type AdditionalScore = Int

  private val rows = 4
  private val cols = 4
  val zero = Board((1 to rows).toList.map(_ => Row(List.fill(cols)(EmptyField))))

  sealed trait Direction
  object Directions {
    case object Left extends Direction
    case object Right extends Direction
    case object Up extends Direction
    case object Down extends Direction
    case object None extends Direction
  }
}

case class Board(rows: List[Row]) {
  import Board._

  def nextBoard: Rng[Board] = {
    val newFieldValue = Rng.chooseint(0, 10).map { n => if (n > 2) 2 else 4 }
    for {
      rc <- emptyFieldIndices
      (r, c) = rc
      newField <- newFieldValue
    } yield updateAt(r, c)(NonEmptyField(newField)(isNew = true))
  }

  def move(d: Direction): (AdditionalScore, Board) = d match {
    case Directions.Left => moveLeft
    case Directions.Right => moveRight
    case Directions.Up => moveUp
    case Directions.Down => moveDown
    case Directions.None => (0, this)
  }

  private def emptyFieldIndices: Rng[(Index, Index)] = {
    val nel = allEmptyIndices match {
      case h :: t => NonEmptyList.nel(h, t)
      case _ => throw new RuntimeException("you lost")
    }
    Rng.oneofL(nel)
  }

  private def allEmptyIndices: List[(Index, Index)] = for {
    (r, rowIndex) <- rows.zipWithIndex
    (f, columnIndex) <- r.fields.zipWithIndex
    if f == EmptyField
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

  private def transpose: Board = Board(rows.map(_.fields).transpose.map(Row.apply))

  private def updateAt(row: Index, column: Index)(f: Field): Board = {
    this.copy(rows = rows.updated(row, Row(rows(row).fields.updated(column, f))))
  }
}

sealed trait Field {
  def value: Int
  def isNew: Boolean
  def asOld: Field = this match {
    case NonEmptyField(v) => NonEmptyField(v)(isNew = false)
    case EmptyField => EmptyField
  }
  def isMerged: Boolean
}

object Fields {
  case class NonEmptyField(override val value: Int)(override val isNew: Boolean = false, override val isMerged: Boolean = false) extends Field
  case object EmptyField extends Field {
    override def value: Int = 0
    override def isNew: Boolean = false
    override def isMerged: Boolean = false
  }
}

case class Row(fields: List[Field]) {

  private val fieldsCount = fields.size

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
    val nonEmptyFields = fields.filterNot(_ == EmptyField)
    Row(complementWithEmptyFields(nonEmptyFields))
  }

  private def complementWithEmptyFields(nonEmptyFields: List[Field]): List[Field] = {
    nonEmptyFields ++ List.fill(fieldsCount - nonEmptyFields.size)(EmptyField)
  }

  private def reverse = this.copy(fields = this.fields.reverse)

  private def mergeAt(i: Index): (AdditionalScore, Row) = {
    val (newScore, neighbours) = merge(this.fields(i), this.fields(i + 1))
    (newScore, Row(fields.take(i) ++ neighbours.toList ++ fields.drop(i + 2)))
  }

  private def merge(previousField: Field, currentField: Field): (AdditionalScore, (Field, Field)) = {
    val twiceCurrentField = 2 * currentField.value
    (previousField, currentField) match {
      case (a: NonEmptyField, b: NonEmptyField) if a == b => (twiceCurrentField, (NonEmptyField(twiceCurrentField)(isMerged = true), EmptyField))
      case _                                              => (0,                 (previousField.asOld, currentField.asOld))
    }
  }

  implicit class RichTuple[A](t: (A, A)) {
    def toList = List(t._1, t._2)
  }
}