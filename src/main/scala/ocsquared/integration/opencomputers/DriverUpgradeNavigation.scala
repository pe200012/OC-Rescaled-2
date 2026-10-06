package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.internal.Rotatable
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.server.component
import net.minecraft.item.ItemStack

object DriverUpgradeNavigation extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.NavigationUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else host match {
      case rotatable: (EnvironmentHost & Rotatable) => new component.UpgradeNavigation(rotatable)
      case _ => null
    }

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) = Tier.Two

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[?] =
      if (worksWith(stack))
        classOf[component.UpgradeNavigation]
      else null
  }

}
