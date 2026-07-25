package dev.miguellopesdel.projectex.datagen;

import dev.miguellopesdel.projectex.Matter;
import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.block.ProjectEXBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

/**
 * The tier variants used to be metadata pointing at hand-written model files. Now every tier is
 * its own block, so the variant models are generated from the hand-made parent models that stay
 * in {@code src/main/resources}.
 */
public class ProjectEXBlockStates extends BlockStateProvider {
	public ProjectEXBlockStates(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, ProjectEX.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		for (Matter matter : Matter.VALUES) {
			simpleBlock(ProjectEXBlocks.COLLECTOR.get(matter).get(), models()
					.withExistingParent(matter.id + "_collector", modLoc("block/collector"))
					.texture("texture", modLoc("block/collector/" + matter.id)));

			simpleBlock(ProjectEXBlocks.RELAY.get(matter).get(), models()
					.withExistingParent(matter.id + "_relay", modLoc("block/relay"))
					.texture("texture", modLoc("block/relay/" + matter.id)));

			simpleBlock(ProjectEXBlocks.POWER_FLOWER.get(matter).get(), models()
					.withExistingParent(matter.id + "_power_flower", modLoc("block/power_flower"))
					.texture("collector", modLoc("block/collector/" + matter.id))
					.texture("relay", modLoc("block/relay/" + matter.id)));
		}

		existingModel(ProjectEXBlocks.ENERGY_LINK, "energy_link");
		existingModel(ProjectEXBlocks.PERSONAL_LINK, "personal_link");
		existingModel(ProjectEXBlocks.REFINED_LINK, "refined_link");
		existingModel(ProjectEXBlocks.COMPRESSED_REFINED_LINK, "compressed_refined_link");
		existingModel(ProjectEXBlocks.ALCHEMY_TABLE, "alchemy_table");

		stoneTable();
	}

	private void existingModel(RegistryObject<Block> block, String model) {
		simpleBlock(block.get(), models().getExistingFile(modLoc("block/" + model)));
	}

	/** The table can be placed on any face, so the single model is rotated per facing. */
	private void stoneTable() {
		ModelFile model = models().getExistingFile(modLoc("block/stone_table"));

		getVariantBuilder(ProjectEXBlocks.STONE_TABLE.get()).forAllStates(state -> {
			Direction facing = state.getValue(BlockStateProperties.FACING);

			int x = switch (facing) {
				case DOWN -> 0;
				case UP -> 180;
				default -> 90;
			};

			int y = switch (facing) {
				case WEST -> 90;
				case NORTH -> 180;
				case EAST -> 270;
				default -> 0;
			};

			return net.minecraftforge.client.model.generators.ConfiguredModel.builder()
					.modelFile(model)
					.rotationX(x)
					.rotationY(y)
					.build();
		});
	}
}
