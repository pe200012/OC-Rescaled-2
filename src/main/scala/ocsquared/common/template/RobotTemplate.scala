package ocsquared.common.template

import ocsquared.Constants
import ocsquared.Settings
import li.cil.oc.api
import li.cil.oc.api.internal
import ocsquared.common.Slot
import ocsquared.common.Tier
import ocsquared.common.item.data.RobotData
import ocsquared.util.ItemUtils
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import scala.jdk.CollectionConverters.*


object RobotTemplate extends Template {
  override protected def hostClass = classOf[internal.Robot]

  def selectTier1(stack: ItemStack) = api.Items.get(stack) == api.Items.get(Constants.BlockName.CaseTier1)

  def selectTier2(stack: ItemStack) = api.Items.get(stack) == api.Items.get(Constants.BlockName.CaseTier2)

  def selectTier3(stack: ItemStack) = api.Items.get(stack) == api.Items.get(Constants.BlockName.CaseTier3)

  def selectCreative(stack: ItemStack) = api.Items.get(stack) == api.Items.get(Constants.BlockName.CaseCreative)

  def validate(inventory: IInventory): Array[AnyRef] = validateComputer(inventory)

  def assemble(inventory: IInventory) = {
    val items = (1 until inventory.getSizeInventory).map(inventory.getStackInSlot)
    val data = new RobotData()
    data.tier = caseTier(inventory)
    data.name = RobotData.randomName
    data.robotEnergy = Settings.get.bufferRobot.toInt
    data.totalEnergy = data.robotEnergy
    data.containers = items.take(3).filter(!_.isEmpty).toArray
    data.components = items.drop(3).filter(!_.isEmpty).toArray
    val stack = data.createItemStack()
    val energy = Settings.get.robotBaseCost + complexity(inventory) * Settings.get.robotComplexityCost

    Array(stack, Double.box(energy))
  }

  def selectDisassembler(stack: ItemStack) = api.Items.get(stack) == api.Items.get(Constants.BlockName.Robot)

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]) = {
    val info = new RobotData(stack)
    val itemName = Constants.BlockName.Case(info.tier)

    Array(api.Items.get(itemName).createItemStack(1)) ++ info.containers ++ info.components
  }

  def register():Unit = {
    // Tier 1
    api.IMC.registerAssemblerTemplate(
      "Robot (Tier 1)",
      "ocsquared.common.template.RobotTemplate.selectTier1",
      "ocsquared.common.template.RobotTemplate.validate",
      "ocsquared.common.template.RobotTemplate.assemble",
      hostClass,
      Array(
        Tier.Two,
        Tier.One,
        Tier.One
      ),
      Array(
        Tier.One,
        Tier.One,
        Tier.One
      ),
      Iterable(
        (Slot.Card, Tier.One),
        null,
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        (Slot.Memory, Tier.One),
        (Slot.EEPROM, Tier.Any),
        (Slot.HDD, Tier.One)
      ).map(toPair).asJava)

    // Tier 2
    api.IMC.registerAssemblerTemplate(
      "Robot (Tier 2)",
      "ocsquared.common.template.RobotTemplate.selectTier2",
      "ocsquared.common.template.RobotTemplate.validate",
      "ocsquared.common.template.RobotTemplate.assemble",
      hostClass,
      Array(
        Tier.Three,
        Tier.Two,
        Tier.One
      ),
      Array(
        Tier.Two,
        Tier.Two,
        Tier.Two,
        Tier.One,
        Tier.One,
        Tier.One
      ),
      Iterable(
        (Slot.Card, Tier.Two),
        (Slot.Card, Tier.One),
        null,
        (Slot.CPU, Tier.Two),
        (Slot.Memory, Tier.Two),
        (Slot.Memory, Tier.Two),
        (Slot.EEPROM, Tier.Any),
        (Slot.HDD, Tier.Two)
      ).map(toPair).asJava)

    // Tier 3
    api.IMC.registerAssemblerTemplate(
      "Robot (Tier 3)",
      "ocsquared.common.template.RobotTemplate.selectTier3",
      "ocsquared.common.template.RobotTemplate.validate",
      "ocsquared.common.template.RobotTemplate.assemble",
      hostClass,
      Array(
        Tier.Three,
        Tier.Two,
        Tier.Two
      ),
      Array(
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Two,
        Tier.Two,
        Tier.Two,
        Tier.One,
        Tier.One,
        Tier.One
      ),
      Iterable(
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Two),
        (Slot.Card, Tier.Two),
        (Slot.CPU, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.EEPROM, Tier.Any),
        (Slot.HDD, Tier.Three),
        (Slot.HDD, Tier.Two)
      ).map(toPair).asJava)

    // Creative
    api.IMC.registerAssemblerTemplate(
      "Robot (Creative)",
      "ocsquared.common.template.RobotTemplate.selectCreative",
      "ocsquared.common.template.RobotTemplate.validate",
      "ocsquared.common.template.RobotTemplate.assemble",
      hostClass,
      Array(
        Tier.Three,
        Tier.Three,
        Tier.Three
      ),
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
        (Slot.EEPROM, Tier.Any),
        (Slot.HDD, Tier.Three),
        (Slot.HDD, Tier.Three)
      ).map(toPair).asJava)

    // Disassembler
    api.IMC.registerDisassemblerTemplate(
      "Robot",
      "ocsquared.common.template.RobotTemplate.selectDisassembler",
      "ocsquared.common.template.RobotTemplate.disassemble")
  }

  override protected def caseTier(inventory: IInventory) = ItemUtils.caseTier(inventory.getStackInSlot(0))
}
