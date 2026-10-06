package ocsquared.integration.enderstorage

import li.cil.oc.api.Driver
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModEnderStorage extends ModProxy {
  override def getMod = Mods.EnderStorage

  override def initialize():Unit = {
    Driver.add(new DriverFrequencyOwner)
  }
}