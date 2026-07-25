package dev.miguellopesdel.projectex.net;

import dev.miguellopesdel.projectex.gui.ContainerArcaneTablet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * JEI's transfer arrow, pressed over an Arcane Tablet.
 *
 * <p>What travels is the recipe's name, not its contents. The server has the same recipe book the
 * client is looking at, so sending the ingredients would be sending the server something it already
 * knows and giving a client the chance to describe a recipe that does not exist.
 */
public class PacketTabletRecipe {
	private final ResourceLocation recipe;
	private final boolean fillStacks;

	public PacketTabletRecipe(ResourceLocation recipe, boolean fillStacks) {
		this.recipe = recipe;
		this.fillStacks = fillStacks;
	}

	public PacketTabletRecipe(FriendlyByteBuf buffer) {
		recipe = buffer.readResourceLocation();
		fillStacks = buffer.readBoolean();
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(recipe);
		buffer.writeBoolean(fillStacks);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get().enqueueWork(() -> {
			ServerPlayer player = context.get().getSender();

			if (player == null || !(player.containerMenu instanceof ContainerArcaneTablet tablet)) {
				return;
			}

			Recipe<?> found = player.server.getRecipeManager().byKey(recipe).orElse(null);

			if (found instanceof CraftingRecipe crafting && crafting.canCraftInDimensions(3, 3)) {
				tablet.transferRecipe(crafting, fillStacks);
			}
		});

		context.get().setPacketHandled(true);
	}
}
