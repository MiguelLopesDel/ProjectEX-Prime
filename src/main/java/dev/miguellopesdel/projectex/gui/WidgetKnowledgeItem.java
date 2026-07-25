package dev.miguellopesdel.projectex.gui;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * One item of the ring: what it is, and how many of it the balance would buy right now.
 *
 * <p>The number underneath is the whole point of the ring. It is read off the balance every frame,
 * so it counts down as the player spends and back up as a collector earns.
 */
public class WidgetKnowledgeItem extends WidgetTableButton {
	private static final BigInteger TEN = BigInteger.valueOf(10L);

	private final ContainerTableBase menu;
	private final Consumer<WidgetKnowledgeItem> action;

	@Nullable
	private ItemInfo type;
	private ItemStack stack = ItemStack.EMPTY;

	public WidgetKnowledgeItem(ContainerTableBase menu, int x, int y, Consumer<WidgetKnowledgeItem> action) {
		super(x, y, 16, 16, null, null);
		this.menu = menu;
		this.action = action;
	}

	@Override
	protected void onPress() {
		if (type != null) {
			action.accept(this);
		}
	}

	@Nullable
	public ItemInfo type() {
		return type;
	}

	public ItemStack stack() {
		return stack;
	}

	public void setType(@Nullable ItemInfo type) {
		this.type = type;
		stack = type == null ? ItemStack.EMPTY : type.createStack();
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (!stack.isEmpty()) {
			graphics.renderItem(stack, getX(), getY());
			graphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX(), getY(), "");
			renderAffordable(graphics);
		}

		super.renderWidget(graphics, mouseX, mouseY, partialTick);
	}

	/** Draws how many the player can afford, small, in the corner the stack size would use. */
	private void renderAffordable(GuiGraphics graphics) {
		String text = affordable();

		if (text.isEmpty()) {
			return;
		}

		var font = Minecraft.getInstance().font;

		graphics.pose().pushPose();
		graphics.pose().translate(getX() + 17.0F, getY() + 12.0F, 200.0F);
		graphics.pose().scale(0.5F, 0.5F, 1.0F);
		graphics.drawString(font, text, -font.width(text), 0, 0xFFFFFF, true);
		graphics.pose().popPose();
	}

	private String affordable() {
		if (type == null) {
			return "";
		}

		long value = EMCHelper.getEmcValue(type);

		if (value <= 0L) {
			return "";
		}

		BigInteger price = BigInteger.valueOf(value);
		BigInteger balance = menu.knowledge.getEmc();
		BigInteger whole = balance.divide(price);

		if (whole.signum() > 0) {
			return TransmutationEMCFormatter.formatEMC(whole).getString();
		}

		// Below one, a tenth of an item is still worth saying: it tells the player the collectors
		// are getting there.
		BigInteger tenths = balance.multiply(TEN).divide(price);
		return tenths.signum() > 0 ? "0." + tenths : "";
	}
}
