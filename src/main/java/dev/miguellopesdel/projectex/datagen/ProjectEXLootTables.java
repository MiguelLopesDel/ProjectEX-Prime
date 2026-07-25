package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Every block simply drops itself; in 1.12 that was the default behaviour of Block.
 */
public class ProjectEXLootTables extends LootTableProvider {
	public ProjectEXLootTables(PackOutput output) {
		super(output, Set.of(), List.of(new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)));
	}

	private static class Blocks extends BlockLootSubProvider {
		private Blocks() {
			super(Set.of(), FeatureFlags.REGISTRY.allFlags());
		}

		@Override
		protected void generate() {
			for (Block block : getKnownBlocks()) {
				dropSelf(block);
			}
		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return ProjectEXBlocks.REGISTRY.getEntries().stream()
					.map(RegistryObject::get)
					.collect(Collectors.toList());
		}
	}
}
