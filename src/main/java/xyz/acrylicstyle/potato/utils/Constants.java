package xyz.acrylicstyle.potato.utils;

import java.util.Random;

public class Constants {
	public final static String version = "v0.1";
	public final static String requiredMinecraftVersion = "1.8.8";
	public final static String instanceIdentifier;
	public final static int intIdentifier;
	public final static char warning = '\u26a0';
	public final static char heavy_check_mark = '\u2714';
	public final static char heart_suit = '\u2665';
	public final static char heart = '\u2764';
	public final static char heart_exclamation = '\u2763';
	public final static char peace = '\u270c';
	public static int mininumPlayers = 5;

	static {
		Random random = new Random();
		char identifier;
		switch(random.nextInt(5)) {
			case 0: identifier = heavy_check_mark; break;
			case 1: identifier = heart_suit; break;
			case 2: identifier = heart; break;
			case 3: identifier = heart_exclamation; break;
			case 4: identifier = peace; break;
			default: identifier = warning;
		}
		intIdentifier = random.nextInt(100000);
		instanceIdentifier = "" + intIdentifier + identifier;
	}
}
