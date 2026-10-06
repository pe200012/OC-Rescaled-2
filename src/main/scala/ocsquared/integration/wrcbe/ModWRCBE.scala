package ocsquared.integration.wrcbe

import ocsquared.integration.ModProxy
import ocsquared.integration.Mods
import ocsquared.integration.util.WirelessRedstone

object ModWRCBE extends ModProxy {
  override def getMod: Mods.SimpleMod = Mods.WirelessRedstoneCBE

  override def initialize():Unit = {
    WirelessRedstone.systems += WirelessRedstoneCBE
  }
}
