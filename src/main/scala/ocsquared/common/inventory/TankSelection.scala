package ocsquared.common.inventory

trait TankSelection {
  def selectedTank: Int

  def selectedTank_=(value: Int): Unit
}
