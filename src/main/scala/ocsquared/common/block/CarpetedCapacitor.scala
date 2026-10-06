package ocsquared.common.block

import ocsquared.common.tileentity
import net.minecraft.world.World

class CarpetedCapacitor extends Capacitor {
  override def createNewTileEntity(world: World, metadata: Int) = new tileentity.CarpetedCapacitor()
}
