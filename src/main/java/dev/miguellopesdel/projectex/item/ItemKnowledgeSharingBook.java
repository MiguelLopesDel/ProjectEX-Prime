package dev.miguellopesdel.projectex.item;

import dev.miguellopesdel.projectex.Knowledge;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A book that copies one player's knowledge to another.
 *
 * <p>Sneak and right click to write yourself into it; hand it to someone else and they right click
 * to learn everything you know. It is consumed doing so, and it teaches through the same path
 * everything else does, so a pack that forbids learning an item forbids it here too.
 *
 * <p>The author does not have to be online. ProjectE can read a knowledge provider straight out of a
 * player's save file, which is what makes the book worth carrying rather than a reason to arrange a
 * meeting.
 */
public class ItemKnowledgeSharingBook extends Item {
	private static final String AUTHOR = "Author";
	private static final String AUTHOR_NAME = "AuthorName";

	public ItemKnowledgeSharingBook(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack book = player.getItemInHand(hand);

		if (player.isShiftKeyDown()) {
			return sign(level, player, book);
		}

		UUID author = authorOf(book);

		// A book of your own knowledge would teach you nothing and cost you the book.
		if (author == null || author.equals(player.getUUID())) {
			return InteractionResultHolder.fail(book);
		}

		if (level.isClientSide()) {
			spawnPageParticles(level, player, book);
		} else if (!copyKnowledge(player, author)) {
			return InteractionResultHolder.fail(book);
		} else {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK,
					SoundSource.PLAYERS, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
		}

		book.shrink(1);
		return InteractionResultHolder.sidedSuccess(book, level.isClientSide());
	}

	private InteractionResultHolder<ItemStack> sign(Level level, Player player, ItemStack book) {
		if (!level.isClientSide()) {
			CompoundTag tag = book.getOrCreateTag();
			tag.putUUID(AUTHOR, player.getUUID());
			tag.putString(AUTHOR_NAME, player.getGameProfile().getName());
		}

		return InteractionResultHolder.sidedSuccess(book, level.isClientSide());
	}

	/** Returns whether anything was there to copy. */
	private static boolean copyKnowledge(Player player, UUID author) {
		IKnowledgeProvider reader = Knowledge.of(player);
		IKnowledgeProvider writer = ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(author);

		if (reader == null || writer == null) {
			return false;
		}

		for (ItemInfo item : writer.getKnowledge()) {
			Knowledge.teachQuietly(player, reader, item);
		}

		if (player instanceof ServerPlayer serverPlayer) {
			// One sync for the lot. Teaching each item on its own would send a packet per item.
			reader.sync(serverPlayer);
		}

		return true;
	}

	/** Torn pages, thrown the way the player is looking. */
	private static void spawnPageParticles(Level level, Player player, ItemStack book) {
		float pitch = -player.getXRot() * Mth.DEG_TO_RAD;
		float yaw = -player.getYRot() * Mth.DEG_TO_RAD;

		for (int i = 0; i < 5; i++) {
			Vec3 speed = new Vec3((level.random.nextDouble() - 0.5D) * 0.1D, level.random.nextDouble() * 0.1D + 0.1D, 0.0D)
					.xRot(pitch).yRot(yaw);
			Vec3 position = new Vec3((level.random.nextDouble() - 0.5D) * 0.3D, -level.random.nextDouble() * 0.6D - 0.3D, 0.6D)
					.xRot(pitch).yRot(yaw)
					.add(player.getX(), player.getEyeY(), player.getZ());

			level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, book),
					position.x, position.y, position.z, speed.x, speed.y + 0.05D, speed.z);
		}
	}

	@Nullable
	private static UUID authorOf(ItemStack book) {
		CompoundTag tag = book.getTag();
		return tag != null && tag.hasUUID(AUTHOR) ? tag.getUUID(AUTHOR) : null;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return authorOf(stack) != null;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		CompoundTag tag = stack.getTag();

		if (tag != null && tag.contains(AUTHOR_NAME)) {
			tooltip.add(Component.literal(tag.getString(AUTHOR_NAME)).withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.add(Component.translatable("item.projectex.knowledge_sharing_book.unsigned").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
