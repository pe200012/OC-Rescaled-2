package ocsquared.common.block

import ocsquared.common.tileentity
import net.minecraft.world.World

class Geolyzer extends SimpleBlock {
  override def createNewTileEntity(world: World, metadata: Int) = new tileentity.Geolyzer()
}
