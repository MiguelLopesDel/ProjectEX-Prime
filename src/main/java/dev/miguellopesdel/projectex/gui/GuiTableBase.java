package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.client.EnumSearchType;
import dev.miguellopesdel.projectex.net.PacketTableAction;
import dev.miguellopesdel.projectex.net.ProjectEXNetwork;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.TransmutationEMCFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The transmutation panel: a search box over a ring of everything the player knows, and the buttons
 * that learn, unlearn and sell.
 *
 * <p>Nothing here is authoritative. The ring is built from the knowledge and the balance that
 * ProjectE already syncs to the client, and pressing anything sends a request; what comes back is
 * the server's answer. The search text and the page survive the screen closing, because looking the
 * same item up again after every craft is the one thing the 1.12 screen got right and no one
 * noticed.
 */
public abstract class GuiTableBase<T extends ContainerTableBase> extends AbstractContainerScreen<T> {
	/** Most expensive first, the way a transmutation table reads. */
	private static final Comparator<ItemInfo> BY_VALUE =
			Comparator.comparingLong((ItemInfo item) -> EMCHelper.getEmcValue(item)).reversed().thenComparing(ItemInfo::toString);

	private static String search = "";
	private static int page;

	private final List<ItemInfo> matches = new ArrayList<>();
	private final List<WidgetKnowledgeItem> itemButtons = new ArrayList<>();

	private EditBox searchField;
	private int knownCount = -1;

	protected GuiTableBase(T menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	protected abstract Rect2i searchBox();

	protected abstract ResourceLocation texture();

	/** Adds the panel's buttons, including the ring, at whatever positions the screen uses. */
	protected abstract void addButtons();

	@Override
	protected void init() {
		super.init();
		itemButtons.clear();

		Rect2i box = searchBox();
		searchField = new EditBox(font, box.getX(), box.getY(), box.getWidth(), box.getHeight(), Component.empty());
		searchField.setMaxLength(35);
		searchField.setBordered(false);
		searchField.setTextColor(0xFFFFFF);
		searchField.setValue(search);
		addRenderableWidget(searchField);

		if (searchType().autoselected) {
			setInitialFocus(searchField);
		}

		addButtons();
		refreshMatches();
	}

	protected EnumSearchType searchType() {
		return ProjectEXConfig.CLIENT.searchType.get();
	}

	/** Adds one slot of the ring. Pressing it buys one, or a whole stack with shift held. */
	protected WidgetKnowledgeItem addItemButton(int x, int y) {
		WidgetKnowledgeItem button = new WidgetKnowledgeItem(menu, x, y, pressed ->
				send(hasShiftDown() ? ContainerTableBase.TAKE_STACK : ContainerTableBase.TAKE_ONE, pressed.type()));

		itemButtons.add(button);
		return addRenderableWidget(button);
	}

	protected WidgetTableButton addActionButton(int x, int y, int width, int height, @Nullable Component tooltip, int mode) {
		return addRenderableWidget(new WidgetTableButton(x, y, width, height, tooltip, () -> send(mode, null)));
	}

	/** Labels of the buttons every transmutation panel has, wherever the screen puts them. */
	protected static Component tableText(String name) {
		return Component.translatable("gui.projectex.table." + name);
	}

	/** The burn slot, and the learn and unlearn buttons, which sit differently on each screen. */
	protected void addPanelButtons(int burnX, int burnY, int learnX, int learnY, int unlearnX, int unlearnY) {
		// Shift over the burn slot charges or drains what is on the cursor instead of selling it.
		addRenderableWidget(new WidgetTableButton(burnX, burnY, 16, 16, tableText("burn"),
				() -> send(hasShiftDown() ? ContainerTableBase.BURN_ALT : ContainerTableBase.BURN, null)));

		addActionButton(learnX, learnY, 16, 16, tableText("learn"), ContainerTableBase.LEARN);
		addActionButton(unlearnX, unlearnY, 16, 16, tableText("unlearn"), ContainerTableBase.UNLEARN);
	}

	protected void send(int mode, @Nullable ItemInfo type) {
		ProjectEXNetwork.CHANNEL.sendToServer(new PacketTableAction(mode, type));
	}

	protected void sendMenuButton(int id) {
		Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, id);
	}

