package dev.miguellopesdel.projectex.integration.jei;

import dev.miguellopesdel.projectex.gui.ContainerArcaneTablet;
import dev.miguellopesdel.projectex.gui.ProjectEXMenus;
import dev.miguellopesdel.projectex.net.PacketTabletRecipe;
import dev.miguellopesdel.projectex.net.ProjectEXNetwork;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;

import javax.annotation.Nullable;

import java.util.Optional;

/**
 * JEI's transfer arrow over the Arcane Tablet.
 *
 * <p>The tablet's grid is real slots, so JEI's own handler would work, but it would only move items
 * the player is already carrying. This one hands the recipe to the server, which fills the grid from
 * the inventory first and buys the rest out of the player's EMC.
 */
public class ArcaneTabletTransfer implements IRecipeTransferHandler<ContainerArcaneTablet, CraftingRecipe> {
	private final IRecipeTransferHandlerHelper helper;

	public ArcaneTabletTransfer(IRecipeTransferHandlerHelper helper) {
		this.helper = helper;
	}

	@Override
	public Class<? extends ContainerArcaneTablet> getContainerClass() {
		return ContainerArcaneTablet.class;
	}

	@Override
	public Optional<MenuType<ContainerArcaneTablet>> getMenuType() {
		return Optional.of(ProjectEXMenus.ARCANE_TABLET.get());
	}

	@Override
	public RecipeType<CraftingRecipe> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}

	@Override
	@Nullable
	public IRecipeTransferError transferRecipe(ContainerArcaneTablet menu, CraftingRecipe recipe,
			IRecipeSlotsView slots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!recipe.canCraftInDimensions(3, 3)) {
			return helper.createUserErrorWithTooltip(Component.translatable("gui.projectex.arcane_tablet.recipe_too_big"));
		}

		if (doTransfer) {
			// Whether the player can afford the missing ingredients is the server's to decide, so
			// nothing is checked here that would only be a guess.
			ProjectEXNetwork.CHANNEL.sendToServer(new PacketTabletRecipe(recipe.getId(), maxTransfer));
		}

		return null;
	}
}
