package ocsquared.common.item

class UpgradeStickyPiston(val parent: Delegator) extends traits.Delegate with traits.ItemTier {
  override protected def tooltipName: Option[String] = Option(super.unlocalizedName)
}

