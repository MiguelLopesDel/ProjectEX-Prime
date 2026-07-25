package com.latmod.mods.projectex.item;

import com.latmod.mods.projectex.Matter;
import net.minecraft.world.item.Item;

/**
 * One of the twelve matter items, used to craft the tier of the same name.
 */
public class ItemMatter extends Item {
	public final Matter matter;

	public ItemMatter(Matter matter) {
		super(new Properties().fireResistant());
		this.matter = matter;
	}
}
