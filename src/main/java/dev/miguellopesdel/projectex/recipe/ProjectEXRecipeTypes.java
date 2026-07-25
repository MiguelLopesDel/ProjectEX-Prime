package dev.miguellopesdel.projectex.recipe;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ProjectEXRecipeTypes {
	public static final DeferredRegister<RecipeType<?>> TYPES =
			DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ProjectEX.MOD_ID);
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ProjectEX.MOD_ID);

	public static final RegistryObject<RecipeType<AlchemyTableRecipe>> ALCHEMY_TABLE =
			TYPES.register("alchemy_table", () -> RecipeType.simple(new ResourceLocation(ProjectEX.MOD_ID, "alchemy_table")));

	public static final RegistryObject<RecipeSerializer<AlchemyTableRecipe>> ALCHEMY_TABLE_SERIALIZER =
			SERIALIZERS.register("alchemy_table", AlchemyTableRecipe.Serializer::new);

	private ProjectEXRecipeTypes() {
	}
}
