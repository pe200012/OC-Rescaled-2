package ocsquared.common

import ocsquared.Constants
import li.cil.oc.api
import net.minecraftforge.oredict.OreDictionary

/**
 * Hardware is not tiered anymore: of each tiered item, only the best tier is made. The others are
 * retired: they have no recipe and are hidden, and the tier kept stands in for them in recipes
 * that still ask for them. RAM and hard drives stay, as sizes. Creative variants stay creative,
 * except the APU's, as there are no APUs anymore.
 */
object Retired {
  /** Retired item to the one that replaces it. */
  val replacements: Map[String, String] = Map(
    Constants.ItemName.APUCreative -> Constants.ItemName.CPUTier3,
    Constants.ItemName.APUTier1 -> Constants.ItemName.CPUTier3,
    Constants.ItemName.APUTier2 -> Constants.ItemName.CPUTier3,
    Constants.ItemName.CPUTier1 -> Constants.ItemName.CPUTier3,
    Constants.ItemName.CPUTier2 -> Constants.ItemName.CPUTier3,
    Constants.BlockName.CaseTier1 -> Constants.BlockName.CaseTier3,
    Constants.BlockName.CaseTier2 -> Constants.BlockName.CaseTier3,
    Constants.ItemName.ServerTier1 -> Constants.ItemName.ServerTier3,
    Constants.ItemName.ServerTier2 -> Constants.ItemName.ServerTier3,
    Constants.BlockName.ScreenTier1 -> Constants.BlockName.ScreenTier3,
    Constants.BlockName.ScreenTier2 -> Constants.BlockName.ScreenTier3,
    Constants.ItemName.GraphicsCardTier1 -> Constants.ItemName.GraphicsCardTier3,
    Constants.ItemName.GraphicsCardTier2 -> Constants.ItemName.GraphicsCardTier3,
    Constants.ItemName.ChipTier2 -> Constants.ItemName.ChipTier1,
    Constants.ItemName.ChipTier3 -> Constants.ItemName.ChipTier1,
    Constants.ItemName.CardContainerTier1 -> Constants.ItemName.CardContainerTier3,
    Constants.ItemName.CardContainerTier2 -> Constants.ItemName.CardContainerTier3,
    Constants.ItemName.UpgradeContainerTier1 -> Constants.ItemName.UpgradeContainerTier3,
    Constants.ItemName.UpgradeContainerTier2 -> Constants.ItemName.UpgradeContainerTier3,
    Constants.ItemName.ComponentBusTier1 -> Constants.ItemName.ComponentBusTier3,
    Constants.ItemName.ComponentBusTier2 -> Constants.ItemName.ComponentBusTier3,
    Constants.ItemName.DataCardTier1 -> Constants.ItemName.DataCardTier3,
    Constants.ItemName.DataCardTier2 -> Constants.ItemName.DataCardTier3,
    Constants.ItemName.DatabaseUpgradeTier1 -> Constants.ItemName.DatabaseUpgradeTier3,
    Constants.ItemName.DatabaseUpgradeTier2 -> Constants.ItemName.DatabaseUpgradeTier3,
    Constants.ItemName.BatteryUpgradeTier1 -> Constants.ItemName.BatteryUpgradeTier3,
    Constants.ItemName.BatteryUpgradeTier2 -> Constants.ItemName.BatteryUpgradeTier3,
    Constants.ItemName.HoverUpgradeTier1 -> Constants.ItemName.HoverUpgradeTier2,
    Constants.ItemName.WirelessNetworkCardTier1 -> Constants.ItemName.WirelessNetworkCardTier2,
    Constants.ItemName.RedstoneCardTier1 -> Constants.ItemName.RedstoneCardTier2,
    Constants.ItemName.MicrocontrollerCaseTier1 -> Constants.ItemName.MicrocontrollerCaseTier2,
    Constants.ItemName.DroneCaseTier1 -> Constants.ItemName.DroneCaseTier2,
    Constants.ItemName.TabletCaseTier1 -> Constants.ItemName.TabletCaseTier2
  )

  def contains(name: String): Boolean = replacements.contains(name)

  /**
   * Lets the item kept stand in for the retired ones in recipes, by giving it their ore names.
   */
  def registerReplacements(): Unit = {
    for ((retired, kept) <- replacements; retiredInfo <- Option(api.Items.get(retired)); keptInfo <- Option(api.Items.get(kept))) {
      val retiredStack = retiredInfo.createItemStack(1)
      val keptStack = keptInfo.createItemStack(1)
      if (!retiredStack.isEmpty && !keptStack.isEmpty) {
        for (id <- OreDictionary.getOreIDs(retiredStack)) {
          OreDictionary.registerOre(OreDictionary.getOreName(id), keptStack)
        }
      }
    }
  }
}
