package ocsquared.server

import ocsquared.OpenComputers
import ocsquared.common.{Proxy => CommonProxy}
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry

private[ocsquared] class Proxy extends CommonProxy {
  override def init(e: FMLInitializationEvent):Unit = {
    super.init(e)

    NetworkRegistry.INSTANCE.registerGuiHandler(OpenComputers, GuiHandler)
  }
}
