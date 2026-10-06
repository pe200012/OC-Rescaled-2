package ocsquared.common.tileentity

import ocsquared.Settings
import li.cil.oc.api
import li.cil.oc.api.network.Component
import li.cil.oc.api.network.Node
import li.cil.oc.api.network.Visibility
import ocsquared.common.tileentity.traits.RedstoneChangedEventArgs
import ocsquared.integration.util.BundledRedstone
import ocsquared.server.component
import ocsquared.server.component.RedstoneVanilla
import ocsquared.util.ExtendedNBT._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

class Redstone extends traits.Environment with traits.BundledRedstoneAware with traits.Tickable {
  val instance: RedstoneVanilla =
    if (BundledRedstone.isAvailable)
      new component.Redstone.Bundled(this)
    else
      new component.Redstone.Vanilla(this)
  instance.wakeNeighborsOnly = false
  val node: Component = instance.node
  val dummyNode: Node = if (node != null) {
    node.setVisibility(Visibility.Network)
    _isOutputEnabled = true
    api.Network.newNode(this, Visibility.None).create()
  }
  else null

  // ----------------------------------------------------------------------- //

  private final val RedstoneTag = Settings.namespace + "redstone"

  override def readFromNBTForServer(nbt: NBTTagCompound):Unit = {
    super.readFromNBTForServer(nbt)
    instance.load(nbt.getCompoundTag(RedstoneTag))
  }

  override def writeToNBTForServer(nbt: NBTTagCompound):Unit = {
    super.writeToNBTForServer(nbt)
    nbt.setNewCompoundTag(RedstoneTag, instance.save)
  }

  // ----------------------------------------------------------------------- //

  override protected def onRedstoneInputChanged(args: RedstoneChangedEventArgs):Unit = {
    super.onRedstoneInputChanged(args)
    if (node != null && node.network != null) {
      node.connect(dummyNode)
      dummyNode.sendToNeighbors("redstone.changed", args)
    }
  }
}
