package ocsquared.integration.cofh.tileentity

import li.cil.oc.api.Driver
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModCoFHTileEntity extends ModProxy {
  override def getMod = Mods.CoFHCore

  override def initialize():Unit = {
    Driver.add(new DriverEnergyInfo)
    Driver.add(new DriverRedstoneControl)
    Driver.add(new DriverSecureTile)
    Driver.add(new DriverSteamInfo)
  }
}