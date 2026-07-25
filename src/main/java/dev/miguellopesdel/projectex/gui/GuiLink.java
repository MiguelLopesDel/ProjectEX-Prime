package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GuiLink extends AbstractContainerScreen<ContainerLink> {
	private static final ResourceLocation PERSONAL = texture("personal_link");
	private static final ResourceLocation REFINED = texture("refined_link");
	private static final ResourceLocation COMPRESSED_REFINED = texture("compressed_refined_link");

	private final ResourceLocation background;

	public GuiLink(ContainerLink menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);

		int inputs = menu.link.items().inputCount();
		int outputs = menu.link.items().getSlots() - inputs;

		if (inputs > 1) {
			background = PERSONAL;
			imageHeight = 166;
		} else if (outputs > 9) {
			background = COMPRESSED_REFINED;
			imageHeight = 244;
		} else {
			background = REFINED;
			imageHeight = 166;
		}

		inventoryLabelY = imageHeight - 94;
	}

	private static ResourceLocation texture(String name) {
		return new ResourceLocation(ProjectEX.MOD_ID, "textures/gui/" + name + ".png");
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTemplates(graphics);
		renderTooltip(graphics, mouseX, mouseY);
	}

	/**
	 * Draws the template of every output slot that currently has nothing in it, dimmed, so a
	 * slot still says what it makes when the owner cannot afford a single one.
	 */
	private void renderTemplates(GuiGraphics graphics) {
		int inputs = menu.link.items().inputCount();
		int outputs = menu.link.items().outputCount();

		for (int i = 0; i < outputs; i++) {
			Slot slot = menu.slots.get(inputs + i);
			ItemStack template = menu.link.items().getTemplate(i);

			if (slot.hasItem() || template.isEmpty()) {
				continue;
			}

			int x = leftPos + slot.x;
			int y = topPos + slot.y;

			graphics.renderItem(template, x, y);
			graphics.fill(x, y, x + 16, y + 16, 0x80101010);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, menu.link.ownerBuffer.ownerName, titleLabelX, titleLabelY, 0x404040, false);
		graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
	}
}
