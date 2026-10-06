package ocsquared.integration.enderio

import li.cil.oc.api
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModEnderIO extends ModProxy {
  override def getMod = Mods.EnderIO

  override def initialize(): Unit = {
    api.IMC.registerWrenchTool("ocsquared.integration.enderio.EventHandlerEnderIO.useWrench")
    api.IMC.registerWrenchToolCheck("ocsquared.integration.enderio.EventHandlerEnderIO.isWrench")
  }
}
