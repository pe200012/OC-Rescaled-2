package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.internal.Adapter
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.entity.Drone
import ocsquared.common.tileentity.Robot
import ocsquared.server.component
import net.minecraft.item.ItemStack

object DriverUpgradeInventoryController extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.InventoryControllerUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else host match {
      case host: (EnvironmentHost & Adapter) => new component.UpgradeInventoryController.Adapter(host)
      case host: (EnvironmentHost & Drone) => new component.UpgradeInventoryController.Drone(host)
      case host: (EnvironmentHost & Robot) => new component.UpgradeInventoryController.Robot(host)
      case _ => null
    }

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) = Tier.Two

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[?] =
      if (worksWith(stack))
        classOf[component.UpgradeInventoryController.Robot]
      else null
  }

}
