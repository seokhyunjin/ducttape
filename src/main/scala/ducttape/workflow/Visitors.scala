package ducttape.workflow

import collection._
import ducttape.exec.UnpackedDagVisitor
import ducttape.versioner.WorkflowVersionInfo
import ducttape.workflow.Types.UnpackedWorkVert
import ducttape.hyperdag.walker.Traversal
import ducttape.hyperdag.walker.Arbitrary
import grizzled.slf4j.Logging

object Visitors extends Logging {
  def visitAll[A <: UnpackedDagVisitor](
      workflow: HyperWorkflow,
      visitor: A,
      planPolicy: PlanPolicy,
      workflowVersion: WorkflowVersionInfo,
      numCores: Int = 1,
      traversal: Traversal = Arbitrary): A = {
    
    debug(s"Visiting workflow using traversal: ${traversal}")
    workflow.unpackedWalker(planPolicy, traversal=traversal).foreach(numCores, { v: UnpackedWorkVert =>
      val taskT: TaskTemplate = v.packed.value.get
      val task: VersionedTask = taskT.toRealTask(v).toVersionedTask(workflowVersion)
      debug(s"Visiting ${task}")
      visitor.visit(task)
    })
    visitor
  }
}
