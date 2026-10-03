package li.cil.oc.common.item

import li.cil.oc.Settings
import net.minecraft.item.ItemStack

class Memory(val parent: Delegator, val tier: Int) extends traits.Delegate with traits.ItemTier {
  override val unlocalizedName = super.unlocalizedName + tier

  override protected def tooltipName = Option(super.unlocalizedName)

  // RAM comes in sizes rather than tiers, so names say which, like hard drives do.
  override def displayName(stack: ItemStack): Some[String] = {
    val sizes = Settings.get.ramSizes
    val kiloBytes = sizes(tier max 0 min (sizes.length - 1))
    val localizedName = parent.internalGetItemStackDisplayName(stack)
    Some(if (kiloBytes >= 1024) {
      localizedName + s" (${kiloBytes / 1024}MB)"
    }
    else {
      localizedName + s" (${kiloBytes}KB)"
    })
  }
}
