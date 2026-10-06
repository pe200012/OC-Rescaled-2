package ocsquared.common.container

import ocsquared.common.Slot
import ocsquared.common.tileentity
import net.minecraft.entity.player.InventoryPlayer

class Adapter(playerInventory: InventoryPlayer, adapter: tileentity.Adapter) extends Player(playerInventory, adapter) {
  addSlotToContainer(80, 35, Slot.Upgrade)
  addPlayerInventorySlots(8, 84)
}
