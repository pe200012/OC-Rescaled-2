package ocsquared.client.gui

import ocsquared.Localization
import ocsquared.common.container
import ocsquared.common.tileentity
import net.minecraft.entity.player.InventoryPlayer

class Charger(playerInventory: InventoryPlayer, val charger: tileentity.Charger) extends DynamicGuiContainer(new container.Charger(playerInventory, charger)) {
  override def drawSecondaryForegroundLayer(mouseX: Int, mouseY: Int) = {
    super.drawSecondaryForegroundLayer(mouseX, mouseY)
    fontRenderer.drawString(
      Localization.localizeImmediately(charger.getName),
      8, 6, 0x404040)
  }
}
