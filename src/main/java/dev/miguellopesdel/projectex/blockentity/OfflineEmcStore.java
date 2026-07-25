package dev.miguellopesdel.projectex.blockentity;

import com.mojang.logging.LogUtils;
import dev.miguellopesdel.projectex.ProjectEX;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
 * <p>Changes are made to a cached copy and written once a tick. A link charges EMC on every
 * extraction, and an item pipe can pull dozens of times in a tick, so writing on the spot would
 * turn a fast pipe into a fast disk. Holding the change in memory is safe because while a player
 * is offline this class is the only thing that touches their file.
 *
 * <p>The flush deliberately happens at the end of a level tick rather than the end of the server
 * tick: a player logging in reads their file from the connection handling that runs after all
 * levels have ticked, so anything the links did to it that tick is already on disk by then.
 */
@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID)
public final class OfflineEmcStore {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String CAPS = "ForgeCaps";
	private static final String KNOWLEDGE = "projecte:knowledge";
	private static final String EMC = "transmutationEmc";

	private static final Map<UUID, CompoundTag> CACHE = new HashMap<>();

	/** Players whose cached copy has moved ahead of what is on disk. */
	private static final Set<UUID> DIRTY = new HashSet<>();

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
		DIRTY.add(player);
		return true;
	}

	/**
	 * Adds an item to an offline player's knowledge, so that what a link eats while they are
	 * away is transmutable when they return.
	 */
	public static void learn(MinecraftServer server, UUID player, ItemInfo item) {
		CompoundTag data = playerData(server, player);

		if (data == null) {
			return;
		}

		CompoundTag knowledge = knowledgeOf(data);
		ListTag known = knowledge.getList("knowledge", Tag.TAG_COMPOUND);
		CompoundTag entry = item.write(new CompoundTag());

		if (indexOf(known, entry) >= 0) {
			return;
		}

		known.add(entry);
		knowledge.put("knowledge", known);
		DIRTY.add(player);
	}

	private static int indexOf(ListTag known, CompoundTag entry) {
		for (int i = 0; i < known.size(); i++) {
			if (known.getCompound(i).equals(entry)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Puts the tick's changes on disk. This runs while the level is still the only thing that has
	 * ticked, which is what keeps a player who logs in this very tick from reading a stale file.
	 */
	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
			flush(event.level.getServer());
		}
	}

	/** Anything a packet did after the levels ticked, such as a player emptying a link by hand. */
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			flush(event.getServer());
		}
	}

	/** Their live capability takes over the moment they are back, so the cache has to go. */
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		UUID player = event.getEntity().getUUID();

		// Their file has already been read by now, so a pending write would be both lost and
		// overwritten. It should have gone out on the level tick; drop it rather than write it
		// over what they are holding.
		DIRTY.remove(player);
		CACHE.remove(player);
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		flush(event.getServer());
		CACHE.clear();
	}

	private static void flush(@Nullable MinecraftServer server) {
		if (server == null || DIRTY.isEmpty()) {
			return;
		}

		for (UUID player : DIRTY) {
			CompoundTag data = CACHE.get(player);

			if (data != null) {
				write(server, player, data);
			}
		}

		DIRTY.clear();
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
