package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.server.component
import net.minecraft.item.ItemStack

object DriverEEPROM extends Item {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.EEPROM))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else new component.EEPROM()

  override def slot(stack: ItemStack) = Slot.EEPROM

  override def tier(stack: ItemStack) = Tier.One

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[?] =
      if (worksWith(stack))
        classOf[component.EEPROM]
      else null
  }

}
