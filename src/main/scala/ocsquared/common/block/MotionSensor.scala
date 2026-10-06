package ocsquared.common.block

import ocsquared.common.tileentity
import net.minecraft.world.World

class MotionSensor extends SimpleBlock {
  override def createNewTileEntity(world: World, metadata: Int) = new tileentity.MotionSensor()
}
