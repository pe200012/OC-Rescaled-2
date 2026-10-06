package ocsquared.common.item

import ocsquared.Settings

class TerminalServer(val parent: Delegator) extends traits.Delegate {
  override protected def tooltipData = Seq(Settings.get.terminalsPerServer)
}
