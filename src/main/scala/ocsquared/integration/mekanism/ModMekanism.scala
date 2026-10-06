package ocsquared.integration.mekanism

import li.cil.oc.api
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModMekanism extends ModProxy {
  override def getMod = Mods.Mekanism

  override def initialize(): Unit = {
    api.IMC.registerWrenchTool("ocsquared.integration.mekanism.EventHandlerMekanism.useWrench")
    api.IMC.registerWrenchToolCheck("ocsquared.integration.mekanism.EventHandlerMekanism.isWrench")
  }
}
