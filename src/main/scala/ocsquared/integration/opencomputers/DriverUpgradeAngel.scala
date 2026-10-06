package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.server.component
import net.minecraft.item.ItemStack

object DriverUpgradeAngel extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.AngelUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else new component.UpgradeAngel()

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) = Tier.Two
}
