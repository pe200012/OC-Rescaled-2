package li.cil.oc.common.item.traits

import java.util

import li.cil.oc.Settings
import li.cil.oc.server.machine.riscv.CpuClock
import li.cil.oc.util.Tooltip
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

import scala.language.existentials

trait CPULike extends Delegate {
  def cpuTier: Int

  override protected def tooltipData: Seq[Any] = Seq(Settings.get.cpuComponentSupport(cpuTier))

  override protected def tooltipExtended(stack: ItemStack, tooltip: util.List[String]):Unit = {
    tooltip.addAll(Tooltip.get("cpu.Clock", Int.box(CpuClock.megahertz(stack))))
  }

  // Sneaking switches to the next clock rate.
  override def onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ActionResult[ItemStack] = {
    if (player.isSneaking) {
      if (!world.isRemote) {
        val megahertz = CpuClock.cycle(stack)
        player.sendMessage(new TextComponentTranslation(Settings.namespace + "tooltip.cpu.Clock", Int.box(megahertz)))
      }
      player.swingArm(EnumHand.MAIN_HAND)
    }
    ActionResult.newResult(EnumActionResult.SUCCESS, stack)
  }
}
