package dev.miguellopesdel.projectex.recipe;

import com.google.common.math.LongMath;
import com.google.gson.JsonObject;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * One step of an alchemy chain: an item goes in, a different item comes out, and EMC pays for the
 * difference.
 *
 * <p>1.12 kept this list in Java and a pack could not touch it. It is a recipe type here, so the
 * chains ship as data and are changed the way any other recipe is.
 *
 * <p>Cost and duration are derived unless the recipe says otherwise. Deriving the cost from what
 * goes in and what comes out means a chain stays sensibly priced when a pack changes EMC values,
 * which a fixed number written into the recipe would not.
 */
public class AlchemyTableRecipe implements Recipe<Container> {
	public static final int DEFAULT_DURATION = 200;

	/** Cheapest a step can be, so that turning dirt into dirt still takes something. */
	private static final long MINIMUM_COST = 64L;

	private final ResourceLocation id;
	private final Ingredient input;
	private final ItemStack output;
	private final long cost;
	private final int duration;

	public AlchemyTableRecipe(ResourceLocation id, Ingredient input, ItemStack output, long cost, int duration) {
		this.id = id;
		this.input = input;
		this.output = output;
		this.cost = cost;
		this.duration = duration;
	}

	public Ingredient input() {
		return input;
	}

	public ItemStack output() {
		return output;
	}

	public int duration() {
		return duration > 0 ? duration : DEFAULT_DURATION;
	}

	/** What one step costs, priced from the stack actually in the slot. */
	public long cost(ItemStack input) {
		if (cost > 0L) {
			return cost;
		}

		// Saturating: wrapping would come out negative, and the floor below would then price a step
		// between two absurdly valuable items at the cheapest a step can be.
		long both = LongMath.saturatedAdd(EMCHelper.getEmcValue(input), EMCHelper.getEmcValue(output));
		return Math.max(MINIMUM_COST, LongMath.saturatedMultiply(both, 3L));
	}

	@Override
	public boolean matches(Container container, Level level) {
		return input.test(container.getItem(0));
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess registries) {
		return output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registries) {
		return output;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ProjectEXRecipeTypes.ALCHEMY_TABLE_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ProjectEXRecipeTypes.ALCHEMY_TABLE.get();
	}

	public static class Serializer implements RecipeSerializer<AlchemyTableRecipe> {
		@Override
		public AlchemyTableRecipe fromJson(ResourceLocation id, JsonObject json) {
			return new AlchemyTableRecipe(id,
					Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient")),
					CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), false),
					GsonHelper.getAsLong(json, "emc", 0L),
					GsonHelper.getAsInt(json, "duration", 0));
		}

		@Override
		public AlchemyTableRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
			return new AlchemyTableRecipe(id, Ingredient.fromNetwork(buffer), buffer.readItem(),
					buffer.readVarLong(), buffer.readVarInt());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlchemyTableRecipe recipe) {
			recipe.input.toNetwork(buffer);
			buffer.writeItem(recipe.output);
			buffer.writeVarLong(recipe.cost);
			buffer.writeVarInt(recipe.duration);
		}
	}
}
