package ocsquared.integration.opencomputers

import ocsquared.Constants
import ocsquared.OpenComputers
import ocsquared.Settings
import li.cil.oc.api
import li.cil.oc.api.network.EnvironmentHost
import ocsquared.common.Loot
import ocsquared.common.Slot
import ocsquared.common.item.Delegator
import ocsquared.common.item.FloppyDisk
import ocsquared.common.item.HardDiskDrive
import ocsquared.common.item.data.DriveData
import ocsquared.server.component.Drive
import ocsquared.server.fs.FileSystem.{ItemLabel, ReadOnlyLabel}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.DimensionManager

object DriverFileSystem extends Item {
  val UUIDVerifier = """^([0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12})$""".r

  override def worksWith(stack: ItemStack) = isOneOf(stack,
    api.Items.get(Constants.ItemName.HDDTier1),
    api.Items.get(Constants.ItemName.HDDTier2),
    api.Items.get(Constants.ItemName.HDDTier3),
    api.Items.get(Constants.ItemName.Floppy)) &&
    (!stack.hasTagCompound || !stack.getTagCompound.hasKey(Settings.namespace + "lootPath"))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost) =
    if (host.world != null && host.world.isRemote) null
    else Delegator.subItem(stack) match {
      case Some(hdd: HardDiskDrive) => createEnvironment(stack, hdd.kiloBytes * 1024, hdd.platterCount, host, hdd.tier + 2)
      case Some(disk: FloppyDisk) => createEnvironment(stack, Settings.get.floppySize * 1024, 1, host, 1)
      case _ => null
    }

  override def slot(stack: ItemStack) =
    Delegator.subItem(stack) match {
      case Some(hdd: HardDiskDrive) => Slot.HDD
      case Some(disk: FloppyDisk) => Slot.Floppy
      case _ => throw new IllegalArgumentException()
    }

  override def tier(stack: ItemStack) =
    Delegator.subItem(stack) match {
      case Some(hdd: HardDiskDrive) => hdd.tier
      case _ => 0
    }

  private def createEnvironment(stack: ItemStack, capacity: Int, platterCount: Int, host: EnvironmentHost, speed: Int) = if (DimensionManager.getWorld(0) != null) {
    if (stack.hasTagCompound && stack.getTagCompound.hasKey(Settings.namespace + "lootFactory")) {
      // Loot disk, create file system using factory callback.
      Loot.factories.get(stack.getTagCompound.getString(Settings.namespace + "lootFactory")) match {
        case Some(factory) =>
          val label =
            if (dataTag(stack).hasKey(Settings.namespace + "fs.label"))
              dataTag(stack).getString(Settings.namespace + "fs.label")
            else null
          api.FileSystem.asManagedEnvironment(factory.call(), label, host, Settings.resourceDomain + ":floppy_access")
        case _ => null // Invalid loot disk.
      }
    }
    else {
      // We have a bit of a chicken-egg problem here, because we want to use the
      // node's address as the folder name... so we generate the address here,
      // if necessary. No one will know, right? Right!?
      val address = addressFromTag(dataTag(stack))
      var label: api.fs.Label = new ReadWriteItemLabel(stack)
      val isFloppy = api.Items.get(stack) == api.Items.get(Constants.ItemName.Floppy)
      val sound = Settings.resourceDomain + ":" + (if (isFloppy) "floppy_access" else "hdd_access")
      val drive = new DriveData(stack)
      val environment = if (drive.isUnmanaged) {
        new Drive(capacity max 0, platterCount, label, Option(host), Option(sound), speed, drive.isLocked)
      }
      else {
        var fs = api.FileSystem.fromSaveDirectory(address, capacity max 0, Settings.get.bufferChanges)
        if (drive.isLocked) {
          fs = api.FileSystem.asReadOnly(fs)
          label = new ReadOnlyLabel(label.getLabel)
        }
        api.FileSystem.asManagedEnvironment(fs, label, host, sound, speed)
      }
      if (environment != null && environment.node != null) {
        environment.node.asInstanceOf[ocsquared.server.network.Node].address = address
      }
      environment
    }
  }
  else null

  private def addressFromTag(tag: NBTTagCompound) =
    if (tag.hasKey("node") && tag.getCompoundTag("node").hasKey("address")) {
      tag.getCompoundTag("node").getString("address") match {
        case UUIDVerifier(address) => address
        case _ => // Invalid disk address.
          val newAddress = java.util.UUID.randomUUID().toString
          tag.getCompoundTag("node").setString("address", newAddress)
          OpenComputers.log.warn(s"Generated new address for disk '${newAddress}'.")
          newAddress
      }
    }
    else java.util.UUID.randomUUID().toString

  private class ReadWriteItemLabel(stack: ItemStack) extends ItemLabel(stack) {
    var label: Option[String] = None

    override def getLabel = label.orNull

    override def setLabel(value: String):Unit = {
      label = Option(value).map(_.take(16))
    }

    private final val LabelTag = Settings.namespace + "fs.label"

    override def load(nbt: NBTTagCompound):Unit = {
      if (nbt.hasKey(LabelTag)) {
        label = Option(nbt.getString(LabelTag))
      }
    }

    override def save(nbt: NBTTagCompound):Unit = {
      label match {
        case Some(value) => nbt.setString(LabelTag, value)
        case _ =>
      }
    }
  }

}