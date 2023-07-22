package tfc.flamemc;

import tfc.flame.loader.IFlameMod;

import java.util.ArrayList;

public class ModInitializer {
	static {
		ArrayList<IFlameMod> mods_list = new ArrayList<>();
		for (Object o : FlameLauncher.modsList) mods_list.add((IFlameMod) o);
		
//		for (IFlameMod iFlameMod : mods_list)
//			if (iFlameMod instanceof IFlameAPIMod)
//				((IFlameAPIMod) iFlameMod).setupAPI(gameArgs);
		for (IFlameMod iFlameMod : mods_list) iFlameMod.preInit();
		for (IFlameMod iFlameMod : mods_list) iFlameMod.onInit();
	}
}
