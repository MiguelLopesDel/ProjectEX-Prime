package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEX;
import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.client.EnumSearchType;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiArcaneTablet extends GuiTableBase<ContainerArcaneTablet> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectEX.MOD_ID, "textures/gui/arcane_tablet.png");

	/** Where the ring's twelve items sit, as the 1.12 screen placed them. */
	private static final int[][] RING = {
			{80, 20}, {105, 26}, {55, 26}, {123, 44}, {37, 44}, {128, 68},
			{32, 68}, {123, 92}, {37, 92}, {105, 110}, {55, 110}, {80, 116}
	};

	/** The crafting side panel, drawn to the left of the screen proper. */
	private static final int PANEL_X = -75;
	private static final int PANEL_Y = 10;
	private static final int PANEL_WIDTH = 76;
	private static final int PANEL_HEIGHT = 89;

	private WidgetTableButton searchTypeButton;

	public GuiArcaneTablet(ContainerArcaneTablet menu, Inventory playerInventory, Component title) {
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

		// Shift over the burn slot charges or drains what is on the cursor instead of selling it.
		addRenderableWidget(new WidgetTableButton(leftPos + 80, topPos + 68, 16, 16, translate("burn"),
				() -> send(hasShiftDown() ? ContainerTableBase.BURN_ALT : ContainerTableBase.BURN, null)));

		addActionButton(leftPos + 8, topPos + 115, 16, 16, translate("learn"), ContainerTableBase.LEARN);
		addActionButton(leftPos + 152, topPos + 115, 16, 16, translate("unlearn"), ContainerTableBase.UNLEARN);

		for (int[] position : RING) {
			addItemButton(leftPos + position[0], topPos + position[1]);
		}

		addPanelButton(16, translate("rotate"), () -> sendMenuButton(
				hasShiftDown() ? ContainerArcaneTablet.ROTATE_ANTICLOCKWISE : ContainerArcaneTablet.ROTATE_CLOCKWISE));
		addPanelButton(26, translate("balance"), () -> sendMenuButton(
				hasShiftDown() ? ContainerArcaneTablet.SPREAD : ContainerArcaneTablet.BALANCE));
		searchTypeButton = addPanelButton(36, searchTypeLabel(), this::cycleSearchType);
		addPanelButton(61, translate("clear"), () -> sendMenuButton(ContainerArcaneTablet.CLEAR));
	}

	private WidgetTableButton addPanelButton(int y, Component tooltip, Runnable action) {
		return addRenderableWidget(new WidgetTableButton(leftPos - 71, topPos + y, 9, 9, tooltip, action));
	}

	private static Component translate(String name) {
		return Component.translatable("gui.projectex.arcane_tablet." + name);
	}

	private void cycleSearchType() {
		EnumSearchType next = EnumSearchType.VALUES[(searchType().ordinal() + 1) % EnumSearchType.VALUES.length];
		ProjectEXConfig.CLIENT.searchType.set(next);
		ProjectEXConfig.CLIENT_SPEC.save();
		searchTypeButton.setTooltip(Tooltip.create(searchTypeLabel()));
	}

	private Component searchTypeLabel() {
		return Component.translatable("projectex.general.search_type")
				.append(": ")
				.append(Component.translatable(searchType().translationKey).withStyle(ChatFormatting.GRAY));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		super.renderBg(graphics, partialTick, mouseX, mouseY);
		graphics.blit(TEXTURE, leftPos + PANEL_X, topPos + PANEL_Y, 180, 19, PANEL_WIDTH, PANEL_HEIGHT, 256, 256);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// The balance goes above the screen rather than inside it: there is no room anywhere in the
		// ring, and it belongs to the player rather than to this tablet.
		String balance = TransmutationEMCFormatter.formatEMC(menu.knowledge.getEmc()).getString();
		graphics.drawString(font, balance, (imageWidth - font.width(balance)) / 2, -9, 0xB5B5B5, true);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
		// The crafting grid hangs off the left edge, so clicks there are inside the screen even
		// though they are outside its background.
		if (mouseX >= guiLeft + PANEL_X && mouseX < guiLeft + PANEL_X + PANEL_WIDTH
				&& mouseY >= guiTop + PANEL_Y && mouseY < guiTop + PANEL_Y + PANEL_HEIGHT) {
			return false;
		}

		return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
	}
}
