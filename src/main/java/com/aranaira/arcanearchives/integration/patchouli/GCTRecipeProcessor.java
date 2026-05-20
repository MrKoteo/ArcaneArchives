package com.aranaira.arcanearchives.integration.patchouli;

import com.aranaira.arcanearchives.api.IGCTRecipe;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipeList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;
import vazkii.patchouli.common.util.ItemStackUtil;

import java.util.List;

public class GCTRecipeProcessor implements IComponentProcessor {

	private final ItemStack[] ingredients = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
	private ItemStack result = ItemStack.EMPTY;

	@Override
	public void setup(IVariableProvider<String> iVariableProvider) {
		String recipeOutput = iVariableProvider.get("recipe");
		if (recipeOutput == null || recipeOutput.isEmpty()) return;

		ItemStack outputStack = ItemStack.EMPTY;
		if (recipeOutput.contains(":")) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(recipeOutput));
			if (item != null) {
				outputStack = new ItemStack(item);
			}
		}

		if (outputStack.isEmpty()) return;

		IGCTRecipe recipe = GCTRecipeList.instance.getRecipeByOutput(outputStack);
		if (recipe == null) return;

		result = recipe.getRecipeOutput().copy();

		List<IngredientStack> ings = recipe.getIngredients();
		for (int i = 0; i < Math.min(ings.size(), 6); i++) {
			IngredientStack ing = ings.get(i);
			ItemStack[] stacks = ing.getMatchingStacks();
			if (stacks.length > 0) {
				ItemStack s = stacks[0].copy();
				s.setCount(ing.getCount());
				ingredients[i] = s;
			}
		}
	}

	@Override
	public String process(String s) {
		if (s.startsWith("item")) {
			int idx = Integer.parseInt(s.substring(4));
			if (idx >= 0 && idx < ingredients.length && !ingredients[idx].isEmpty()) {
				return ItemStackUtil.serializeStack(ingredients[idx]);
			}
			return ItemStackUtil.serializeStack(ItemStack.EMPTY);
		}
		if ("result".equals(s)) {
			return ItemStackUtil.serializeStack(result);
		}
		return null;
	}
}
