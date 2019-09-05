package xyz.acrylicstyle.potato.utils;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class Constants {
	public final static String version = "v0.1";
	public final static String requiredMinecraftVersion = "1.8.8";
	public final static String instanceIdentifier;
	public final static String gameName = "Yululi Head";
	public final static String gameNameColored = "" + ChatColor.YELLOW + ChatColor.BOLD + "Yululi Head";
	public final static String ITActionBar = "You're yululi! Tag someone!";
	public final static String playerActionBar = ChatColor.GREEN + "Run away!";
	public final static String potatoItemName = ChatColor.RED + "Super Yululi Head";
	public final static Material potatoItem = Material.SKULL_ITEM;
	public final static Enchantment potatoEnchant = Enchantment.PROTECTION_ENVIRONMENTAL;
	public final static int intIdentifier;
	public final static char warning = '\u26a0';
	public final static char heavy_check_mark = '\u2714';
	public final static char heart_suit = '\u2665';
	public final static char heart = '\u2764';
	public final static char heart_exclamation = '\u2763';
	public final static char peace = '\u270c';
	public final static int ITSpeed = 3; // IV
	public final static int playerSpeed = 2; // III
	public final static int infinityPotionDuration = 100000;
	public final static String ITPrefix = ChatColor.YELLOW + "[Yululi] ";
	public final static String spectatorPrefix = ChatColor.GRAY + "[SPECTATOR] ";
	public final static String playerPrefix = ChatColor.GRAY + "";
	public static int mininumPlayers = 5;
	public final static GameMode defaultGameMode = GameMode.ADVENTURE;
	public final static ItemStack helmet;
	public final static ItemStack chestplate = new ItemStack(Material.AIR);
	public final static ItemStack leggings = new ItemStack(Material.AIR);
	public final static ItemStack boots = new ItemStack(Material.AIR);

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
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, (byte) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner("yululi");
		skull.setItemMeta(meta);
		helmet = skull;
	}
}
