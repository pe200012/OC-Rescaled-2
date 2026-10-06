package ocsquared.integration.computercraft

import li.cil.oc.api.Driver
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModComputerCraft extends ModProxy {
  override def getMod = Mods.ComputerCraft

  override def initialize():Unit = {
    PeripheralProvider.init()

    Driver.add(DriverComputerCraftMedia)
    Driver.add(new DriverPeripheral())

    Driver.add(new ConverterLuaObject)
  }
}
