package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.Environment
import ocsquared.common
import ocsquared.common.tileentity
import ocsquared.integration.util.BundledRedstone
import ocsquared.server.component
import ocsquared.server.machine.Machine
import net.minecraft.block.Block
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack

/**
 * Provide static environment lookup for blocks that are components.
 * This allows showing their documentation in NEI, for example. Not
 * all blocks are present here, because some also serve as upgrades
 * and therefore have item drivers.
 */
object EnvironmentProviderBlocks extends EnvironmentProvider {
  override def getEnvironment(stack: ItemStack): Class[?] = stack.getItem match {
    case block: ItemBlock if block.getBlock != null =>
      if (isOneOf(block.getBlock, Constants.BlockName.Assembler)) classOf[tileentity.Assembler]
      else if (isOneOf(block.getBlock, Constants.BlockName.CaseTier1, Constants.BlockName.CaseTier2, Constants.BlockName.CaseTier3, Constants.BlockName.CaseCreative, Constants.BlockName.Microcontroller)) classOf[Machine]
      else if (isOneOf(block.getBlock, Constants.BlockName.HologramTier1, Constants.BlockName.HologramTier2)) classOf[tileentity.Hologram]
      else if (isOneOf(block.getBlock, Constants.BlockName.Printer)) classOf[tileentity.Printer]
      else if (isOneOf(block.getBlock, Constants.BlockName.Relay)) classOf[tileentity.Relay]
      else if (isOneOf(block.getBlock, Constants.BlockName.Redstone)) if (BundledRedstone.isAvailable) classOf[component.Redstone.Bundled] else classOf[component.Redstone.Vanilla]
      else if (isOneOf(block.getBlock, Constants.BlockName.ScreenTier1)) classOf[common.component.TextBuffer]: Class[? <: Environment]
      else if (isOneOf(block.getBlock, Constants.BlockName.ScreenTier2, Constants.BlockName.ScreenTier3)) classOf[common.component.Screen]
      else if (isOneOf(block.getBlock, Constants.BlockName.Robot)) classOf[component.Robot]: Class[? <: Environment]
      else if (isOneOf(block.getBlock, Constants.BlockName.Waypoint)) classOf[tileentity.Waypoint]: Class[? <: Environment]
      else null
    case _ =>
      if (api.Items.get(stack) == api.Items.get(Constants.ItemName.Drone)) classOf[component.Drone]: Class[? <: Environment]
      else null
  }

  private def isOneOf(block: Block, names: String*) = names.exists(api.Items.get(_).block == block)
}
