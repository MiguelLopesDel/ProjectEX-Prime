package dev.miguellopesdel.projectex.client;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.gui.GuiAlchemyTable;
import dev.miguellopesdel.projectex.gui.GuiArcaneTablet;
import dev.miguellopesdel.projectex.gui.GuiLink;
import dev.miguellopesdel.projectex.gui.GuiStoneTable;
import dev.miguellopesdel.projectex.gui.ProjectEXMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ProjectEXClient {
	private ProjectEXClient() {
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(ProjectEXMenus.LINK.get(), GuiLink::new);
			MenuScreens.register(ProjectEXMenus.ARCANE_TABLET.get(), GuiArcaneTablet::new);
			MenuScreens.register(ProjectEXMenus.STONE_TABLE.get(), GuiStoneTable::new);
			MenuScreens.register(ProjectEXMenus.ALCHEMY_TABLE.get(), GuiAlchemyTable::new);
		});
	}
}
