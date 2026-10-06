package ocsquared.integration.waila

import ocsquared.integration.ModProxy
import ocsquared.integration.Mods
import net.minecraftforge.fml.common.event.FMLInterModComms

object ModWaila extends ModProxy {
  override def getMod = Mods.Waila

  override def initialize():Unit = {
    FMLInterModComms.sendMessage(Mods.IDs.Waila, "register", "ocsquared.integration.waila.BlockDataProvider.init")
  }
}
