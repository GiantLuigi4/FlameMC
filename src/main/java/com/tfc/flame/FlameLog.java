package com.tfc.flame;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FlameLog extends TextArea {
	@Override
	public void append(String str) {
		String pattern = "hh:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		//https://www.edureka.co/blog/date-format-in-java/#:~:text=Creating%20A%20Simple%20Date%20Format,-A%20SimpleDateFormat%20is&text=String%20pattern%20%3D%20%22yyyy%2DMM,for%20formatting%20and%20parsing%20dates.
		super.append("[" + simpleDateFormat.format(new Date()) + "] " + str);
//		try{Thread.sleep(250);}catch(Throwable ignored){}
	}
}
