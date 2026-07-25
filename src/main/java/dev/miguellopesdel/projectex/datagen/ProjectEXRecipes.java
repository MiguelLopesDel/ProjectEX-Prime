package dev.miguellopesdel.projectex.datagen;

import com.google.gson.JsonObject;
import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import dev.miguellopesdel.projectex.recipe.ProjectEXRecipeTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * In 1.12 these recipes were built in code at registry time; they are data now, so the same
 * chains are emitted from here instead.
 */
public class ProjectEXRecipes extends RecipeProvider {
	public ProjectEXRecipes(PackOutput output) {
		super(output);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		links(consumer);
		stoneTable(consumer);
		matter(consumer);
		machines(consumer);
		stars(consumer);
		arcaneTablet(consumer);
		alchemyTable(consumer);
		alchemyChains(consumer);
	}

	private void alchemyTable(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.ALCHEMY_TABLE.get())
				.pattern("123")
				.pattern("TST")
				.pattern("LDL")
				.define('1', PEItems.LOW_COVALENCE_DUST.get())
				.define('2', PEItems.MEDIUM_COVALENCE_DUST.get())
				.define('3', PEItems.HIGH_COVALENCE_DUST.get())
				.define('S', ProjectEXBlocks.STONE_TABLE.get())
				.define('L', Tags.Items.RODS_WOODEN)
				.define('D', Tags.Items.GEMS_DIAMOND)
				.define('T', Items.TORCH)
				.unlockedBy("has_stone_table", has(ProjectEXBlocks.STONE_TABLE.get()))
				.save(consumer);
	}

	/**
	 * The chains the Alchemy Table can walk, carried over from 1.12 where they were a hardcoded
	 * list. They are ordinary recipes now, so a pack adds, removes or reprices them like any other.
	 */
	private void alchemyChains(Consumer<FinishedRecipe> consumer) {
		chain(consumer, Items.CHARCOAL, Items.COAL);
		chain(consumer, Items.REDSTONE, Items.GUNPOWDER, Items.GLOWSTONE_DUST, Items.BLAZE_POWDER, Items.BLAZE_ROD);
		chain(consumer, Items.LAPIS_LAZULI, Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS);
		chain(consumer, PEItems.LOW_COVALENCE_DUST.get(), PEItems.MEDIUM_COVALENCE_DUST.get(), PEItems.HIGH_COVALENCE_DUST.get());
		chain(consumer, Items.BEEF, Items.ROTTEN_FLESH, Items.LEATHER, Items.SPIDER_EYE, Items.BONE);
		chain(consumer, Items.WHEAT_SEEDS, Items.MELON_SLICE, Items.APPLE, Items.CARROT, Items.BEETROOT, Items.POTATO, Items.PUMPKIN);
		chain(consumer, Items.COOKIE, Items.BREAD, Items.CAKE);
		chain(consumer, PEItems.ALCHEMICAL_COAL.get(), Items.REDSTONE_BLOCK, Items.LAVA_BUCKET, Items.OBSIDIAN);
		chain(consumer, Items.OAK_LEAVES, Items.GRASS, Items.FERN, Items.VINE, Items.LILY_PAD);

		step(consumer, Items.ENDER_EYE, Items.CHORUS_FRUIT);
		step(consumer, Items.STRING, Items.FEATHER);
		step(consumer, Items.STICK, Items.DEAD_BUSH);
	}

	private void chain(Consumer<FinishedRecipe> consumer, ItemLike... steps) {
		for (int i = 1; i < steps.length; i++) {
			step(consumer, steps[i - 1], steps[i]);
		}
	}

	private void step(Consumer<FinishedRecipe> consumer, ItemLike from, ItemLike to) {
		consumer.accept(new AlchemyStep(id("alchemy/" + path(from) + "_to_" + path(to)), Ingredient.of(from), new ItemStack(to)));
	}

	private static String path(ItemLike item) {
		return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
	}

	/**
	 * Writes one alchemy step out. There is no advancement: the table is the recipe book for its
	 * own chains, and a toast for each of the thirty steps would be noise.
	 */
	private record AlchemyStep(ResourceLocation id, Ingredient input, ItemStack output) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("ingredient", input.toJson());

			JsonObject result = new JsonObject();
			result.addProperty("item", ForgeRegistries.ITEMS.getKey(output.getItem()).toString());

			if (output.getCount() > 1) {
				result.addProperty("count", output.getCount());
			}

			json.add("result", result);
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return ProjectEXRecipeTypes.ALCHEMY_TABLE_SERIALIZER.get();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}

	/**
	 * A crafting table and a chest folded into four Stone Tables, around a star to pay for what it
	 * makes. Either kind of transmutation matter works as the binding, the same as in 1.12.
	 */
	private void arcaneTablet(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXItems.ARCANE_TABLET.get())
				.pattern("TWT")
				.pattern("MSM")
				.pattern("TCT")
				.define('T', ProjectEXBlocks.STONE_TABLE.get())
				.define('W', Items.CRAFTING_TABLE)
				.define('C', Tags.Items.CHESTS_WOODEN)
				.define('M', Ingredient.of(ProjectEXItems.MATTER.get(Matter.MAGENTA).get(), PEItems.TRANSMUTATION_TABLET.get()))
				.define('S', ProjectEXItems.MAGNUM_STAR.get(0).get())
				.unlockedBy("has_stone_table", has(ProjectEXBlocks.STONE_TABLE.get()))
				.save(consumer);
	}

	/**
	 * Every star is four of the star below it, so the ladder is one loop: the first Magnum is
	 * four of ProjectE's Omega Klein Stars, and the first Colossal is four Magnum Omegas.
	 */
	private void stars(Consumer<FinishedRecipe> consumer) {
		Item previous = PEItems.KLEIN_STAR_OMEGA.get();

		for (List<RegistryObject<Item>> ladder : List.of(ProjectEXItems.MAGNUM_STAR, ProjectEXItems.COLOSSAL_STAR)) {
			for (RegistryObject<Item> star : ladder) {
				Item result = star.get();
				Item ingredient = previous;

				ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
						.requires(ingredient, 4)
						.group("projectex:star")
						.unlockedBy("has_previous_star", has(ingredient))
						.save(consumer, id("star/" + star.getId().getPath()));

				previous = result;
			}
		}

		// Eight of the last power flower there is, around a dragon egg.
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXItems.FINAL_STAR.get())
				.pattern("FFF")
				.pattern("FEF")
				.pattern("FFF")
				.define('F', ProjectEXBlocks.POWER_FLOWER.get(Matter.FINAL).get())
				.define('E', Items.DRAGON_EGG)
				.group("projectex:star")
				.unlockedBy("has_final_power_flower", has(ProjectEXBlocks.POWER_FLOWER.get(Matter.FINAL).get()))
				.save(consumer);
	}

	private void links(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.ENERGY_LINK.get())
				.pattern("LMH")
				.pattern("SRS")
				.pattern("HML")
				.define('S', Tags.Items.STONE)
				.define('R', PEItems.RED_MATTER.get())
				.define('L', PEItems.LOW_COVALENCE_DUST.get())
				.define('M', PEItems.MEDIUM_COVALENCE_DUST.get())
				.define('H', PEItems.HIGH_COVALENCE_DUST.get())
				.group("projectex:link")
				.unlockedBy("has_red_matter", has(PEItems.RED_MATTER.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.PERSONAL_LINK.get())
				.pattern("RBR")
				.pattern("BCB")
				.pattern("RBR")
				.define('B', ProjectEXBlocks.ENERGY_LINK.get())
				.define('R', PEBlocks.RED_MATTER.asItem())
				.define('C', PEBlocks.CONDENSER_MK2.asItem())
				.group("projectex:link")
				.unlockedBy("has_energy_link", has(ProjectEXBlocks.ENERGY_LINK.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.REFINED_LINK.get())
				.pattern("LLL")
				.pattern("LLL")
				.pattern("LLL")
				.define('L', ProjectEXBlocks.PERSONAL_LINK.get())
				.group("projectex:link")
				.unlockedBy("has_personal_link", has(ProjectEXBlocks.PERSONAL_LINK.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.COMPRESSED_REFINED_LINK.get())
				.pattern("LLL")
				.pattern("LLL")
				.define('L', ProjectEXBlocks.REFINED_LINK.get())
				.group("projectex:link")
				.unlockedBy("has_refined_link", has(ProjectEXBlocks.REFINED_LINK.get()))
				.save(consumer);
	}

	private void stoneTable(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.STONE_TABLE.get())
				.pattern("SSS")
				.pattern("STS")
				.pattern("SSS")
				.define('S', Ingredient.of(Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS))
				.define('T', Ingredient.of(PEBlocks.TRANSMUTATION_TABLE.asItem(), PEItems.PHILOSOPHERS_STONE.get()))
				.group("projectex:stone_table")
				.unlockedBy("has_philosophers_stone", has(PEItems.PHILOSOPHERS_STONE.get()))
				.save(consumer);
	}

	/**
	 * Matter is crafted from the previous tier surrounded by aeternalis fuel, in two layouts so
	 * the recipe works whichever way round the player builds it.
	 */
	private void matter(Consumer<FinishedRecipe> consumer) {
		Item fuel = PEItems.AETERNALIS_FUEL.get();

		for (Matter matter : Matter.VALUES) {
			if (!matter.hasMatterItem) {
				continue;
			}

			Matter previous = matter.getPrevious();
			Item result = ProjectEXItems.MATTER.get(matter).get();
			Item input = previous.hasMatterItem ? ProjectEXItems.MATTER.get(previous).get() : previous.getCraftingItem().get();

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
					.pattern("FFF")
					.pattern("MMM")
					.pattern("FFF")
					.define('F', fuel)
					.define('M', input)
					.group("projectex:matter")
					.unlockedBy("has_previous_matter", has(input))
					.save(consumer, id("matter/" + matter.id + "_h"));

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
					.pattern("FMF")
					.pattern("FMF")
					.pattern("FMF")
					.define('F', fuel)
					.define('M', input)
					.group("projectex:matter")
					.unlockedBy("has_previous_matter", has(input))
					.save(consumer, id("matter/" + matter.id + "_v"));
		}
	}

	private void machines(Consumer<FinishedRecipe> consumer) {
		for (Matter matter : Matter.VALUES) {
			ItemLike collector = ProjectEXBlocks.COLLECTOR.get(matter).get();
			ItemLike relay = ProjectEXBlocks.RELAY.get(matter).get();
			ItemLike compressed = ProjectEXItems.COMPRESSED_COLLECTOR.get(matter).get();

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, compressed)
					.pattern("CCC")
					.pattern("CCC")
					.pattern("CCC")
					.define('C', collector)
					.group("projectex:compressed_collector")
					.unlockedBy("has_collector", has(collector))
					.save(consumer, id("compressed_collector/" + matter.id));

			ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ProjectEXBlocks.POWER_FLOWER.get(matter).get())
					.pattern("CEC")
					.pattern("RRR")
					.pattern("RRR")
					.define('C', compressed)
					.define('E', ProjectEXBlocks.ENERGY_LINK.get())
					.define('R', relay)
					.group("projectex:power_flower")
					.unlockedBy("has_compressed_collector", has(compressed))
					.save(consumer, id("power_flower/" + matter.id));

			// The first tier is crafted from its own material rather than upgraded from a
			// previous tier, and that recipe is part of the item phase of the port.
			Matter previous = matter.getPrevious();

			if (previous == null) {
				continue;
			}

			Item upgrade = matter.getCraftingItem().get();

			ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, collector)
					.requires(ProjectEXBlocks.COLLECTOR.get(previous).get())
					.requires(upgrade)
					.group("projectex:collector")
					.unlockedBy("has_previous_collector", has(ProjectEXBlocks.COLLECTOR.get(previous).get()))
					.save(consumer, id("collector/" + matter.id));

			ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, relay)
					.requires(ProjectEXBlocks.RELAY.get(previous).get())
					.requires(upgrade)
					.group("projectex:relay")
					.unlockedBy("has_previous_relay", has(ProjectEXBlocks.RELAY.get(previous).get()))
					.save(consumer, id("relay/" + matter.id));
		}
	}

	private static ResourceLocation id(String path) {
		return new ResourceLocation(ProjectEX.MOD_ID, path);
	}
}
