package ocsquared.server.component.traits

import ocsquared.Settings
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import ocsquared.server.component._
import ocsquared.util.ExtendedArguments._
import ocsquared.util.FluidUtils
import ocsquared.util.InventoryUtils

trait InventoryTransfer extends traits.WorldAware with traits.SideRestricted {
  // Return None on success, else Some("failure reason")
  def onTransferContents(): Option[String]

  @Callback(doc = """function(sourceSide:number, sinkSide:number[, count:number[, sourceSlot:number[, sinkSlot:number]]]):boolean -- Transfer some items between two inventories.""")
  def transferItem(context: Context, args: Arguments): Array[AnyRef] = {
    val sourceSide = checkSideForAction(args, 0)
    val sourcePos = position.offset(sourceSide)
    val sinkSide = checkSideForAction(args, 1)
    val sinkPos = position.offset(sinkSide)
    val count = args.optItemCount(2)

    onTransferContents() match {
      case Some(reason) =>
        result((), reason)
      case _ =>
        val extractor = if (args.count > 3) {
          val sourceSlot = args.checkSlot(InventoryUtils.inventoryAt(sourcePos, sourceSide.getOpposite).getOrElse(throw new IllegalArgumentException("no inventory")), 3)
          val sinkSlot = args.optSlot(InventoryUtils.inventoryAt(sinkPos, sinkSide.getOpposite).getOrElse(throw new IllegalArgumentException("no inventory")), 4, -1)

          InventoryUtils.getTransferBetweenInventoriesSlotsAt(sourcePos, sourceSide.getOpposite, sourceSlot, sinkPos, Option(sinkSide.getOpposite), if (sinkSlot < 0) None else Option(sinkSlot), count)
        }
        else
          InventoryUtils.getTransferBetweenInventoriesAt(sourcePos, sourceSide.getOpposite, sinkPos, Option(sinkSide.getOpposite), count)

        Option(extractor) match {
          case Some(ex) => result(ex())
          case _ => result((), "no inventory")
        }
    }
  }

  @Callback(doc = """function(sourceSide:number, sinkSide:number[, count:number [, sourceTank:number]]):boolean, number -- Transfer some fluid between two tanks. Returns operation result and filled amount""")
  def transferFluid(context: Context, args: Arguments): Array[AnyRef] = {
    val sourceSide = checkSideForAction(args, 0)
    val sourcePos = position.offset(sourceSide)
    val sinkSide = checkSideForAction(args, 1)
    val sinkPos = position.offset(sinkSide)
    val count = args.optFluidCount(2)
    val sourceTank = args.optInteger(3, -1)

    onTransferContents() match {
      case Some(reason) =>
        result((), reason)
      case _ =>
        val moved = FluidUtils.transferBetweenFluidHandlersAt(sourcePos, sourceSide.getOpposite, sinkPos, sinkSide.getOpposite, count, sourceTank)
        if (moved > 0) context.pause(moved / Settings.get.transposerFluidTransferRate) // Allow up to 16 buckets per second.
        result(moved > 0, moved)
    }
  }
}
