package ocsquared.integration.ic2

import ocsquared.common.EventHandler
import ocsquared.common.tileentity.traits.power
import ocsquared.util.SideTracker
import net.minecraftforge.common.MinecraftForge

object IC2EventHandler {
  def scheduleIC2Add(tileEntity: power.IndustrialCraft2Experimental): Unit ={
    if (SideTracker.isServer)
      tileEntity match {
        case tile: ic2.api.energy.tile.IEnergyTile =>
          EventHandler.scheduleServer (() => if (!tileEntity.addedToIC2PowerGrid && !tileEntity.isInvalid) {
            MinecraftForge.EVENT_BUS.post(new ic2.api.energy.event.EnergyTileLoadEvent(tile))
            tileEntity.addedToIC2PowerGrid = true
          })
        case _ =>
      }
  }
}
