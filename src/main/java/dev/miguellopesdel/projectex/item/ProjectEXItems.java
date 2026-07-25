package dev.miguellopesdel.projectex.item;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ProjectEXItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ProjectEX.MOD_ID);

	// Block items
	public static final RegistryObject<Item> ENERGY_LINK = blockItem("energy_link", ProjectEXBlocks.ENERGY_LINK);
	public static final RegistryObject<Item> PERSONAL_LINK = blockItem("personal_link", ProjectEXBlocks.PERSONAL_LINK);
	public static final RegistryObject<Item> REFINED_LINK = blockItem("refined_link", ProjectEXBlocks.REFINED_LINK);
	public static final RegistryObject<Item> COMPRESSED_REFINED_LINK = blockItem("compressed_refined_link", ProjectEXBlocks.COMPRESSED_REFINED_LINK);
	public static final RegistryObject<Item> STONE_TABLE = blockItem("stone_table", ProjectEXBlocks.STONE_TABLE);

	public static final Map<Matter, RegistryObject<Item>> COLLECTOR = perMatter(matter -> blockItem(matter.id + "_collector", ProjectEXBlocks.COLLECTOR.get(matter)));
	public static final Map<Matter, RegistryObject<Item>> RELAY = perMatter(matter -> blockItem(matter.id + "_relay", ProjectEXBlocks.RELAY.get(matter)));
	public static final Map<Matter, RegistryObject<Item>> POWER_FLOWER = perMatter(matter -> blockItem(matter.id + "_power_flower", ProjectEXBlocks.POWER_FLOWER.get(matter)));

	/** Crafting intermediate: nine collectors of a tier, used to build that tier's power flower. */
	public static final Map<Matter, RegistryObject<Item>> COMPRESSED_COLLECTOR =
			perMatter(matter -> REGISTRY.register(matter.id + "_compressed_collector", () -> new ItemCompressedCollector(matter)));

	/** Only the 12 tiers made of matter have a matter item; basic, dark, red and final do not. */
	public static final Map<Matter, RegistryObject<Item>> MATTER = perMatter(matter ->
			matter.hasMatterItem ? REGISTRY.register(matter.id + "_matter", () -> new ItemMatter(matter)) : null);

	public static final RegistryObject<Item> CLAY_MATTER = REGISTRY.register("clay_matter", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> FINAL_STAR_SHARD = REGISTRY.register("final_star_shard", () -> new Item(new Item.Properties().fireResistant()));

	private ProjectEXItems() {
	}

	private static RegistryObject<Item> blockItem(String name, Supplier<? extends Block> block) {
		return REGISTRY.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
	}

	private static Map<Matter, RegistryObject<Item>> perMatter(Function<Matter, RegistryObject<Item>> factory) {
		Map<Matter, RegistryObject<Item>> map = new EnumMap<>(Matter.class);

		for (Matter matter : Matter.VALUES) {
			RegistryObject<Item> item = factory.apply(matter);

			if (item != null) {
				map.put(matter, item);
			}
		}

		return map;
	}

}
