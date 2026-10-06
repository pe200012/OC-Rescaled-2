package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.internal.Robot
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.entity.Drone
import ocsquared.common.item.TabletWrapper
import ocsquared.server.component
import ocsquared.server.component.UpgradeTractorBeam
import net.minecraft.item.ItemStack

object DriverUpgradeTractorBeam extends Item with HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.TractorBeamUpgrade))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else host match {
      case drone: Drone => new UpgradeTractorBeam.Drone(drone)
      case robot: Robot => new component.UpgradeTractorBeam.Player(host, robot.player)
      case tablet: TabletWrapper => new component.UpgradeTractorBeam.Player(host, () => tablet.player)
      case _ => null
    }

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) = Tier.Three

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[?] =
      if (worksWith(stack))
        classOf[component.UpgradeTractorBeam.Common]
      else null
  }

}
