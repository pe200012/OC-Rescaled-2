package ocsquared.integration.jei

import java.util

import ocsquared.Constants
import li.cil.oc.api
import ocsquared.common.Loot
import ocsquared.common.recipe.LootDiskCyclingRecipe
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe._
import net.minecraft.item.ItemStack

import scala.jdk.CollectionConverters.*

object LootDiskCyclingRecipeHandler extends IRecipeWrapperFactory[LootDiskCyclingRecipe] {
  override def getRecipeWrapper(recipe: LootDiskCyclingRecipe): IRecipeWrapper = new LootDiskCyclingRecipeWrapper(recipe)

  class LootDiskCyclingRecipeWrapper(val recipe: LootDiskCyclingRecipe) extends BlankRecipeWrapper {

    def getInputs: util.List[util.List[ItemStack]] = List(Loot.disksForCycling.asJava, List(api.Items.get(Constants.ItemName.Wrench).createItemStack(1)).asJava).asJava

    def getOutputs: util.List[ItemStack] = Loot.disksForCycling.toList.asJava

    override def getIngredients(ingredients: IIngredients): Unit = {
      ingredients.setInputLists(classOf[ItemStack], getInputs)
      ingredients.setOutputs(classOf[ItemStack], getOutputs)
    }
  }

}



