package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.Knowledge;
import dev.miguellopesdel.projectex.blockentity.PersistentItems;
import dev.miguellopesdel.projectex.item.ItemArcaneTablet;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A crafting table you carry, wired to your own EMC.
 *
 * <p>What makes it more than a portable workbench is that the grid buys itself back: take the
 * result and every ingredient the player knows is paid for out of the balance and put straight back
 * where it was, so a recipe can be repeated until the EMC runs out.
 *
 * <p>The panel down the side is the transmutation table, and comes from {@link ContainerTableBase}.
 */
public class ContainerArcaneTablet extends ContainerTableBase {
	/** Slots of the grid, walked around its edge, which is what rotating turns. */
	private static final int[] RING = {0, 1, 2, 5, 8, 7, 6, 3};

	public static final int CLEAR = 40;
	public static final int ROTATE_CLOCKWISE = 41;
	public static final int ROTATE_ANTICLOCKWISE = 42;
	public static final int BALANCE = 43;
	public static final int SPREAD = 44;

	private static final int FIRST_INVENTORY_SLOT = 10;

	private final InteractionHand hand;
	private final ArcaneTabletGrid grid;
	private final ResultContainer result = new ResultContainer();

	private boolean refillAfterCraft = true;

	public ContainerArcaneTablet(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		this(windowId, playerInventory, buffer.readEnum(InteractionHand.class));
	}

