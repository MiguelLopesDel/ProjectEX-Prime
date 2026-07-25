package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ProjectEXMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ProjectEX.MOD_ID);

	public static final RegistryObject<MenuType<ContainerLink>> LINK = REGISTRY.register("link",
			() -> IForgeMenuType.create((IContainerFactory<ContainerLink>) ContainerLink::new));

	public static final RegistryObject<MenuType<ContainerArcaneTablet>> ARCANE_TABLET = REGISTRY.register("arcane_tablet",
			() -> IForgeMenuType.create((IContainerFactory<ContainerArcaneTablet>) ContainerArcaneTablet::new));

	private ProjectEXMenus() {
	}
}
