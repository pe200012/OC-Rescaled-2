package ocsquared.common.template

import ocsquared.Constants
import li.cil.oc.api
import ocsquared.common.inventory.ServerInventory
import ocsquared.util.ItemUtils
import net.minecraft.item.ItemStack

import scala.language.postfixOps

object ServerTemplate {
  def selectDisassembler(stack: ItemStack) =
    api.Items.get(stack) == api.Items.get(Constants.ItemName.ServerTier1) ||
      api.Items.get(stack) == api.Items.get(Constants.ItemName.ServerTier2) ||
      api.Items.get(stack) == api.Items.get(Constants.ItemName.ServerTier3)

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]) = {
    val info = new ServerInventory {
      override def container = stack
    }
    Array(ingredients, (0 until info.getSizeInventory).map(info.getStackInSlot).filter(null !=).toArray)
  }

  def register():Unit = {
    // Disassembler
    api.IMC.registerDisassemblerTemplate("Server",
      "ocsquared.common.template.ServerTemplate.selectDisassembler",
      "ocsquared.common.template.ServerTemplate.disassemble")
  }
}
