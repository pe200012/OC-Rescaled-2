package ocsquared.common.block

import java.util

import ocsquared.common.tileentity
import ocsquared.integration.Mods
import ocsquared.util.Tooltip
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class Redstone extends RedstoneAware {
  override protected def tooltipTail(metadata: Int, stack: ItemStack, world: World, tooltip: util.List[String], advanced: ITooltipFlag):Unit = {
    super.tooltipTail(metadata, stack, world, tooltip, advanced)
    // todo more generic way for redstone mods to provide lines
    if (Mods.ProjectRedTransmission.isModAvailable) {
      tooltip.addAll(Tooltip.get("RedstoneCard.ProjectRed"))
    }
    if (Mods.Charset.isModAvailable) {
      tooltip.addAll(Tooltip.get("RedstoneCard.Charset"))
    }
  }

  // ----------------------------------------------------------------------- //

  override def createNewTileEntity(world: World, metadata: Int) = new tileentity.Redstone()
}