	/** Rebuilds the list of items the search matches. */
	private void refreshMatches() {
		matches.clear();

		String query = normalise(search);
		boolean byMod = query.startsWith("@");

		if (byMod) {
			query = query.substring(1);
		}

		for (ItemInfo item : menu.knowledge.getKnowledge()) {
			if (item == null || !menu.isItemValid(item) || !matchesSearch(item, query, byMod)) {
				continue;
			}

			matches.add(item);
		}

		matches.sort(BY_VALUE);
		knownCount = menu.knowledge.getKnowledge().size();
		refreshPage();
	}

	private static boolean matchesSearch(ItemInfo item, String query, boolean byMod) {
		if (query.isEmpty()) {
			return true;
		}

		if (byMod) {
			return BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace().startsWith(query);
		}

		return normalise(item.createStack().getHoverName().getString()).contains(query);
	}

	private void refreshPage() {
		int perPage = itemButtons.size();

		for (int i = 0; i < perPage; i++) {
			int index = i + page * perPage;
			itemButtons.get(i).setType(index < matches.size() ? matches.get(index) : null);
		}
	}

	private static String normalise(String text) {
		String stripped = ChatFormatting.stripFormatting(text.trim());
		return stripped == null ? "" : stripped.toLowerCase(Locale.ROOT);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		searchField.tick();

		if (!search.equals(searchField.getValue())) {
			search = searchField.getValue();
			page = 0;
			refreshMatches();
		} else if (knownCount != menu.knowledge.getKnowledge().size()) {
			// Learning or forgetting something happens on the server, so the ring finds out the
			// same way the player does: the knowledge it is drawn from changed underneath it.
			page = 0;
			refreshMatches();
		}
	}

	private void changePage(boolean next) {
		int perPage = itemButtons.size();

		if (perPage == 0) {
			return;
		}

		int pages = Mth.ceil(matches.size() / (float) perPage);

		if (next && page < pages - 1) {
			page++;
		} else if (!next && page > 0) {
			page--;
		} else {
			return;
		}

		refreshPage();
	}

	protected void addPageButtons(int previousX, int previousY, int nextX, int nextY, int previousU, int nextU) {
		addRenderableWidget(new WidgetTableButton(previousX, previousY, 18, 18, null, () -> changePage(false))
				.hoverIcon(texture(), previousU, 0));
		addRenderableWidget(new WidgetTableButton(nextX, nextY, 18, 18, null, () -> changePage(true))
				.hoverIcon(texture(), nextU, 0));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (delta != 0.0D) {
			changePage(delta < 0.0D);
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOverSearchField(mouseX, mouseY)) {
			// Right click empties the box, and clicking it while holding something searches for
			// that, which is faster than typing the name of the thing already in your hand.
			if (button == 1) {
				searchField.setValue("");
			} else if (!menu.getCarried().isEmpty()) {
				searchField.setValue(normalise(menu.getCarried().getHoverName().getString()));
			}

			searchField.setFocused(true);
			setFocused(searchField);
			return true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean isOverSearchField(double mouseX, double mouseY) {
		Rect2i box = searchBox();
		return mouseX >= box.getX() && mouseX < box.getX() + box.getWidth()
				&& mouseY >= box.getY() && mouseY < box.getY() + box.getHeight();
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		// Typing "e" into the search box must not close the screen, which is what the inventory key
		// would otherwise do before the box ever sees the character.
		if (searchField.isFocused() && key != GLFW.GLFW_KEY_ESCAPE) {
			searchField.keyPressed(key, scanCode, modifiers);
			return true;
		}

		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char character, int modifiers) {
		return searchField.isFocused() ? searchField.charTyped(character, modifiers) : super.charTyped(character, modifiers);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.blit(texture(), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// The balance goes above the screen rather than inside it: the ring leaves no room, and it
		// belongs to the player rather than to whatever they opened.
		String balance = TransmutationEMCFormatter.formatEMC(menu.knowledge.getEmc()).getString();
		graphics.drawString(font, balance, (imageWidth - font.width(balance)) / 2, -9, 0xB5B5B5, true);
	}

	@Override
	protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		for (WidgetKnowledgeItem button : itemButtons) {
			ItemStack stack = button.stack();

			if (button.isHovered() && !stack.isEmpty()) {
				graphics.renderTooltip(font, stack, mouseX, mouseY);
				return;
			}
		}

		super.renderTooltip(graphics, mouseX, mouseY);
	}
}
