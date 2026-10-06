package ocsquared.common.item

import ocsquared.Settings

class UpgradeSolarGenerator(val parent: Delegator) extends traits.Delegate with traits.ItemTier {
  override protected def tooltipData = Seq((Settings.get.solarGeneratorEfficiency * 100).toInt)
}
