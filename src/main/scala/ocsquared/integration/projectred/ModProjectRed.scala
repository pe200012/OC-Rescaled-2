package ocsquared.integration.projectred

import li.cil.oc.api
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods
import ocsquared.integration.util.BundledRedstone
import ocsquared.integration.util.BundledRedstone.RedstoneProvider
import ocsquared.util.BlockPosition
import mrtjp.projectred.api.ProjectRedAPI
import net.minecraft.util.EnumFacing

object ModProjectRed extends ModProxy with RedstoneProvider {
  override def getMod = Mods.ProjectRedTransmission

  override def initialize(): Unit = {
    api.IMC.registerWrenchTool("ocsquared.integration.projectred.EventHandlerProjectRed.useWrench")
    api.IMC.registerWrenchToolCheck("ocsquared.integration.projectred.EventHandlerProjectRed.isWrench")

    BundledRedstone.addProvider(this)
  }

  override def computeInput(pos: BlockPosition, side: EnumFacing): Int = 0

  def computeBundledInput(pos: BlockPosition, side: EnumFacing): Array[Int] = {
    Option(ProjectRedAPI.transmissionAPI.getBundledInput(pos.world.get, pos.toBlockPos, side)).
      fold(null: Array[Int])(_.map(_ & 0xFF))
  }
}
