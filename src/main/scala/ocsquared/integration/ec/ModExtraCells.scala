package ocsquared.integration.ec

import li.cil.oc.api.Driver
import ocsquared.integration.Mod
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModExtraCells extends ModProxy {
  override def getMod: Mod = Mods.ExtraCells

  override def initialize(): Unit = {
    Driver.add(DriverController)
    Driver.add(DriverBlockInterface)

    Driver.add(DriverController.Provider)
    Driver.add(DriverBlockInterface.Provider)
  }
}