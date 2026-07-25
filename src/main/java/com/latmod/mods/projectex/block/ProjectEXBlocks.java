package com.latmod.mods.projectex.block;

import com.latmod.mods.projectex.Matter;
import com.latmod.mods.projectex.ProjectEX;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public final class ProjectEXBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ProjectEX.MOD_ID);

	public static final RegistryObject<Block> ENERGY_LINK = REGISTRY.register("energy_link", BlockEnergyLink::new);
	public static final RegistryObject<Block> PERSONAL_LINK = REGISTRY.register("personal_link", BlockLinkMK1::new);
	public static final RegistryObject<Block> REFINED_LINK = REGISTRY.register("refined_link", BlockLinkMK2::new);
	public static final RegistryObject<Block> COMPRESSED_REFINED_LINK = REGISTRY.register("compressed_refined_link", BlockLinkMK3::new);

	// One block per tier: block metadata is gone, so the 1.12 "collector" block with its 16
	// metadata variants becomes 16 separate blocks.
	public static final Map<Matter, RegistryObject<Block>> COLLECTOR = perMatter(matter -> REGISTRY.register(matter.id + "_collector", () -> new BlockCollector(matter)));
	public static final Map<Matter, RegistryObject<Block>> RELAY = perMatter(matter -> REGISTRY.register(matter.id + "_relay", () -> new BlockRelay(matter)));
	public static final Map<Matter, RegistryObject<Block>> POWER_FLOWER = perMatter(matter -> REGISTRY.register(matter.id + "_power_flower", () -> new BlockPowerFlower(matter)));

	public static final RegistryObject<Block> STONE_TABLE = REGISTRY.register("stone_table", BlockStoneTable::new);

	private ProjectEXBlocks() {
	}

	private static Map<Matter, RegistryObject<Block>> perMatter(Function<Matter, RegistryObject<Block>> factory) {
		Map<Matter, RegistryObject<Block>> map = new EnumMap<>(Matter.class);

		for (Matter matter : Matter.VALUES) {
			map.put(matter, factory.apply(matter));
		}

		return map;
	}
}
