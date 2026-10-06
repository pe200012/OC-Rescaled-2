package ocsquared.common.container

import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.tileentity
import net.minecraft.entity.player.InventoryPlayer

class Raid(playerInventory: InventoryPlayer, raid: tileentity.Raid) extends Player(playerInventory, raid) {
  addSlotToContainer(60, 23, Slot.HDD, Tier.Three)
  addSlotToContainer(80, 23, Slot.HDD, Tier.Three)
  addSlotToContainer(100, 23, Slot.HDD, Tier.Three)
  addPlayerInventorySlots(8, 84)
}
