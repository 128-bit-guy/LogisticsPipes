package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.Maps;
import net.minecraft.launchwrapper.Launch;

import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import lombok.Getter;
import net.minecraftforge.fml.relauncher.Side;

@IFMLLoadingPlugin.MCVersion("1.12.2")
//@IFMLLoadingPlugin.SortingIndex(1001) TODO: For next MC update. Changing this now, will change ASM check sums as well.
public class LogisticsPipesCoreLoader implements IFMLLoadingPlugin {

//	static {
//		try {
//			Field field = NetworkRegistry.class.getDeclaredField("channels");
//			field.setAccessible(true);
//			((EnumMap<Side, Map<String, FMLEmbeddedChannel>>)field.get(NetworkRegistry.INSTANCE)).put((Side) Side.class.getField("BUKKIT").get(null), Maps.newConcurrentMap());
//		} catch (Throwable thr) {
//
//		}
//	}
	@Getter
	private static boolean coremodLoaded = false;
	private static boolean developmentEnvironment = false;

	public LogisticsPipesCoreLoader() throws Exception {
		Launch.classLoader.addTransformerExclusion("logisticspipes.asm.");
		coremodLoaded = true;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "logisticspipes.asm.LogisticsClassTransformer" };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		if (data.containsKey("runtimeDeobfuscationEnabled")) {
			developmentEnvironment = !((Boolean) data.get("runtimeDeobfuscationEnabled"));
		}
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static boolean isDevelopmentEnvironment() {
		return developmentEnvironment;
	}
}
