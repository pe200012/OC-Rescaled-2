package ocsquared.integration.appeng

import ocsquared.common.EventHandler
import ocsquared.common.tileentity.traits.power
import ocsquared.util.SideTracker

object AE2EventHandler {
  def scheduleAE2Add(tileEntity: power.AppliedEnergistics2): Unit = {
    if (SideTracker.isServer) EventHandler.scheduleServer(() => tileEntity.updateGridNodeState())
  }
}
