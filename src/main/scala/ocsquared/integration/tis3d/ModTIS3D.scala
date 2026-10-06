package ocsquared.integration.tis3d

import ocsquared.integration.ModProxy
import ocsquared.integration.Mods

object ModTIS3D extends ModProxy {
  override def getMod = Mods.TIS3D

  override def initialize(): Unit = {
    SerialInterfaceProviderAdapter.init()
  }
}
