package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.Matter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.List;

/**
 * Nine collectors of one tier compressed into a single item, the intermediate step towards
 * that tier's power flower. It does not collect EMC on its own.
 */
public class ItemCompressedCollector extends Item {
	public final Matter matter;

	public ItemCompressedCollector(Matter matter) {
		super(new Properties());
		this.matter = matter;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(stack, level, list, flag);
		list.add(Component.translatable("item.projectex.compressed_collector.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
