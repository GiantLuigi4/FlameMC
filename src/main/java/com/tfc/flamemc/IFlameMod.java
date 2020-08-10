package com.tfc.flamemc;

public interface IFlameMod {
	void preinit(String[] args, FlameLog log);
	void init(String[] args, FlameLog log);
	void postinit(String[] args, FlameLog log);
}
