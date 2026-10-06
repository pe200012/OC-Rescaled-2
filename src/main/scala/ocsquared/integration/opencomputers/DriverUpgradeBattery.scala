package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.item
import ocsquared.common.item.Delegator
import ocsquared.server.component
import net.minecraft.item.ItemStack

object DriverUpgradeBattery extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.BatteryUpgradeTier1),
    api.Items.get(Constants.ItemName.BatteryUpgradeTier2),
    api.Items.get(Constants.ItemName.BatteryUpgradeTier3))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else new component.UpgradeBattery(tier(stack))

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) =
    Delegator.subItem(stack) match {
      case Some(battery: item.UpgradeBattery) => battery.tier
      case _ => Tier.One
    }
}
