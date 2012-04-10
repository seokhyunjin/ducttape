package ducttape.syntax

import java.io.File
import scala.util.parsing.input.Position
import ducttape.syntax.AbstractSyntaxTree.ASTType
import scala.collection.LinearSeq

/**
 * Each element of ref has (file, line, col, untilLine)
 */
class FileFormatException(val msg: String, val refs: Seq[(File, Int, Int, Int)]) extends Exception(msg) {
  
  def this(msg: String, file: File, line: Int, col: Int) = this(msg, List( (file, line, col, line) ))
  
  def this(msg: String, file: File, pos: Position) = this(msg, List( (file, pos.line, pos.column, pos.line) ))
  
  // require iterable instead of Seq to get around erasure
  def this(msg: String, refs: Iterable[(File, Position)]) = this(msg, (for( (f,p) <- refs) yield (f, p.line, p.column, p.line)).toList )
  
    // require list instead of Seq to get around erasure
  def this(msg: String, refs: List[(File, Position, Int)]) = this(msg, for( (f,p,until) <- refs) yield (f, p.line, p.column, until) )
  
  // require LinearSeq instead of Seq to get around erasure
  def this(msg: String, refs: LinearSeq[ASTType]) = this(msg, for(t <- refs) yield (t.declaringFile, t.pos.line, t.pos.column, t.pos.line))
  
  def this(msg: String, ref: ASTType) = this(msg, LinearSeq(ref))
}
