package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.network.ManagedEnvironment
import ocsquared.common.Slot
import ocsquared.server.component
import ocsquared.util.ExtendedInventory._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object DriverDiskDriveMountable extends Item with HostAware {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack,
    api.Items.get(Constants.ItemName.DiskDriveMountable))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment = host match {
    case rack: api.internal.Rack => new component.DiskDriveMountable(rack, rack.indexOf(stack))
    case _ => null // Welp.
  }

  override def slot(stack: ItemStack): String = Slot.RackMountable

  override def dataTag(stack: ItemStack): NBTTagCompound = {
    if (!stack.hasTagCompound) {
      stack.setTagCompound(new NBTTagCompound())
    }
    stack.getTagCompound
  }
}
