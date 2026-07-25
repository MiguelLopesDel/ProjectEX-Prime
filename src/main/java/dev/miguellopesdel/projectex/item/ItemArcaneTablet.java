package dev.miguellopesdel.projectex.item;

import dev.miguellopesdel.projectex.gui.ContainerArcaneTablet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Right click to open the tablet. The hand travels with the screen, because that is what tells the
 * menu which stack holds the grid it is about to show.
 */
public class ItemArcaneTablet extends Item {
	public ItemArcaneTablet(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
					(windowId, inventory, opener) -> new ContainerArcaneTablet(windowId, inventory, hand),
					Component.translatable(getDescriptionId())), buffer -> buffer.writeEnum(hand));
		}

		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
	}
}
