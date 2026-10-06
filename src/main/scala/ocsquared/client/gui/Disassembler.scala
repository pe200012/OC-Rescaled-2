package ocsquared.client.gui

import ocsquared.Localization
import ocsquared.client.Textures
import ocsquared.client.gui.widget.ProgressBar
import ocsquared.common.container
import ocsquared.common.tileentity
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer

class Disassembler(playerInventory: InventoryPlayer, val disassembler: tileentity.Disassembler) extends DynamicGuiContainer(new container.Disassembler(playerInventory, disassembler)) {
  val progress = addWidget(new ProgressBar(18, 65))

  override def drawSecondaryForegroundLayer(mouseX: Int, mouseY: Int) = {
    fontRenderer.drawString(
      Localization.localizeImmediately(disassembler.getName),
      8, 6, 0x404040)
  }

  override def drawGuiContainerBackgroundLayer(dt: Float, mouseX: Int, mouseY: Int):Unit = {
    GlStateManager.color(1, 1, 1)
    Textures.bind(Textures.GUI.Disassembler)
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
    progress.level = inventoryContainer.disassemblyProgress / 100.0
    drawWidgets()
  }
}
