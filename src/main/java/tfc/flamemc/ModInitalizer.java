package tfc.flamemc;

import tfc.flame.IFlameAPIMod;
import tfc.flame.IFlameMod;

import java.util.ArrayList;

import static tfc.flamemc.FlameLauncher.gameArgs;

public class ModInitalizer {
	static {
		ArrayList<IFlameMod> mods_list = new ArrayList<>();
		for (Object o : FlameLauncher.mods_list) mods_list.add((IFlameMod) o);
		
		for (IFlameMod iFlameMod : mods_list)
			if (iFlameMod instanceof IFlameAPIMod)
				((IFlameAPIMod) iFlameMod).setupAPI(gameArgs);
		for (IFlameMod iFlameMod : mods_list) iFlameMod.preinit(gameArgs);
		for (IFlameMod iFlameMod : mods_list) iFlameMod.init(gameArgs);
		for (IFlameMod iFlameMod : mods_list) iFlameMod.postinit(gameArgs);
//		mods_list.forEach(mod -> {
//			try {
//				if (mod instanceof IFlameAPIMod) {
////					mod.getClass().getMethod("setupAPI", String[].class).invoke(mod, (Object) finalDefaultArgs);
//					((IFlameAPIMod) mod).setupAPI(args);
//				}
//			} catch (Throwable err) {
//				FlameConfig.logError(err);
//			}
//		});
//		mods_list.forEach(mod -> {
//			try {
////				mod.getClass().getMethod("preinit", String[].class).invoke(mod, (Object) finalDefaultArgs);
//				mod.preinit(args);
//			} catch (Throwable err) {
//				FlameConfig.logError(err);
//			}
//		});
//		mods_list.forEach(mod -> {
//			try {
////				mod.getClass().getMethod("init", String[].class).invoke(mod, (Object) finalDefaultArgs);
//				mod.init(args);
//			} catch (Throwable err) {
//				FlameConfig.logError(err);
//			}
//		});
//		mods_list.forEach(mod -> {
//			try {
////				mod.getClass().getMethod("postinit", String[].class).invoke(mod, (Object) finalDefaultArgs);
//				mod.postinit(args);
//			} catch (Throwable err) {
//				FlameConfig.logError(err);
//			}
//		});
	}
}
