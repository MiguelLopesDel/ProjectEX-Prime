package dev.miguellopesdel.projectex.integration.jei;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.gui.GuiArcaneTablet;
import dev.miguellopesdel.projectex.gui.GuiStoneTable;
import dev.miguellopesdel.projectex.gui.GuiTableBase;
import dev.miguellopesdel.projectex.gui.WidgetKnowledgeItem;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import dev.miguellopesdel.projectex.recipe.AlchemyTableRecipe;
import dev.miguellopesdel.projectex.recipe.ProjectEXRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

@JeiPlugin
public class ProjectEXJEI implements IModPlugin {
	public static final RecipeType<AlchemyTableRecipe> ALCHEMY_TABLE =
			RecipeType.create(ProjectEX.MOD_ID, "alchemy_table", AlchemyTableRecipe.class);

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ProjectEX.MOD_ID, "jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new AlchemyTableCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ClientLevel level = Minecraft.getInstance().level;

		if (level != null) {
			registration.addRecipes(ALCHEMY_TABLE,
					level.getRecipeManager().getAllRecipesFor(ProjectEXRecipeTypes.ALCHEMY_TABLE.get()));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ProjectEXItems.ALCHEMY_TABLE.get()), ALCHEMY_TABLE);
		registration.addRecipeCatalyst(new ItemStack(ProjectEXItems.ARCANE_TABLET.get()), RecipeTypes.CRAFTING);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(new ArcaneTabletTransfer(registration.getTransferHelper()), RecipeTypes.CRAFTING);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		JeiSearchSync.setRuntime(runtime);
	}

	@Override
	public void onRuntimeUnavailable() {
		JeiSearchSync.setRuntime(null);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		IIngredientManager ingredients = registration.getJeiHelpers().getIngredientManager();

		registration.addGuiContainerHandler(GuiArcaneTablet.class, new PanelHandler<>(ingredients));
		registration.addGuiContainerHandler(GuiStoneTable.class, new PanelHandler<>(ingredients));
	}

	/**
	 * Teaches JEI about the ring, so that hovering an item in it and pressing R or U works the way it
	 * does over any slot, and about the tablet's crafting panel, so JEI does not draw its list on top
	 * of it.
	 */
	private record PanelHandler<T extends GuiTableBase<?>>(IIngredientManager ingredients) implements IGuiContainerHandler<T> {
		@Override
		public List<Rect2i> getGuiExtraAreas(T screen) {
			return screen.extraAreas();
		}

		@Override
		public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(T screen, double mouseX, double mouseY) {
			for (WidgetKnowledgeItem button : screen.knowledgeButtons()) {
				ItemStack stack = button.stack();

				if (stack.isEmpty() || !button.isMouseOver(mouseX, mouseY)) {
					continue;
				}

				Rect2i area = new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight());

				return ingredients.createTypedIngredient(VanillaTypes.ITEM_STACK, stack)
						.map(typed -> new RingIngredient<>(typed, area));
			}

			return Optional.empty();
		}
	}

	private record RingIngredient<V>(ITypedIngredient<V> typedIngredient, Rect2i area) implements IClickableIngredient<V> {
		@Override
		public ITypedIngredient<V> getTypedIngredient() {
			return typedIngredient;
		}

		@Override
		public Rect2i getArea() {
			return area;
		}
	}
}
