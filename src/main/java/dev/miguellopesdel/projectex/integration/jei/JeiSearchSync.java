package dev.miguellopesdel.projectex.integration.jei;

import mezz.jei.api.runtime.IJeiRuntime;

import javax.annotation.Nullable;

/**
 * Types the transmutation screen's search into JEI's own search bar.
 *
 * <p>This is the only place that touches JEI's runtime, and nothing outside the JEI package
 * references it directly: {@link dev.miguellopesdel.projectex.gui.GuiTableBase} reaches it through
 * a check that JEI is installed, so the class is never loaded when it is not.
 */
public final class JeiSearchSync {
	@Nullable
	private static IJeiRuntime runtime;

	private JeiSearchSync() {
	}

	static void setRuntime(@Nullable IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	public static void setFilter(String text) {
		if (runtime != null) {
			runtime.getIngredientFilter().setFilterText(text);
		}
	}
}
