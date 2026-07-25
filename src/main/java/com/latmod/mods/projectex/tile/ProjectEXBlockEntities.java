package com.latmod.mods.projectex.tile;

import com.latmod.mods.projectex.ProjectEX;
import com.latmod.mods.projectex.block.ProjectEXBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ProjectEXBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ProjectEX.MOD_ID);

	public static final RegistryObject<BlockEntityType<TileEnergyLink>> ENERGY_LINK =
			REGISTRY.register("energy_link", () -> BlockEntityType.Builder.of(TileEnergyLink::new, ProjectEXBlocks.ENERGY_LINK.get()).build(null));
	public static final RegistryObject<BlockEntityType<TileLinkMK1>> PERSONAL_LINK =
			REGISTRY.register("personal_link", () -> BlockEntityType.Builder.of(TileLinkMK1::new, ProjectEXBlocks.PERSONAL_LINK.get()).build(null));
	public static final RegistryObject<BlockEntityType<TileLinkMK2>> REFINED_LINK =
			REGISTRY.register("refined_link", () -> BlockEntityType.Builder.of(TileLinkMK2::new, ProjectEXBlocks.REFINED_LINK.get()).build(null));
	public static final RegistryObject<BlockEntityType<TileLinkMK3>> COMPRESSED_REFINED_LINK =
			REGISTRY.register("compressed_refined_link", () -> BlockEntityType.Builder.of(TileLinkMK3::new, ProjectEXBlocks.COMPRESSED_REFINED_LINK.get()).build(null));

	// One block entity type shared by all 16 tiers of each machine.
	public static final RegistryObject<BlockEntityType<TileCollector>> COLLECTOR =
			REGISTRY.register("collector", () -> BlockEntityType.Builder.of(TileCollector::new, blocks(ProjectEXBlocks.COLLECTOR.values())).build(null));
	public static final RegistryObject<BlockEntityType<TileRelay>> RELAY =
			REGISTRY.register("relay", () -> BlockEntityType.Builder.of(TileRelay::new, blocks(ProjectEXBlocks.RELAY.values())).build(null));
	public static final RegistryObject<BlockEntityType<TilePowerFlower>> POWER_FLOWER =
			REGISTRY.register("power_flower", () -> BlockEntityType.Builder.of(TilePowerFlower::new, blocks(ProjectEXBlocks.POWER_FLOWER.values())).build(null));

	private ProjectEXBlockEntities() {
	}

	private static Block[] blocks(Iterable<? extends Supplier<Block>> suppliers) {
		java.util.List<Block> list = new java.util.ArrayList<>();

		for (Supplier<Block> supplier : suppliers) {
			list.add(supplier.get());
		}

		return list.toArray(new Block[0]);
	}
}
