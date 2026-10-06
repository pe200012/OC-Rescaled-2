package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.internal.Adapter
import li.cil.oc.api.internal.Rotatable
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.server.component
import ocsquared.server.component.UpgradeSignInAdapter
import ocsquared.server.component.UpgradeSignInRotatable
import net.minecraft.item.ItemStack

object DriverUpgradeSign extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.SignUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else host match {
      case rotatable: (EnvironmentHost & Rotatable) => new UpgradeSignInRotatable(rotatable)
      case adapter: (EnvironmentHost & Adapter) => new UpgradeSignInAdapter(adapter)
      case _ => null
    }

  override def slot(stack: ItemStack) = Slot.Upgrade

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[?] =
      if (worksWith(stack))
        classOf[component.UpgradeSign]
      else null
  }

}
