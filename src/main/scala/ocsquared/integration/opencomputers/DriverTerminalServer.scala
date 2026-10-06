package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.network.ManagedEnvironment
import ocsquared.common.Slot
import ocsquared.common.component.TerminalServer
import ocsquared.util.ExtendedInventory._
import net.minecraft.item.ItemStack

object DriverTerminalServer extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.TerminalServer))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment = host match {
    case rack: api.internal.Rack => new TerminalServer(rack, rack.indexOf(stack))
    case _ => null
  }

  override def slot(stack: ItemStack): String = Slot.RackMountable
}
