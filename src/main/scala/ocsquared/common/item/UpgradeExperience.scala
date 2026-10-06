package ocsquared.common.item

import java.util

import ocsquared.util.UpgradeExperience
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import ocsquared.Localization;

class UpgradeExperience(val parent: Delegator) extends traits.Delegate with traits.ItemTier {

  @SideOnly(Side.CLIENT) override
  def tooltipLines(stack: ItemStack, world: World, tooltip: util.List[String], flag: ITooltipFlag): Unit = {
    if (stack.hasTagCompound) {
      val nbt = ocsquared.integration.opencomputers.Item.dataTag(stack)
      val experience = UpgradeExperience.getExperience(nbt)
      val level = UpgradeExperience.calculateLevelFromExperience(experience)
      val reportedLevel = UpgradeExperience.calculateExperienceLevel(level, experience)
      tooltip.add(Localization.Tooltip.ExperienceLevel(reportedLevel))
    }
    super.tooltipLines(stack, world, tooltip, flag)
  }
}
