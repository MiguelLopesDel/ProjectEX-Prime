package dev.miguellopesdel.projectex.gui;

import dev.miguellopesdel.projectex.ProjectEXConfig;
import dev.miguellopesdel.projectex.block.BlockStoneTable;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The Stone Table's own transmutation panel.
 *
 * <p>It exists rather than handing off to ProjectE's screen because of the whitelist: a pack can
 * cut the table down to a short list of materials, and that only means anything if the screen it
 * opens is one that knows about the list. Everything else about it is the shared panel.
 *
 * <p>Items that hold EMC are always allowed through, whatever the whitelist says, so a Klein Star
 * can be filled or emptied at any table.
 */
public class ContainerStoneTable extends ContainerTableBase {
	private final BlockPos pos;

	public ContainerStoneTable(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		this(windowId, playerInventory, buffer.readBlockPos());
	}

	public ContainerStoneTable(int windowId, Inventory playerInventory, BlockPos pos) {
		super(ProjectEXMenus.STONE_TABLE.get(), windowId, playerInventory);
		this.pos = pos;

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 135 + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, 8 + column * 18, 193));
		}
	}

	@Override
	public boolean isItemValid(ItemInfo item) {
		ItemStack stack = item.createStack();
		return ProjectEXConfig.isStoneTableWhitelisted(stack)
				|| stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).isPresent();
	}

	@Override
	public boolean stillValid(Player player) {
		return player.level().getBlockState(pos).getBlock() instanceof BlockStoneTable
				&& player.distanceToSqr(pos.getCenter()) <= 64.0D;
	}
}
