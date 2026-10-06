package ocsquared.integration.minecraftforge

import li.cil.oc.api
import ocsquared.integration.Mod
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods
import net.minecraftforge.common.MinecraftForge

object ModMinecraftForge extends ModProxy {
  override def getMod: Mod = Mods.Forge

  override def initialize():Unit = {
    MinecraftForge.EVENT_BUS.register(EventHandlerMinecraftForge)
    api.IMC.registerItemCharge("MinecraftForge",
      "ocsquared.integration.minecraftforge.EventHandlerMinecraftForge.canCharge",
      "ocsquared.integration.minecraftforge.EventHandlerMinecraftForge.charge")
    api.Driver.add(DriverEnergyStorage)
  }
}
