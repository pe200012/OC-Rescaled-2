package ocsquared.common.block.traits

import java.util

import ocsquared.common.block.SimpleBlock
import ocsquared.util.Tooltip
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World

trait PowerAcceptor extends SimpleBlock {
  def energyThroughput: Double

  // ----------------------------------------------------------------------- //

  override protected def tooltipTail(metadata: Int, stack: ItemStack, world: World, tooltip: util.List[String], advanced: ITooltipFlag): Unit = {
    super.tooltipTail(metadata, stack, world, tooltip, advanced)
    tooltip.addAll(Tooltip.extended("poweracceptor", energyThroughput.toInt))
  }
}
