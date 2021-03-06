// This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.

package ducttape.graph

import scala.collection.mutable.HashMap

import ducttape.syntax.AbstractSyntaxTree._

/**
 *
 *
 * @author Lane Schwartz
 * @author Jonathan Clark
 */
abstract sealed class Vertex(val id:String, val comment: Option[String] = None) {

  val contents:Any

  if (id==null) throw new NullPointerException("Value id was initialized as null. This is forbidden.")

  private[graph] val incomingEdges = new HashMap[String, Edge]
  private[graph] val outgoingEdges = new HashMap[String, Edge]

  def foreachEdge[U](f: Edge => U) = (incomingEdges.values ++ outgoingEdges.values).foreach(f)

  def foreachParent[U](f: Vertex => U) = incomingEdges.values.map({ edge => edge.from }).foreach(f)
  def foreachChild [U](f: Vertex => U) = outgoingEdges.values.map({ edge => edge.to   }).foreach(f)

  override def hashCode() = id.hashCode()

  /** Because we are operating on a graph, equality between vertices is defined as reference equality. */
  override def equals(other: Any) : Boolean = { //that match { case other: Vertex => (other.id.equals(this.id)) }
    other match {
      case that:AnyRef => this.eq(that)
      case _ => false
    }
  }

  override def toString() = comment match {
    case Some(str) => "%s:%s".format(str, id)
    case None => "ID=%s".format(id)
  }

}



case class RootVertex() extends Vertex(id="global root vertex", comment=None) {
  val contents=None
}


//class ConfigVertex(val contents:ConfigDefinition)
//  extends Vertex(
//    id=contents.name match {
//      case Some(s) => contents.keyword + " " + s
//      case None    => contents.keyword
//    },
//    contents.comments.value
//  )

class GoalVertex(taskName:String, val contents:CrossProduct) extends Vertex(id=taskName) {
  override def toString = taskName
}

class LiteralVertex(val contents:Literal) extends Vertex(id=contents.value, comment=None)

sealed trait ParamVertex
class ConfigParamVertex(contents : ConfigParamSpec[RValue]) extends SpecVertex(contents) with ParamVertex

abstract sealed class GroupLikeVertex[Contents <: GroupLike](val contents:Contents) extends Vertex(id=contents.name.toString(), comment=contents.comments.value)
class SummaryVertex(contents: SummaryDef) extends GroupLikeVertex(contents)

abstract sealed class TaskLikeVertex[Contents <: TaskLike](val contents:Contents) extends Vertex(id=contents.name.toString(), comment=contents.comments.value)
class PackageVertex(contents:PackageDef) extends TaskLikeVertex(contents)
class SummaryOfVertex(contents:SummaryOfDef) extends TaskLikeVertex(contents) {
  override def toString() = contents.name.toString()
}
class TaskVertex(contents:TaskDef) extends TaskLikeVertex(contents) {
  override def toString() = contents.name.toString()
}

class ConfigDefinitionVertex(val contents:ConfigDefinition) extends Vertex(id=ConfigDefinition.getName(contents), contents.comments.value)

abstract sealed class SpecVertex(val contents:Spec,comment:Option[String]=None) extends Vertex(id=contents.name,comment)

class BranchSpecVertex(contents:BranchSpec[RValue], comment: Option[String]=None) extends SpecVertex(contents, comment)
class PackageSpecVertex(contents:PackageSpec, comment: Option[String]=None) extends SpecVertex(contents, comment)

abstract sealed class TaskSpecVertex(contents:Spec,comment:Option[String]=None) extends SpecVertex(contents, comment) {

  def whereToLookForTask() : Iterable[Vertex]

	def task() : Option[TaskLikeVertex[_]] = whereToLookForTask.collectFirst({ case vertex:TaskLikeVertex[_] => vertex})

