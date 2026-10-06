package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.driver.item.HostAware
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.item
import ocsquared.common.item.Delegator
import net.minecraft.item.ItemStack

object DriverUpgradeHover extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.HoverUpgradeTier1),
    api.Items.get(Constants.ItemName.HoverUpgradeTier2))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) = null

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) =
    Delegator.subItem(stack) match {
      case Some(upgrade: item.UpgradeHover) => upgrade.tier
      case _ => Tier.One
    }
}
