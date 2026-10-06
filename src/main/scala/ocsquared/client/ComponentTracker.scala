package ocsquared.client

import ocsquared.common
import net.minecraft.world.World

object ComponentTracker extends common.ComponentTracker {
  override protected def clear(world: World) = if (world.isRemote) super.clear(world)
}
