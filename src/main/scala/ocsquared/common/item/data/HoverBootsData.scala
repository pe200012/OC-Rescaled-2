package ocsquared.common.item.data

import ocsquared.Constants
import ocsquared.Settings
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class HoverBootsData extends ItemData(Constants.ItemName.HoverBoots) {
  def this(stack: ItemStack) = {
    this()
    load(stack)
  }

  var charge = 0.0

  private final val ChargeTag = Settings.namespace + "charge"

  override def load(nbt: NBTTagCompound):Unit = {
    charge = nbt.getDouble(ChargeTag)
  }

  override def save(nbt: NBTTagCompound):Unit = {
    nbt.setDouble(ChargeTag, charge)
  }
}
