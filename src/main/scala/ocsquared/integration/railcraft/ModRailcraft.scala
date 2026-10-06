package ocsquared.integration.railcraft

import li.cil.oc.api
import li.cil.oc.api.Driver
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModRailcraft extends ModProxy {
  override def getMod = Mods.Railcraft

  override def initialize():Unit = {
    api.IMC.registerWrenchTool("ocsquared.integration.railcraft.EventHandlerRailcraft.useWrench")
    api.IMC.registerWrenchToolCheck("ocsquared.integration.railcraft.EventHandlerRailcraft.isWrench")

    Driver.add(DriverWorldspike)
  }
}