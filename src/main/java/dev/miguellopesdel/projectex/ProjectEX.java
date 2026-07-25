package dev.miguellopesdel.projectex;

import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import dev.miguellopesdel.projectex.gui.ProjectEXMenus;
import dev.miguellopesdel.projectex.item.ProjectEXItems;
import dev.miguellopesdel.projectex.net.ProjectEXNetwork;
import dev.miguellopesdel.projectex.blockentity.ProjectEXBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(ProjectEX.MOD_ID)
public class ProjectEX {
	public static final String MOD_ID = "projectex";

	public static final Direction[] DIRECTIONS = Direction.values();

	private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

	public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + MOD_ID))
			.icon(() -> new ItemStack(ProjectEXItems.PERSONAL_LINK.get()))
			.displayItems((parameters, output) -> ProjectEXItems.REGISTRY.getEntries().forEach(item -> output.accept(item.get())))
			.build());

	public ProjectEX() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		ProjectEXBlocks.REGISTRY.register(modBus);
		ProjectEXItems.REGISTRY.register(modBus);
		ProjectEXBlockEntities.REGISTRY.register(modBus);
		ProjectEXMenus.REGISTRY.register(modBus);
		TABS.register(modBus);

		ProjectEXNetwork.register();

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ProjectEXConfig.COMMON_SPEC);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ProjectEXConfig.CLIENT_SPEC);
		}
	}
}
