package ocsquared.common.template

import ocsquared.Constants
import ocsquared.Settings
import li.cil.oc.api
import li.cil.oc.api.internal
import li.cil.oc.api.internal.Microcontroller
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.item.data.MicrocontrollerData
import ocsquared.util.ItemUtils
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

import scala.jdk.CollectionConverters.*

object MicrocontrollerTemplate extends Template {
  override protected val suggestedComponents = Array(
    "BIOS" -> hasComponent("eeprom"))

  override protected def hostClass: Class[Microcontroller] = classOf[internal.Microcontroller]

  def selectTier1(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier1)

  def selectTier2(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier2)

  def selectTierCreative(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseCreative)

  def validate(inventory: IInventory): Array[AnyRef] = validateComputer(inventory)

  def assemble(inventory: IInventory): Array[Object] = {
    val items = (0 until inventory.getSizeInventory).map(inventory.getStackInSlot)
    val data = new MicrocontrollerData()
    data.tier = caseTier(inventory)
    data.components = items.drop(1).filter(!_.isEmpty).toArray
    data.storedEnergy = Settings.get.bufferMicrocontroller.toInt
    val stack = data.createItemStack()
    val energy = Settings.get.microcontrollerBaseCost + complexity(inventory) * Settings.get.microcontrollerComplexityCost

    Array(stack, Double.box(energy))
  }

  def selectDisassembler(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.BlockName.Microcontroller)

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]): Array[ItemStack] = {
    val info = new MicrocontrollerData(stack)
    val itemName = Constants.ItemName.MicrocontrollerCase(info.tier)

    Array(api.Items.get(itemName).createItemStack(1)) ++ info.components
  }

  def register():Unit = {
    // Tier 1
    api.IMC.registerAssemblerTemplate(
      "Microcontroller (Tier 1)",
      "ocsquared.common.template.MicrocontrollerTemplate.selectTier1",
      "ocsquared.common.template.MicrocontrollerTemplate.validate",
      "ocsquared.common.template.MicrocontrollerTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Two
      ),
      Iterable(
        (Slot.Card, Tier.One),
        (Slot.Card, Tier.One),
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        null,
        (Slot.EEPROM, Tier.Any)
      ).map(toPair).asJava)

    // Tier 2
    api.IMC.registerAssemblerTemplate(
      "Microcontroller (Tier 2)",
      "ocsquared.common.template.MicrocontrollerTemplate.selectTier2",
      "ocsquared.common.template.MicrocontrollerTemplate.validate",
      "ocsquared.common.template.MicrocontrollerTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Three
      ),
      Iterable(
        (Slot.Card, Tier.Two),
        (Slot.Card, Tier.One),
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        (Slot.Memory, Tier.One),
        (Slot.EEPROM, Tier.Any)
      ).map(toPair).asJava)

    // Creative
    api.IMC.registerAssemblerTemplate(
      "Microcontroller (Creative)",
      "ocsquared.common.template.MicrocontrollerTemplate.selectTierCreative",
      "ocsquared.common.template.MicrocontrollerTemplate.validate",
      "ocsquared.common.template.MicrocontrollerTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three
      ),
      Iterable(
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Three),
        (Slot.CPU, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.EEPROM, Tier.Any)
      ).map(toPair).asJava)

    // Disassembler
    api.IMC.registerDisassemblerTemplate(
      "Microcontroller",
      "ocsquared.common.template.MicrocontrollerTemplate.selectDisassembler",
      "ocsquared.common.template.MicrocontrollerTemplate.disassemble")
  }

  override protected def maxComplexity(inventory: IInventory): Int =
    if (caseTier(inventory) == Tier.Two) 5
    else if (caseTier(inventory) == Tier.Four) 9001 // Creative
    else 4

  override protected def caseTier(inventory: IInventory): Int = ItemUtils.caseTier(inventory.getStackInSlot(0))
}
