package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiAlchemyTable extends AbstractContainerScreen<ContainerAlchemyTable> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectEX.MOD_ID, "textures/gui/alchemy_table.png");

	/** The EMC bar sits behind the arrow and is a little larger, so it reads as a backing. */
	private static final int CHARGE_X = 77;
	private static final int CHARGE_Y = 34;
	private static final int CHARGE_WIDTH = 24;
	private static final int CHARGE_HEIGHT = 18;

	private static final int ARROW_X = 78;
	private static final int ARROW_Y = 35;
	private static final int ARROW_WIDTH = 22;
	private static final int ARROW_HEIGHT = 16;

	public GuiAlchemyTable(ContainerAlchemyTable menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

		int charge = menu.chargeFill();

		if (charge > 0) {
			graphics.blit(TEXTURE, leftPos + CHARGE_X, topPos + CHARGE_Y, 177, 17,
					scaled(charge, CHARGE_WIDTH), CHARGE_HEIGHT, 256, 256);
		}

		int progress = menu.progressFill();

		if (progress > 0) {
			graphics.blit(TEXTURE, leftPos + ARROW_X, topPos + ARROW_Y, 177, 0,
					scaled(progress, ARROW_WIDTH), ARROW_HEIGHT, 256, 256);
		}
	}

	/** Always at least a pixel, so that "started" never looks the same as "idle". */
	private static int scaled(int fill, int width) {
		return Math.max(1, fill * width / ContainerAlchemyTable.BAR_STEPS);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}
}
