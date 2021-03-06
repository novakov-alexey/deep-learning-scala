package ml.preprocessing

import ml.transformation.{castTo, castArray}
import ml.tensors.api._
import ml.tensors.ops.{T, col, slice}

import java.io.File
import java.nio.file.Path
import scala.io.Source
import scala.reflect.ClassTag
import scala.util.Using

object TextLoader:
  val defaultDelimiter: String = ","

  def apply(rows: String*): TextLoader =
    TextLoader(data = rows.toArray.map(_.split(defaultDelimiter).toArray))

case class TextLoader(
    path: Path = new File("data.csv").toPath,
    header: Boolean = true,
    delimiter: String = TextLoader.defaultDelimiter,
    data: Array[Array[String]] = Array.empty[Array[String]]
):

  def load(): TextLoader = copy(
    data = Using.resource(Source.fromFile(path.toFile)) { s =>
      val lines = s.getLines()
      (if header && lines.nonEmpty then lines.toArray.tail else lines.toArray)
        .map(_.split(delimiter))
    }
  )

  def cols[T: ClassTag](from: Int, to: Int): Tensor2D[T] =
    castTo[T](data.slice(None, Some((from, to))))

  def col[T: ClassTag](i: Int): Tensor1D[T] =
    val col = data.col(i)
    Tensor1D(castArray[T](col))

  def cols[T: ClassTag](i: Int): Tensor[T] = col(i).T