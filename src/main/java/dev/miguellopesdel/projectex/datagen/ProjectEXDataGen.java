package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ProjectEXDataGen {
	private ProjectEXDataGen() {
	}

	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();

		generator.addProvider(event.includeClient(), new ProjectEXBlockStates(output, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new ProjectEXItemModels(output, event.getExistingFileHelper()));
		generator.addProvider(event.includeClient(), new ProjectEXLanguage(output));
		generator.addProvider(event.includeServer(), new ProjectEXLootTables(output));
		generator.addProvider(event.includeServer(), new ProjectEXRecipes(output));
	}
}
