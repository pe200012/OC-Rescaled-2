package ocsquared.integration.opencomputers

import java.util

import ocsquared.Constants
import li.cil.oc.api
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.api.driver.Converter
import ocsquared.server.component
import net.minecraft.item.ItemStack

import scala.jdk.CollectionConverters.*

object ConverterLinkedCard extends Converter {
  lazy val linkedCard: ItemInfo = api.Items.get(Constants.ItemName.LinkedCard)

  override def convert(value: scala.Any, output: util.Map[AnyRef, AnyRef]): Unit = value match {
    case stack: ItemStack if api.Items.get(stack) == linkedCard =>
      val card = new component.LinkedCard()
      output.asScala += "linkChannel" -> card.tunnel
    case _ => // Ignore.
  }
}