	public ContainerArcaneTablet(int windowId, Inventory playerInventory, InteractionHand hand) {
		super(ProjectEXMenus.ARCANE_TABLET.get(), windowId, playerInventory);
		this.hand = hand;
		grid = new ArcaneTabletGrid(this, tablet());

		addSlot(new ResultSlot(player, grid, result, 0, -23, 75) {
			@Override
			protected void checkTakeAchievements(ItemStack stack) {
				super.checkTakeAchievements(stack);

				// Crafting something is how the tablet comes to know it, exactly as ProjectE's own
				// tables learn from what passes through them.
				if (EMCHelper.doesItemHaveEmc(stack)) {
					Knowledge.teach(player, knowledge, stack);
				}
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				if (!refillAfterCraft) {
					super.onTake(player, stack);
					return;
				}

				ItemStack[] before = new ItemStack[grid.getContainerSize()];

				for (int i = 0; i < before.length; i++) {
					ItemStack ingredient = grid.getItem(i);
					before[i] = ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.copyWithCount(1);
				}

				super.onTake(player, stack);

				for (int i = 0; i < before.length; i++) {
					if (!before[i].isEmpty() && grid.getItem(i).isEmpty()) {
						buyInto(i, before[i]);
					}
				}

				grid.setChanged();
			}
		});

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				addSlot(new Slot(grid, column + row * 3, -59 + column * 18, 17 + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 135 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			int index = column;

			addSlot(new Slot(playerInventory, index, 8 + column * 18, 193) {
				@Override
				public boolean mayPickup(Player player) {
					// Selling the tablet that is drawing the screen would leave the screen open on
					// nothing.
					return !isOpenTablet(index);
				}
			});
		}

		slotsChanged(grid);
	}

	private boolean isOpenTablet(int hotbarSlot) {
		return hand == InteractionHand.MAIN_HAND && hotbarSlot == player.getInventory().selected;
	}

	ItemStack tablet() {
		return player.getItemInHand(hand);
	}

	@Override
	public boolean stillValid(Player player) {
		return tablet().getItem() instanceof ItemArcaneTablet;
	}

	@Override
	public void slotsChanged(Container container) {
		if (container == grid) {
			refreshResult();
		}
	}

	/**
	 * Works out what the grid makes and sends it over.
	 *
	 * <p>This is {@code CraftingMenu.slotChangedCraftingGrid}, which does the same for every vanilla
	 * crafting screen but is not reachable from outside that class.
	 */
	private void refreshResult() {
		Level level = player.level();

		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		ItemStack crafted = ItemStack.EMPTY;
		Optional<CraftingRecipe> recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level);

		if (recipe.isPresent() && result.setRecipeUsed(level, serverPlayer, recipe.get())) {
			ItemStack assembled = recipe.get().assemble(grid, level.registryAccess());

			if (assembled.isItemEnabled(level.enabledFeatures())) {
				crafted = assembled;
			}
		}

		result.setItem(0, crafted);
		setRemoteSlot(0, crafted);
		serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, crafted));
	}

	/**
	 * Puts one of an item back into a grid slot and charges the player for it. Only what the player
	 * already knows can be bought, so the tablet never conjures an item the transmutation table
	 * would refuse to.
	 */
	private boolean buyInto(int index, ItemStack wanted) {
		ItemInfo item = PersistentItems.infoOf(wanted);

		if (!knowledge.hasKnowledge(item)) {
			return false;
		}

		long value = EMCHelper.getEmcValue(item);

		if (value <= 0L) {
			return false;
		}

		BigInteger price = BigInteger.valueOf(value);

		if (knowledge.getEmc().compareTo(price) < 0 || !place(index, item.createStack())) {
			return false;
		}

		removeEmc(price);
		return true;
	}

	/** Buys the cheapest of the things a recipe would accept in this slot. */
	private boolean buyInto(int index, List<ItemStack> options) {
		List<ItemStack> cheapestFirst = new ArrayList<>(options);
		cheapestFirst.sort(Comparator.comparingLong((ItemStack stack) -> EMCHelper.getEmcValue(stack)));

		for (ItemStack option : cheapestFirst) {
			if (buyInto(index, option)) {
				return true;
			}
		}

		return false;
	}

	/** Puts one of an item into a grid slot, if the slot is empty or already holds the same thing. */
	private boolean place(int index, ItemStack wanted) {
		ItemStack current = grid.getItem(index);

		if (current.isEmpty()) {
			grid.getItems().set(index, wanted.copyWithCount(1));
			return true;
		}

		if (current.getCount() < current.getMaxStackSize() && ItemStack.isSameItemSameTags(current, wanted)) {
			current.grow(1);
			return true;
		}

		return false;
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		// Shift clicking the result crafts until the grid runs dry. Buying the ingredients back
		// between each one would spend the balance filling a grid the player is trying to empty.
		refillAfterCraft = clickType != ClickType.QUICK_MOVE;

		try {
			super.clicked(slotId, button, clickType, player);
		} finally {
			refillAfterCraft = true;
		}
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		switch (id) {
			case CLEAR -> clear();
			case ROTATE_CLOCKWISE -> rotate(true);
			case ROTATE_ANTICLOCKWISE -> rotate(false);
			case BALANCE -> balance();
			case SPREAD -> spread();
			default -> {
				return super.clickMenuButton(player, id);
			}
		}

		grid.setChanged();
		broadcastChanges();
		return true;
	}

	/**
	 * Lays a crafting recipe out in the grid, which is what JEI's transfer arrow asks for.
	 *
	 * <p>Ingredients come from the player's inventory first and are bought from EMC only for what is
	 * missing, so the arrow spends items before it spends the balance. With {@code fillStacks} it
	 * repeats until the grid is full, for the same reason vanilla's shift click does.
	 */
	public void transferRecipe(CraftingRecipe recipe, boolean fillStacks) {
		List<List<ItemStack>> options = layOut(recipe);
		clear();
		fill(options);

		if (fillStacks) {
			// A grid slot holds at most 64, and the first pass placed one.
			for (int i = 1; i < 64; i++) {
				fill(options);
			}
		}

		grid.setChanged();
		broadcastChanges();
	}

	/**
	 * Where each ingredient goes in the three by three. A shaped recipe keeps its shape; anything
	 * else is filled in reading order, which is what a shapeless recipe means.
	 */
	private static List<List<ItemStack>> layOut(CraftingRecipe recipe) {
		List<List<ItemStack>> grid = new ArrayList<>(9);

		for (int i = 0; i < 9; i++) {
			grid.add(List.of());
		}

		NonNullList<Ingredient> ingredients = recipe.getIngredients();

		if (recipe instanceof ShapedRecipe shaped) {
			for (int y = 0; y < shaped.getHeight(); y++) {
				for (int x = 0; x < shaped.getWidth(); x++) {
					grid.set(x + y * 3, List.of(ingredients.get(x + y * shaped.getWidth()).getItems()));
				}
			}
		} else {
			for (int i = 0; i < ingredients.size() && i < grid.size(); i++) {
				grid.set(i, List.of(ingredients.get(i).getItems()));
			}
		}

		return grid;
	}

	private void fill(List<List<ItemStack>> options) {
		for (int i = 0; i < options.size(); i++) {
			if (!options.get(i).isEmpty()) {
				takeFromInventory(i, options.get(i));
			}
		}

		for (int i = 0; i < options.size(); i++) {
			if (!options.get(i).isEmpty()) {
				buyInto(i, options.get(i));
			}
		}
	}

	/** Moves one matching item out of the player's inventory into a grid slot. */
	private boolean takeFromInventory(int index, List<ItemStack> options) {
		Inventory inventory = player.getInventory();

		for (ItemStack wanted : options) {
			ItemInfo want = PersistentItems.infoOf(wanted);

			for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
				ItemStack candidate = inventory.getItem(slot);

				if (candidate.isEmpty() || !want.equals(PersistentItems.infoOf(candidate)) || !place(index, candidate)) {
					continue;
				}

				candidate.shrink(1);

				if (candidate.isEmpty()) {
					inventory.setItem(slot, ItemStack.EMPTY);
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Empties the grid. What the table would buy back at full price is sold; the rest goes to the
	 * player, because tidying up should not cost covalence loss.
	 */
	private void clear() {
		for (int i = 0; i < grid.getContainerSize(); i++) {
			ItemStack stack = grid.removeItemNoUpdate(i);

			if (stack.isEmpty()) {
				continue;
			}

			if (!soldAtFullValue(stack) || !sell(stack)) {
				player.getInventory().placeItemBackInInventory(stack);
			}
		}
	}

	private static boolean soldAtFullValue(ItemStack stack) {
		long value = EMCHelper.getEmcValue(stack);
		return value > 0L && EMCHelper.getEmcSellValue(value) >= value;
	}

	private void rotate(boolean clockwise) {
		List<ItemStack> items = grid.getItems();
		ItemStack[] rotated = new ItemStack[RING.length];

		for (int i = 0; i < RING.length; i++) {
			rotated[i] = items.get(RING[Math.floorMod(clockwise ? i - 1 : i + 1, RING.length)]);
		}

		for (int i = 0; i < RING.length; i++) {
			items.set(RING[i], rotated[i]);
		}
	}

	/** Levels every group of identical stacks, so nine of something end up three, three and three. */
	private void balance() {
		List<ItemStack> items = grid.getItems();
		Map<CompoundTag, List<ItemStack>> groups = new LinkedHashMap<>();

		for (ItemStack stack : items) {
			if (stack.isEmpty() || stack.getMaxStackSize() <= 1) {
				continue;
			}

			CompoundTag key = stack.save(new CompoundTag());
			key.remove("Count");
			groups.computeIfAbsent(key, tag -> new ArrayList<>()).add(stack);
		}

		for (List<ItemStack> group : groups.values()) {
			int total = 0;

			for (ItemStack stack : group) {
				total += stack.getCount();
			}

			int each = total / group.size();
			int rest = total % group.size();

			for (ItemStack stack : group) {
				stack.setCount(each);
			}

			for (int i = 0; rest > 0; i = (i + 1) % group.size()) {
				ItemStack stack = group.get(i);

				if (stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					rest--;
				}
			}
		}

		// Levelling can leave a slot holding a stack of nothing, which is not the same thing as an
		// empty slot to anything that looks at the grid afterwards.
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).isEmpty()) {
				items.set(i, ItemStack.EMPTY);
			}
		}
	}

	/** Fills the empty slots from the fullest ones, then levels what is left. */
	private void spread() {
		List<ItemStack> items = grid.getItems();

		for (int empty = firstEmptySlot(items); empty >= 0; empty = firstEmptySlot(items)) {
			ItemStack biggest = biggestStack(items);

			if (biggest == null) {
				break;
			}

			items.set(empty, biggest.split(1));
		}

		balance();
	}

	private static int firstEmptySlot(List<ItemStack> items) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).isEmpty()) {
				return i;
			}
		}

		return -1;
	}

	/** The stack with the most in it, or nothing when no stack has anything to spare. */
	private static ItemStack biggestStack(List<ItemStack> items) {
		ItemStack biggest = null;

		for (ItemStack stack : items) {
			if (stack.getCount() > 1 && (biggest == null || stack.getCount() > biggest.getCount())) {
				biggest = stack;
			}
		}

		return biggest;
	}

	/**
	 * Shift clicking the result moves the crafted items to the inventory, the way it does in every
	 * crafting screen. Everywhere else the base menu's rule stands and the item is sold.
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (index != 0) {
			return super.quickMoveStack(player, index);
		}

		Slot slot = slots.get(index);

		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack crafted = slot.getItem();
		ItemStack original = crafted.copy();

		if (!moveItemStackTo(crafted, FIRST_INVENTORY_SLOT, slots.size(), true)) {
			return ItemStack.EMPTY;
		}

		slot.onQuickCraft(crafted, original);

		if (crafted.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (crafted.getCount() == original.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, crafted);
		player.drop(crafted, false);
		return original;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != result && super.canTakeItemForPickAll(stack, slot);
	}
}
