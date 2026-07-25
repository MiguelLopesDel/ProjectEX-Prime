package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.blockentity.PersistentItems;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GuiStoneTable extends GuiTableBase<ContainerStoneTable> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectEX.MOD_ID, "textures/gui/stone_table.png");

	/** Where the ring's eight items sit, as the 1.12 screen placed them. */
	private static final int[][] RING = {
			{80, 28}, {110, 38}, {50, 38}, {120, 68}, {40, 68}, {110, 98}, {50, 98}, {80, 108}
	};

	public GuiStoneTable(ContainerStoneTable menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		imageWidth = 176;
		imageHeight = 217;
	}

	@Override
	protected ResourceLocation texture() {
		return TEXTURE;
	}

	@Override
	protected Rect2i searchBox() {
		return new Rect2i(leftPos + 8, topPos + 7, 160, 11);
	}

	@Override
	protected void addButtons() {
		addPageButtons(leftPos + 7, topPos + 20, leftPos + 151, topPos + 20, 196, 215);
		addPanelButtons(leftPos + 80, topPos + 68, leftPos + 8, topPos + 115, leftPos + 152, topPos + 115);

		for (int[] position : RING) {
			addItemButton(leftPos + position[0], topPos + position[1]);
		}
	}

	/**
	 * Says why an item is not in the ring. Without this the whitelist is invisible: a player holding
	 * something worth EMC over a table that refuses it has no way to tell that from a bug.
	 */
	@Override
	protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
		List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));

		if (EMCHelper.doesItemHaveEmc(stack) && !menu.isItemValid(PersistentItems.infoOf(stack))) {
			tooltip.add(Component.translatable("gui.projectex.stone_table.cant_use").withStyle(ChatFormatting.RED));
		}

		return tooltip;
	}
}
