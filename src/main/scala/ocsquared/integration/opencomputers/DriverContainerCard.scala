package ocsquared.integration.opencomputers

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.driver.item.Container
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.item
import ocsquared.common.item.Delegator
import net.minecraft.item.ItemStack

object DriverContainerCard extends Item with Container {
  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.CardContainerTier1),
    api.Items.get(Constants.ItemName.CardContainerTier2),
    api.Items.get(Constants.ItemName.CardContainerTier3))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) = null

  override def slot(stack: ItemStack) = Slot.Container

  override def providedSlot(stack: ItemStack) = Slot.Card

  override def providedTier(stack: ItemStack) = tier(stack)

  override def tier(stack: ItemStack) =
    Delegator.subItem(stack) match {
      case Some(container: item.UpgradeContainerCard) => container.tier
      case _ => Tier.One
    }
}
