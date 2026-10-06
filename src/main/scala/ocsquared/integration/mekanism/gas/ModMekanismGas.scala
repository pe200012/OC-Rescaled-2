package ocsquared.integration.mekanism.gas

import li.cil.oc.api.Driver
import ocsquared.integration.Mod
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModMekanismGas extends ModProxy {
  override def getMod: Mod = Mods.MekanismGas

  override def initialize(): Unit = {
    Driver.add(ConverterGasStack)
  }
}
