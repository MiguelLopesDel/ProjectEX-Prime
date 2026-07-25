package dev.miguellopesdel.projectex.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * A button with nothing of its own to draw: the screen's texture already shows where it is, so all
 * it adds is a highlight under the cursor. The transmutation panel is made of these.
 */
public class WidgetTableButton extends AbstractWidget {
	private static final int HIGHLIGHT = 0x80FFFFFF;

	@Nullable
	private final Runnable action;

	@Nullable
	private ResourceLocation icon;
	private int iconU;
	private int iconV;

	public WidgetTableButton(int x, int y, int width, int height, @Nullable Component tooltip, @Nullable Runnable action) {
		super(x, y, width, height, tooltip == null ? Component.empty() : tooltip);
		this.action = action;

		if (tooltip != null) {
			setTooltip(Tooltip.create(tooltip));
		}
	}

	/** Draws a piece of the screen's own texture while hovered, which is how the arrows appear. */
	public WidgetTableButton hoverIcon(ResourceLocation texture, int u, int v) {
		icon = texture;
		iconU = u;
		iconV = v;
		return this;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (!isHovered()) {
			return;
		}

		if (icon == null) {
			graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, HIGHLIGHT, HIGHLIGHT);
		} else {
			graphics.blit(icon, getX(), getY(), iconU, iconV, width, height, 256, 256);
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		onPress();
	}

	protected void onPress() {
		if (action != null) {
			action.run();
		}
	}

	@Override
	public void playDownSound(SoundManager sounds) {
		// Buying an item a click is not the same as pressing a button, and the click sound on every
		// one of them turns a shopping trip into a rattle.
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