  override def toString() : String = {
    task() match {
      case Some(task) => "$%s@%s".format(contents.name, task.id)
      case None       => throw new RuntimeException("No task found for %s: $%s".format(this.getClass.getSimpleName, contents.name))
    }
  }
}
class TaskInputVertex (contents:Spec, comment: Option[String]=None) extends TaskSpecVertex(contents, comment) {
  override def whereToLookForTask = outgoingEdges.values.map({ edge => edge.to })
}
class TaskOutputVertex(contents:Spec, comment: Option[String]=None) extends TaskSpecVertex(contents, comment) {
  override def whereToLookForTask = incomingEdges.values.map({ edge => edge.from })
}
class TaskParamVertex (contents:Spec, comment: Option[String]=None) extends TaskSpecVertex(contents, comment) with ParamVertex {
  override def whereToLookForTask = outgoingEdges.values.map({ edge => edge.to })
}

class BranchPointDefVertex(val contents:BranchPointDef) extends Vertex(id=BranchPointDef.getName(contents))
class SequentialBranchPointVertex(val contents:SequentialBranchPoint) extends Vertex(id=SequentialBranchPoint.getName(contents))

//class BranchVertex(val contents:Spec) extends Vertex(id=contents.name)

sealed abstract class VariableReferenceVertex(id:String) extends Vertex(id) {
  def toString() : String
}
//{
//
//  def task() : Option[TaskVertex] = {
//
//    val childVertices = outgoingEdges.values.map({ edge => edge.to })
//
//    return childVertices.collectFirst({ case vertex:TaskVertex => vertex})
//  }
//
//  override def toString() : String = {
//
//    this.task() match {
//      case Some(taskVertex) => return "%s___%s".format(id, taskVertex.id)
//      case None             => return id+"_______"
//    }
//
//  }
//}

//{
//  val variableName: String
//  val taskName: Option[String]
//  val branchGraftElements: Seq[BranchGraftElement]
//}

class TaskVariableVertex(val contents:TaskVariable) extends VariableReferenceVertex(id=contents.toString()) {
  override def toString() : String = contents.toString()
}
//{
//  val variableName = contents.value
//  val taskName = Some(contents.taskName)
//  val branchGraftElements = Seq[BranchGraftElement]()
//}

class ShorthandTaskVariableVertex(val contents:ShorthandTaskVariable) extends VariableReferenceVertex(id=contents.toString()) {
  override def toString = ???
}
//{
//  override def toString() : String = {
//    contents.toString()
//  }
//}
//extends VariableReferenceVertex(id="$%s@%s".format(variableName,contents.taskName)) {
//  val taskName = Some(contents.taskName)
//  val branchGraftElements = Seq[BranchGraftElement]()
//}

class ConfigVariableVertex(val contents:ConfigVariable) extends VariableReferenceVertex(id=contents.toString()) {
  override def toString = ???
}

class ShorthandConfigVariableVertex(val contents:ShorthandConfigVariable) extends VariableReferenceVertex(id=contents.toString()) {
  override def toString = ???
}

//extends VariableReferenceVertex(id="$%s".format(variableName)) {
//  val taskName = None
//  val branchGraftElements = Seq[BranchGraftElement]()
//}

class BranchGraftVertex(val contents:BranchGraft) extends VariableReferenceVertex(id=contents.toString()) {
  override def toString() = contents.toString(withBranchGraftElements=false)
}
//{
//  val variableName = contents.variableName
//  val taskName = contents.taskName
//  val branchGraftElements = contents.branchGraftElements
//}

class ShorthandBranchGraftVertex(val contents:ShorthandBranchGraft) extends VariableReferenceVertex(id=contents.toString()) {
  override def toString = ???
}

//extends VariableReferenceVertex(id="$%s@%s%s".format(variableName,contents.taskName,contents.branchGraftElements.toString())) {
//  val taskName = Some(contents.taskName)
//  val branchGraftElements = contents.branchGraftElements
//}

class PlanDefinitionVertex(val contents:PlanDefinition) extends Vertex(id=contents.toString())
