package ocsquared.common.item.data

import ocsquared.common.nanomachines.ControllerImpl
import ocsquared.Constants
import ocsquared.Settings
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class NanomachineData extends ItemData(Constants.ItemName.Nanomachines) {
  def this(stack: ItemStack) = {
    this()
    load(stack)
  }

  def this(controller: ControllerImpl) = {
    this()
    uuid = controller.uuid
    val nbt = new NBTTagCompound()
    controller.configuration.save(nbt, forItem = true)
    configuration = Option(nbt)
  }

  var uuid = ""
  var configuration: Option[NBTTagCompound] = None

  private final val UUIDTag = Settings.namespace + "uuid"
  private final val ConfigurationTag = Settings.namespace + "configuration"

  override def load(nbt: NBTTagCompound): Unit = {
    uuid = nbt.getString(UUIDTag)
    if (nbt.hasKey(ConfigurationTag)) {
      configuration = Option(nbt.getCompoundTag(ConfigurationTag))
    }
    else {
      configuration = None
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    nbt.setString(UUIDTag, uuid)
    configuration.foreach(nbt.setTag(ConfigurationTag, _))
  }
}
