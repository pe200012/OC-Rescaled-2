package ocsquared.integration.thaumcraft

import li.cil.oc.api.Driver
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModThaumcraft extends ModProxy {
  override def getMod: Mods.ModBase = Mods.Thaumcraft

  override def initialize():Unit = {
    Driver.add(ConverterThaumcraftItems)
  }
}
