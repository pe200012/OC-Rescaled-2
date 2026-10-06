package ocsquared.common.inventory

import ocsquared.Settings
import ocsquared.integration.opencomputers.DriverUpgradeDatabase
import net.minecraft.item.ItemStack

trait DatabaseInventory extends ItemStackInventory {
  def tier: Int = DriverUpgradeDatabase.tier(container)

  override def getSizeInventory = Settings.get.databaseEntriesPerTier(tier)

  override protected def inventoryName = "database"

  override def getInventoryStackLimit = 1

  override def getInventoryStackRequired = 1

  override def isItemValidForSlot(slot: Int, stack: ItemStack) = stack != container
}
