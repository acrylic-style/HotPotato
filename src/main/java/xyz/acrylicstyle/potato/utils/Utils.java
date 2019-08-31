package xyz.acrylicstyle.potato.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import xyz.acrylicstyle.potato.HotPotato;
import xyz.acrylicstyle.potato.Teams;
import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.utils.Log;

public class Utils {
	public static HotPotato getPotatoInstance() {
		return HotPotato.getPlugin(HotPotato.class);
	}

	public static void teleportAllPlayers() {
		ConfigProvider.initWithoutException("./plugins/HotPotato/config.yml");
	}

	public static void reload() {
		HotPotato.config.reloadWithoutException();
		try {
			HotPotato.mapConfig = new ConfigProvider("./plugins/HotPotato/maps/" + HotPotato.mapName + ".yml");
		} catch (IOException | InvalidConfigurationException e) {
			Log.error("Couldn't read config: maps/" + HotPotato.mapName + ".yml");
			e.printStackTrace();
		}
		HotPotato.debug = HotPotato.config.getBoolean("debug", false);
	}

	public static void roll() {
		HotPotato.round = HotPotato.round + 1;
		if (HotPotato.round >= 5) Utils.teleportAllPlayers();
		if (HotPotato.round == 5) Bukkit.broadcastMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Deathmatch has started!");
		List<Player> players = new ArrayList<Player>();
		players.addAll(Bukkit.getOnlinePlayers());
		players.removeIf(player -> {
			return HotPotato.teamMap.get(player.getUniqueId()) == Teams.SPECTATOR;
		});
		final double ppl = players.size();
		players.forEach(player -> {
			if (Math.ceil(ppl/(double) 5) - HotPotato.its > 0) {
				HotPotato.teamMap.put(player.getUniqueId(), Teams.IT);
				Utils.potatoInventory(player);
				Utils.sendITMessage(player);
			} else {
				HotPotato.teamMap.put(player.getUniqueId(), Teams.PLAYER);
				Utils.sendPlayerMessage(player);
			}
		});
	}

