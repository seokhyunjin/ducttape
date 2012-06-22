package ducttape.hyperdag

import collection._
import ducttape.viz._
import ducttape.hyperdag.walker.VertexFilter
import ducttape.hyperdag.walker.DefaultVertexFilter
import ducttape.hyperdag.walker.DefaultToD
import ducttape.hyperdag.walker.RealizationMunger
import ducttape.hyperdag.walker.DefaultRealizationMunger

// immutable
class HyperDag[V,H,E](val roots: Seq[PackedVertex[V]],
                      val vertices: Seq[PackedVertex[V]],
                      private[hyperdag] val inEdgesMap: Map[PackedVertex[_], Seq[HyperEdge[H,E]]],
                      private[hyperdag] val outEdgesMap: Map[PackedVertex[_], Seq[HyperEdge[H,E]]],
                      private[hyperdag] val edges: Map[HyperEdge[H,E], (Seq[PackedVertex[V]],PackedVertex[V])]) {
  
  val size: Int = vertices.size

  def packedWalker() = new walker.PackedDagWalker[V](this)

  def unpackedWalker[D,F](munger: RealizationMunger[V,H,E,D,F],
                          vertexFilter: VertexFilter[V,H,E,D],
                          toD: H => D)
                         (implicit ordering: Ordering[D])
    = new walker.UnpackedDagWalker[V,H,E,D,F](this, munger, vertexFilter, toD)
    
  def unpackedWalker[D](vertexFilter: VertexFilter[V,H,E,D] = new DefaultVertexFilter[V,H,E,D],
                        toD: H => D = new DefaultToD[H])
                       (implicit ordering: Ordering[D]) = {
    val munger = new DefaultRealizationMunger[V,H,E,D]
    new walker.UnpackedDagWalker[V,H,E,D,immutable.HashSet[D]](this, munger, vertexFilter, toD)
  }
    
  def inEdges(v: PackedVertex[_]): Seq[HyperEdge[H,E]]
    = inEdgesMap.getOrElse(v, Seq.empty)
  def outEdges(v: PackedVertex[_]): Seq[HyperEdge[H,E]]
    = outEdgesMap.getOrElse(v, Seq.empty)
  def parents(v: PackedVertex[_]): Seq[PackedVertex[V]]
    = for (e <- inEdges(v); src <- sources(e)) yield src
  def children(v: PackedVertex[_]): Seq[PackedVertex[V]]
    = for (e <- outEdges(v)) yield sink(e)
  def sources(e: HyperEdge[H,E]): Seq[PackedVertex[V]]
    = edges(e)._1
  def sink(e: HyperEdge[H,E]): PackedVertex[V]
    = edges(e)._2

  def toGraphViz(): String = toGraphViz(vertices, inEdges, { v => v.toString }) 

  def toGraphViz(vertexList: Seq[PackedVertex[V]],
                 inEdgesFunc: PackedVertex[V] => Seq[HyperEdge[H,E]],
                 stringifyV: PackedVertex[V] => String): String = {
      
    // TODO: Expose these
    // XXX: HACK
    def stringifyH(h: H): String = if (h == null) "" else h.toString
    def stringifyE(e: E): String = if (e == null) {
      ""
    } else if(e.isInstanceOf[Seq[_]]) {
      val seq = e.asInstanceOf[Seq[_]]
      seq.mkString("\n")
    } else {
      e.toString
    }
    def colorizeV(v: PackedVertex[V]): String = {
      if (v.toString.startsWith("Phantom")) {
        "grey"
      } else if(v.toString.startsWith("Epsilon")) {
        "cornsilk"
      } else {
        "white"
      }  
    }
      
    val str = new StringBuilder(1000)
    str ++= "digraph G {\n"
    for (v: PackedVertex[V] <- vertexList) {
      val color = colorizeV(v)
      str ++= "\"%s\" [fillcolor=\"%s\",style=\"filled\"]\n".format(GraphViz.escape(stringifyV(v)), color)
      for (he: HyperEdge[H,E] <- inEdgesFunc(v)) {
        for ( (ant, e) <- sources(he).zip(he.e)) {
          str ++= "\"%s\" -> \"%s\" [label=\"%s\"]\n".format(
                  GraphViz.escape(stringifyV(ant)),
                  GraphViz.escape(stringifyV(v)),
                  GraphViz.escape(Seq(stringifyH(he.h),
                                      stringifyE(e)).mkString("\n")))
        }
      }
    }
    str ++= "}\n"
    str.toString
  }
}
