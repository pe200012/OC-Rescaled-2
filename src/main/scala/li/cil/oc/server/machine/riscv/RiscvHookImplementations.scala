package li.cil.oc.server.machine.riscv

import li.cil.oc.api.machine.Machine
import li.cil.oc.common.component.TextBuffer
import li.cil.oc.common.tileentity.Screen
import li.cil.oc.server.component.Keyboard
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing

import scala.jdk.CollectionConverters.*

/**
 * Implementations of [[RiscvHooks]] that know about multi-block screens.
 */
object RiscvHookImplementations {
  def install(): Unit = {
    RiscvHooks.adjacentScreen = (machine: Machine) => adjacentScreen(machine)
    RiscvHooks.screenKeyboards = (machine: Machine, screen: String) => keyboards(machine, screen)
  }

  private def adjacentScreen(machine: Machine): String = machine.host match {
    case tile: TileEntity if tile.getWorld != null =>
      EnumFacing.values.iterator
        .map(side => tile.getWorld.getTileEntity(tile.getPos.offset(side)))
        .collectFirst { case screen: Screen => screen.origin.node.address }
        .orNull
    case _ => null
  }

  // Keyboards may be attached to any block of a multi-block screen.
  private def keyboards(machine: Machine, address: String): java.util.Collection[String] = {
    val node = Option(machine.node).flatMap(own => Option(own.network)).flatMap(network => Option(network.node(address)))
    val blocks = node.map(_.host) match {
      case Some(buffer: TextBuffer) => buffer.host match {
        case screen: Screen => screen.origin.screens.toSeq.map(_.node)
        case _ => Seq(buffer.node)
      }
      case _ => Seq.empty
    }
    blocks.flatMap(_.neighbors.asScala).filter(_.host.isInstanceOf[Keyboard]).map(_.address).distinct.asJava
  }
}
