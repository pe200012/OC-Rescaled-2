package ocsquared.integration.ic2

import li.cil.oc.api
import li.cil.oc.api.Driver
import li.cil.oc.api.prefab.DriverSidedTileEntity
import ocsquared.integration.ModProxy
import ocsquared.integration.Mods
import net.minecraftforge.common.MinecraftForge

object ModIndustrialCraft2 extends ModProxy {
  override def getMod: Mods.SimpleMod = Mods.IndustrialCraft2

  def tryAddDriver(driver: DriverSidedTileEntity): Unit = {
    try {
      if (driver.getTileEntityClass != null)
        Driver.add(driver)
    } catch {
      case _: Exception =>
    }
  }

  override def initialize():Unit = {
    api.IMC.registerToolDurabilityProvider("ocsquared.integration.ic2.EventHandlerIndustrialCraft2.getDurability")
    api.IMC.registerWrenchTool("ocsquared.integration.ic2.EventHandlerIndustrialCraft2.useWrench")
    api.IMC.registerWrenchToolCheck("ocsquared.integration.ic2.EventHandlerIndustrialCraft2.isWrench")
    api.IMC.registerItemCharge(
      "IndustrialCraft2",
      "ocsquared.integration.ic2.EventHandlerIndustrialCraft2.canCharge",
      "ocsquared.integration.ic2.EventHandlerIndustrialCraft2.charge")

    MinecraftForge.EVENT_BUS.register(EventHandlerIndustrialCraft2)

    tryAddDriver(new DriverReactorRedstonePort)
    tryAddDriver(new DriverMassFab)

    Driver.add(new DriverEnergyConductor)
    Driver.add(new DriverEnergy)
    Driver.add(new DriverReactor)
    Driver.add(new DriverReactorChamber)

    Driver.add(new ConverterElectricItem)
  }
}