	public static void sendITMessage(Player player) {
		player.sendMessage(ChatColor.BLUE + "==================================================");
		player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "                    ROUND " + HotPotato.round + " STARTED!");
		player.sendMessage("");
		player.sendMessage(ChatColor.RED + "              You did started as IT!");
		player.sendMessage(ChatColor.RED + "                  Tag to someone!");
		player.sendMessage(ChatColor.BLUE + "==================================================");
	}

	public static void sendPlayerMessage(Player player) {
		player.sendMessage(ChatColor.BLUE + "==================================================");
		player.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "                    ROUND " + HotPotato.round + " STARTED!");
		player.sendMessage("");
		player.sendMessage(ChatColor.GREEN + "              You did NOT started as IT!");
		player.sendMessage(ChatColor.GREEN + "                      Run away!");
		player.sendMessage(ChatColor.BLUE + "==================================================");
	}

	public static void explodeITs() {
		Bukkit.getOnlinePlayers().forEach(player -> {
			if (HotPotato.teamMap.get(player.getUniqueId()) == Teams.IT) {
				player.getWorld().spigot().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, 0, 0, 0, 0, 0, 1, 10, 15);
				player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 150, 1); // does volume 150 really works?
				player.sendMessage(ChatColor.YELLOW + "You blew up!");
				Bukkit.broadcastMessage(ChatColor.GRAY + (player.getDisplayName() == "" || player.getDisplayName() == null ? player.getName() : player.getDisplayName()) + ChatColor.YELLOW + " blew up!");
				HotPotato.teamMap.put(player.getUniqueId(), Teams.SPECTATOR);
				player.setGameMode(GameMode.SPECTATOR);
				player.getInventory().clear();
				Location location = new Location(Bukkit.getWorld(HotPotato.mapConfig.getString("world")), HotPotato.spawnX, HotPotato.spawnY, HotPotato.spawnZ);
				player.teleport(location);
				player.setPlayerListName(ChatColor.GRAY + player.getName());
				return;
			}
		});
	}

	public static void endGame() {
		if (HotPotato.shutdownIn != 15) return;
		HotPotato.gameEnded = true;
		for (Player player : Bukkit.getOnlinePlayers()) {
			new BukkitRunnable() {
				public void run() {
					if (HotPotato.fireworked*Bukkit.getOnlinePlayers().size() >= 40*Bukkit.getOnlinePlayers().size()) this.cancel();
					player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 100, 1);
					HotPotato.fireworked++;
				}
			}.runTaskTimer(Utils.getPotatoInstance(), 0, 5);
		}
		List<Player> players = new ArrayList<Player>();
		players.addAll(Bukkit.getOnlinePlayers());
		players.removeIf(player -> {
			return HotPotato.teamMap.get(player.getUniqueId()) != Teams.PLAYER;
		});
		Bukkit.broadcastMessage(ChatColor.GREEN + players.get(0).getName() + " won the game!");
		Bukkit.broadcastMessage(ChatColor.GRAY + "This server will be shutdown in 15 seconds.");
		TimerTask task = new TimerTask() {
			public void run() {
				Bukkit.broadcastMessage(ChatColor.GRAY + "Shutting down...");
				Bukkit.shutdown();
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 1000*15);
	}

	public static int getExplodeIn(int round) {
		switch (round) {
			case 0: return 50; // it shouldn't be called
			case 1: return 50;
			case 2: return 45;
			case 3: return 30;
			case 4: return 25;
			case 5: return 20;
			default: return 15; // round 6 and later
		}
	}

	public static ItemStack createLeatherItemStack(Material material, int red, int green, int blue) {
		ItemStack item = new ItemStack(material);
		LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
		lam.setColor(Color.fromRGB(red, green, blue));
		item.setItemMeta(lam);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 100);
		return item;
	}

	public static ItemStack hotPotatoItem() {
		ItemStack item = new ItemStack(Material.BAKED_POTATO);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Super Hot Potato");
		meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 0, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		return item;
	}

	public static void chat(AsyncPlayerChatEvent event, Teams pteam, String teamname) {
		chat(event, pteam, teamname, false);
	}

	public static void chat(AsyncPlayerChatEvent event, Teams pteam, String teamname, boolean alwaysAll) {
		event.setMessage(event.getMessage().replaceAll("<3", "" + ChatColor.RED + Constants.heart + ChatColor.RESET));
		event.setMessage(event.getMessage().replaceAll(":peace:", "" + ChatColor.GREEN + Constants.peace + ChatColor.RESET));
		if (HotPotato.gameEnded || !HotPotato.gameStarted || alwaysAll) {
			event.setFormat(teamname + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
			return;
		}
		if (pteam == Teams.SPECTATOR) {
			HotPotato.teamMap.forEach((uuid, team) -> {
				if (team != pteam) return;
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getUniqueId().equals(uuid)) {
						player.sendMessage(teamname + " " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
					}
				}
			});
			event.setCancelled(true);
			return;
		}
		event.setFormat(teamname + " " + event.getPlayer().getName() + ChatColor.RESET + ChatColor.WHITE + ": " + event.getMessage());
	}

	public static void potatoInventory(Player player) {
		player.getInventory().clear();
		ItemStack item = Utils.hotPotatoItem();
		player.getInventory().setItem(0, item);
		player.getInventory().setItem(1, item);
		player.getInventory().setItem(2, item);
		player.getInventory().setItem(3, item);
		player.getInventory().setItem(4, item);
		player.getInventory().setItem(5, item);
		player.getInventory().setItem(6, item);
		player.getInventory().setItem(7, item);
		player.getInventory().setItem(8, item);
		player.getInventory().setHelmet(Utils.createLeatherItemStack(Material.LEATHER_HELMET, 255, 165, 0));
		player.getInventory().setChestplate(Utils.createLeatherItemStack(Material.LEATHER_CHESTPLATE, 255, 165, 0));
		player.getInventory().setLeggings(Utils.createLeatherItemStack(Material.LEATHER_LEGGINGS, 255, 165, 0));
		player.getInventory().setBoots(Utils.createLeatherItemStack(Material.LEATHER_BOOTS, 255, 165, 0));
	}
}
