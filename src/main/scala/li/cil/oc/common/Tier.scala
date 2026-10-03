package li.cil.oc.common

object Tier {
  final val None = -1
  final val One = 0
  final val Two = 1
  final val Three = 2
  final val Four = 3
  final val Five = 4
  final val Six = 5
  final val Any = Int.MaxValue

  /**
   * Whether an item of a tier goes into a slot of a tier. Hardware is not tiered anymore, so any
   * slot there is takes items of any tier.
   */
  def fits(itemTier: Int, slotTier: Int): Boolean = slotTier != None
}
