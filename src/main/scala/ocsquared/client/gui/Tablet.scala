package ocsquared.client.gui

import ocsquared.Localization
import ocsquared.common.container
import ocsquared.common.item.TabletWrapper
import net.minecraft.entity.player.InventoryPlayer

class Tablet(playerInventory: InventoryPlayer, val tablet: TabletWrapper) extends DynamicGuiContainer(new container.Tablet(playerInventory, tablet)) with traits.LockedHotbar {
  override def lockedStack = tablet.stack

  override def drawSecondaryForegroundLayer(mouseX: Int, mouseY: Int) = {
    super.drawSecondaryForegroundLayer(mouseX, mouseY)
    fontRenderer.drawString(
      Localization.localizeImmediately(tablet.getName),
      8, 6, 0x404040)
  }
}
