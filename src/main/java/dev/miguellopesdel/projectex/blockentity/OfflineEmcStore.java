package dev.miguellopesdel.projectex.blockentity;

import com.mojang.logging.LogUtils;
import dev.miguellopesdel.projectex.ProjectEX;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reads and writes the transmutation knowledge of players who are not logged in, by editing
 * their save file directly.
 *
 * <p>Without this a link stops producing the moment its owner logs out, which on a server means
 * every automation built on links dies overnight. ProjectE keeps knowledge in a capability, and
 * capabilities are stored in the player file under {@code ForgeCaps}, so the balance can be
 * moved there while nobody is holding it.
 *
 * <p>Writes go straight to disk rather than being batched. Batching would be faster, but a
 * player logging in reads their file during tick processing, and a delayed write would either
 * be lost or overwrite what they earned after logging in. Reads are cached, which is safe
 * because while a player is offline this class is the only thing that changes their file.
 */
@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID)
public final class OfflineEmcStore {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String CAPS = "ForgeCaps";
	private static final String KNOWLEDGE = "projecte:knowledge";
	private static final String EMC = "transmutationEmc";

	private static final Map<UUID, CompoundTag> CACHE = new HashMap<>();

	private OfflineEmcStore() {
	}

	public static BigInteger balanceOf(MinecraftServer server, UUID player) {
		CompoundTag knowledge = knowledge(server, player);

		if (knowledge == null) {
			return BigInteger.ZERO;
		}

		String emc = knowledge.getString(EMC);
		return emc.isEmpty() ? BigInteger.ZERO : new BigInteger(emc);
	}

	public static boolean setBalance(MinecraftServer server, UUID player, BigInteger balance) {
		CompoundTag data = playerData(server, player);

		if (data == null) {
			return false;
		}

		knowledgeOf(data).putString(EMC, balance.toString());
		return write(server, player, data);
	}

	/**
	 * Adds an item to an offline player's knowledge, so that what a link eats while they are
	 * away is transmutable when they return.
	 */
	public static void learn(MinecraftServer server, UUID player, ItemStack stack) {
		CompoundTag data = playerData(server, player);

		if (data == null) {
			return;
		}

		CompoundTag knowledge = knowledgeOf(data);
		ListTag known = knowledge.getList("knowledge", Tag.TAG_COMPOUND);
		CompoundTag entry = ItemInfo.fromStack(stack).write(new CompoundTag());

		for (int i = 0; i < known.size(); i++) {
			if (known.getCompound(i).equals(entry)) {
				return;
			}
		}

		known.add(entry);
		knowledge.put("knowledge", known);
		write(server, player, data);
	}

	/** Their live capability takes over the moment they are back, so the cache has to go. */
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		CACHE.remove(event.getEntity().getUUID());
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		CACHE.clear();
	}

	private static CompoundTag knowledge(MinecraftServer server, UUID player) {
		CompoundTag data = playerData(server, player);
		return data == null ? null : knowledgeOf(data);
	}

	private static CompoundTag knowledgeOf(CompoundTag playerData) {
		CompoundTag caps = playerData.getCompound(CAPS);
		playerData.put(CAPS, caps);
		CompoundTag knowledge = caps.getCompound(KNOWLEDGE);
		caps.put(KNOWLEDGE, knowledge);
		return knowledge;
	}

	private static CompoundTag playerData(MinecraftServer server, UUID player) {
		CompoundTag cached = CACHE.get(player);

		if (cached != null) {
			return cached;
		}

		File file = playerFile(server, player);

		if (!file.isFile()) {
			return null;
		}

		try {
			CompoundTag data = NbtIo.readCompressed(file);
			CACHE.put(player, data);
			return data;
		} catch (Exception ex) {
			LOGGER.error("Could not read the save file of offline player {}", player, ex);
			return null;
		}
	}

	private static boolean write(MinecraftServer server, UUID player, CompoundTag data) {
		File file = playerFile(server, player);

		try {
			// Write beside the real file and swap it in, so a crash mid write cannot leave a
			// player with a truncated save.
			File temp = new File(file.getParentFile(), player + ".dat.projectex_tmp");
			NbtIo.writeCompressed(data, temp);

			if (file.exists() && !file.delete()) {
				LOGGER.error("Could not replace the save file of offline player {}", player);
				return false;
			}

			if (!temp.renameTo(file)) {
				LOGGER.error("Could not move the new save file of offline player {} into place", player);
				return false;
			}

			CACHE.put(player, data);
			return true;
		} catch (Exception ex) {
			LOGGER.error("Could not write the save file of offline player {}", player, ex);
			return false;
		}
	}

	private static File playerFile(MinecraftServer server, UUID player) {
		Path playerData = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
		return playerData.resolve(player + ".dat").toFile();
	}
}
