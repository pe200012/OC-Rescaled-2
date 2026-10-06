package ocsquared.integration.forestry

import li.cil.oc.api.Driver
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModForestry extends ModProxy {
  override def getMod = Mods.Forestry

  override def initialize():Unit = {
    Driver.add(new ConverterIAlleles)
    Driver.add(new ConverterIIndividual)
    Driver.add(ConverterItemStack)
    Driver.add(new DriverAnalyzer)
    Driver.add(new DriverBeeHouse)
  }
}