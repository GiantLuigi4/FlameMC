package com.tfc.flame;

public interface IFlameMod {
	void preinit(String[] args);
	
	void init(String[] args);
	
	void postinit(String[] args);
}
