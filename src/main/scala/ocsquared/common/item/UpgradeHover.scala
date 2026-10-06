package ocsquared.common.item

import ocsquared.Settings

class UpgradeHover(val parent: Delegator, val tier: Int) extends traits.Delegate with traits.ItemTier {
  override val unlocalizedName = super.unlocalizedName + tier

  override protected def tooltipName = Option(super.unlocalizedName)

  override protected def tooltipData = Seq(Settings.get.upgradeFlightHeight(tier))
}
