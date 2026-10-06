package ocsquared.client.gui

import ocsquared.Localization
import ocsquared.common.container
import ocsquared.common.tileentity
import net.minecraft.entity.player.InventoryPlayer

class Adapter(playerInventory: InventoryPlayer, val adapter: tileentity.Adapter) extends DynamicGuiContainer(new container.Adapter(playerInventory, adapter)) {
  override def drawSecondaryForegroundLayer(mouseX: Int, mouseY: Int) = {
    super.drawSecondaryForegroundLayer(mouseX, mouseY)
    fontRenderer.drawString(
      Localization.localizeImmediately(adapter.getName),
      8, 6, 0x404040)
  }
}
