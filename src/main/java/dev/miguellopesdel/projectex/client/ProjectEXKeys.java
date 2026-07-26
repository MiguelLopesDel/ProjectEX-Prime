package dev.miguellopesdel.projectex.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.miguellopesdel.projectex.ProjectEX;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ProjectEX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ProjectEXKeys {
	/**
	 * Puts the cursor in a transmutation screen's search bar. Bound to tab, and only inside a
	 * screen, so it cannot fight with anything bound in the world.
	 */
	public static final KeyMapping FOCUS_SEARCH = new KeyMapping("key.projectex.focus_search",
			KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, "key.categories.projectex");

	private ProjectEXKeys() {
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		event.register(FOCUS_SEARCH);
	}
}
