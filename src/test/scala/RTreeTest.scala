import li.cil.oc.util.RTree
import org.scalatest.funspec.AnyFunSpec

import scala.collection.mutable

class RTreeTest extends AnyFunSpec {
  describe("An R-tree") {
    it("should move and remove entries, as wireless cards in tablets do") {
      val positions = mutable.Map.empty[Int, (Double, Double, Double)]
      val tree = new RTree[Int](4)(using positions)
      for (i <- 0 until 50) {
        positions(i) = (i.toDouble, (i % 7).toDouble, (i % 3).toDouble)
        tree.add(i)
      }
      for (i <- 0 until 50 by 2) {
        positions(i) = (i + 100.0, 0.0, 0.0)
        assert(tree.remove(i))
        tree.add(i)
      }
      for (i <- 0 until 50 by 3) {
        assert(tree.remove(i))
      }
      val left = (0 until 50).filter(_ % 3 != 0)
      assert(tree.query((-1000, -1000, -1000), (1000, 1000, 1000)).toSet == left.toSet)
    }
  }
}
