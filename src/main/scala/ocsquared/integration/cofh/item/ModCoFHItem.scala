package ocsquared.integration.cofh.item

import li.cil.oc.api
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModCoFHItem extends ModProxy {
  override def getMod = Mods.CoFHCore

  override def initialize(): Unit = {
    api.IMC.registerWrenchTool("ocsquared.integration.cofh.item.EventHandlerCoFH.useWrench")
    api.IMC.registerWrenchToolCheck("ocsquared.integration.cofh.item.EventHandlerCoFH.isWrench")
  }
}
