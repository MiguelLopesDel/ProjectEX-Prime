package dev.miguellopesdel.projectex.integration.jei;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import dev.miguellopesdel.projectex.recipe.AlchemyTableRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AlchemyTableCategory implements IRecipeCategory<AlchemyTableRecipe> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectEX.MOD_ID, "textures/gui/alchemy_table_jei.png");

	private static final int WIDTH = 128;
	private static final int HEIGHT = 18;

	private final IDrawable background;
	private final IDrawable icon;

	public AlchemyTableCategory(IGuiHelper helper) {
		background = helper.drawableBuilder(TEXTURE, 0, 0, WIDTH, HEIGHT).setTextureSize(128, 64).build();
		icon = helper.createDrawableItemStack(new ItemStack(ProjectEXItems.ALCHEMY_TABLE.get()));
	}

	@Override
	public RecipeType<AlchemyTableRecipe> getRecipeType() {
		return ProjectEXJEI.ALCHEMY_TABLE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("block.projectex.alchemy_table");
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, AlchemyTableRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.input());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 1).addItemStack(recipe.output());
	}

	/**
	 * The cost is what a player comes here to find out, and it is derived rather than written into
	 * the recipe, so it is worked out from the same item the recipe would price.
	 */
	@Override
	public void draw(AlchemyTableRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
		ItemStack[] inputs = recipe.input().getItems();
		String cost = TransmutationEMCFormatter.formatEMC(recipe.cost(inputs.length == 0 ? ItemStack.EMPTY : inputs[0])).getString();

		var font = Minecraft.getInstance().font;
		graphics.drawString(font, cost, (WIDTH - font.width(cost)) / 2, 1, 0x404040, false);

		String seconds = MathUtils.tickToSecFormatted(recipe.duration()).getString();
		graphics.drawString(font, seconds, (WIDTH - font.width(seconds)) / 2, 10, 0x808080, false);
	}
}
