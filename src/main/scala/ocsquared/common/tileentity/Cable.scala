package ocsquared.common.tileentity

import li.cil.oc.api
import li.cil.oc.api.network.Visibility
import ocsquared.common
import ocsquared.Constants
import ocsquared.util.Color
import net.minecraft.item.EnumDyeColor
import ocsquared.util.ItemColorizer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class Cable extends traits.Environment with traits.NotAnalyzable with traits.ImmibisMicroblock with traits.Colored {
  val node = api.Network.newNode(this, Visibility.None).create()

  setColor(Color.rgbValues(EnumDyeColor.SILVER))

  def createItemStack() = {
    val stack = api.Items.get(Constants.BlockName.Cable).createItemStack(1)
    if (getColor != Color.rgbValues(EnumDyeColor.SILVER)) {
      ItemColorizer.setColor(stack, getColor)
    }
    stack
  }

  def fromItemStack(stack: ItemStack): Unit = {
    if (ItemColorizer.hasColor(stack)) {
      setColor(ItemColorizer.getColor(stack))
    }
  }

  override def controlsConnectivity = true

  override def consumesDye = true

  override protected def onColorChanged():Unit = {
    super.onColorChanged()
    if (getWorld != null && isServer) {
      api.Network.joinOrCreateNetwork(this)
    }
  }

  override def getRenderBoundingBox = common.block.Cable.bounds(getWorld, getPos).offset(x, y, z)
}
