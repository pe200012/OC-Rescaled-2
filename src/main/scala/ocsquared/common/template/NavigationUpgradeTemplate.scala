package ocsquared.common.template

import ocsquared.Constants
import li.cil.oc.api
import ocsquared.common.item.data.NavigationUpgradeData
import net.minecraft.item.ItemStack

import scala.language.postfixOps

object NavigationUpgradeTemplate {
  def selectDisassembler(stack: ItemStack) = api.Items.get(stack) == api.Items.get(Constants.ItemName.NavigationUpgrade)

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]) = {
    val info = new NavigationUpgradeData(stack)
    ingredients.map {
      case part if part.getItem == net.minecraft.init.Items.FILLED_MAP => info.map
      case part => part
    }
  }

  def register():Unit = {
    // Disassembler
    api.IMC.registerDisassemblerTemplate(
      "Navigation Upgrade",
      "ocsquared.common.template.NavigationUpgradeTemplate.selectDisassembler",
      "ocsquared.common.template.NavigationUpgradeTemplate.disassemble")
  }
}
