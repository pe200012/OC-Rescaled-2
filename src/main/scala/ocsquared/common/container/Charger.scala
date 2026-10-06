package ocsquared.common.container

import ocsquared.common.tileentity
import net.minecraft.entity.player.InventoryPlayer

class Charger(playerInventory: InventoryPlayer, charger: tileentity.Charger) extends Player(playerInventory, charger) {
  addSlotToContainer(80, 35, "tablet")
  addPlayerInventorySlots(8, 84)
}
