package ocsquared.server.command

import li.cil.oc.api
import ocsquared.common.command.SimpleCommand
import ocsquared.common.nanomachines.ControllerImpl
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TextComponentString

object DebugNanomachinesCommand extends SimpleCommand("oc_debugNanomachines") {
  aliases += "oc_dn"

  override def getUsage(source: ICommandSender): String = name

  override def execute(server: MinecraftServer, source: ICommandSender, args: Array[String]): Unit = {
    source match {
      case player: EntityPlayer =>
        api.Nanomachines.installController(player) match {
          case controller: ControllerImpl =>
            controller.debug()
            player.sendMessage(new TextComponentString("Debug configuration created, see log for mappings."))
          case _ => // Someone did something.
        }
      case _ => throw new WrongUsageException("Can only be used by players.")
    }
  }

  // OP levels for reference:
  // 1 - Ops can bypass spawn protection.
  // 2 - Ops can use /clear, /difficulty, /effect, /gamemode, /gamerule, /give, /summon, /setblock and /tp, and can edit command blocks.
  // 3 - Ops can use /ban, /deop, /kick, and /op.
  // 4 - Ops can use /stop.

  override def getRequiredPermissionLevel = 2
}
