package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.blockentity.ProjectEXBlockEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Keeps power flowers out of the Watch of Flowing Time.
 *
 * <p>The watch ticks nearby block entities extra times, which on a block that produces EMC on a
 * timer means free EMC. 1.12 had a config option for this; in 1.20.1 ProjectE decides what the
 * watch skips from a tag, so this adds to that tag instead. A pack that wants the watch to work on
 * power flowers after all removes them from it, and one that wants collectors and relays left alone
 * too can add those.
 */
public class ProjectEXBlockEntityTypeTags extends TagsProvider<BlockEntityType<?>> {
	private static final TagKey<BlockEntityType<?>> TIME_WATCH_BLACKLIST =
			TagKey.create(Registries.BLOCK_ENTITY_TYPE, new ResourceLocation("projecte", "blacklist/time_watch"));

	public ProjectEXBlockEntityTypeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFileHelper) {
		super(output, Registries.BLOCK_ENTITY_TYPE, lookup, ProjectEX.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(TIME_WATCH_BLACKLIST).add(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, ProjectEXBlockEntities.POWER_FLOWER.getId()));
	}
}
