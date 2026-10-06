package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.inventory.DatabaseInventory
import ocsquared.common.item
import ocsquared.common.item.Delegator
import ocsquared.server.component
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object DriverUpgradeDatabase extends Item with api.driver.item.HostAware {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.DatabaseUpgradeTier1),
    api.Items.get(Constants.ItemName.DatabaseUpgradeTier2),
    api.Items.get(Constants.ItemName.DatabaseUpgradeTier3))

  override def createEnvironment(stack: ItemStack, host: api.network.EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else new component.UpgradeDatabase(new DatabaseInventory {
      override def container = stack

      override def isUsableByPlayer(player: EntityPlayer) = false
    })

  override def slot(stack: ItemStack) = Slot.Upgrade

  override def tier(stack: ItemStack) =
    Delegator.subItem(stack) match {
      case Some(database: item.UpgradeDatabase) => database.tier
      case _ => Tier.One
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[?] =
      if (worksWith(stack))
        classOf[component.UpgradeDatabase]
      else null
  }

}
