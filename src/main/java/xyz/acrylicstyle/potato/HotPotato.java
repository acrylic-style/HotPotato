package xyz.acrylicstyle.potato;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import xyz.acrylicstyle.potato.utils.ActionBar;
import xyz.acrylicstyle.potato.utils.Constants;
import xyz.acrylicstyle.potato.utils.UpdateTask;
import xyz.acrylicstyle.potato.utils.Utils;
import xyz.acrylicstyle.tomeito_core.providers.ConfigProvider;
import xyz.acrylicstyle.tomeito_core.utils.Log;

public final class HotPotato extends JavaPlugin implements Listener {
	public static ConfigProvider mapConfig = null;
	public static ConfigProvider config = null;
	public static ScoreboardManager manager = null;
	public static boolean gameStarted = false;
	public static boolean gameEnded = false;
	public static boolean debug = false;
	public static boolean timerStarted = false;
	public static boolean orange = true;
	public static boolean roundEnded = false;
	public static HashMap<UUID, Scoreboard> scoreboardMap = new HashMap<UUID, Scoreboard>();
	public static HashMap<UUID, String> lastScore4Map = new HashMap<UUID, String>();
	public static HashMap<UUID, String> lastScore8Map = new HashMap<UUID, String>();
	public static HashMap<UUID, Teams> teamMap = new HashMap<UUID, Teams>();
	public static int timesLeft = 60;
	public static int playedTime = 0;
	public static int explodeIn = 50;
	public static int round = 0;
	public static int its = 0;
	public static int cooldown = 5;
	public static int shutdownIn = 15;
	public static int fireworked = 0;
	public static String mapName = ""; // not name but name of map file
	public static String name = ""; // its actually map name
	public static double spawnX = 0;
	public static double spawnY = 0;
	public static double spawnZ = 0;

	private final HotPotato potato = this;

	@Override
	public void onEnable() {
		Log.info("This server is running at " + Constants.instanceIdentifier);
		if (!Bukkit.getBukkitVersion().contains(Constants.requiredMinecraftVersion)) {
			Log.error("Your current minecraft version(" + Bukkit.getBukkitVersion() + ") is incompatible.");
			Log.error("Please use spigot " + Constants.requiredMinecraftVersion + " and restart your server.");
			Log.warn("Shutting down server");
			Bukkit.shutdown();
			return;
		}
		try {
			config = new ConfigProvider("./plugins/HotPotato/config.yml");
			mapName = config.getString("map", "default");
			mapConfig = new ConfigProvider("./plugins/HotPotato/maps/" + mapName + ".yml");
			name = mapConfig.getString("name", "???");
			spawnX = mapConfig.getDouble("spawnX");
			spawnY = mapConfig.getDouble("spawnY");
			spawnZ = mapConfig.getDouble("spawnZ");
		} catch (IOException | InvalidConfigurationException e) {
			Log.error("Failed to load config");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		manager = Bukkit.getScoreboardManager();
		Bukkit.getPluginManager().registerEvents(potato, potato);
		Constants.mininumPlayers = config.getInt("mininumPlayers", 3);
		if (Constants.mininumPlayers <= 1) {
			Log.error("You've set mininum players (in config.yml) to " + Constants.mininumPlayers + "!");
			Log.error("This game won't work!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! (it'll end immediaty)");
		}
		new BukkitRunnable() {
			public void run() {
				Bukkit.getWorlds().forEach(world -> world.setPVP(true));
				Log.info("Enabled HotPotato");
			}
		}.runTaskLater(this, 1);
	}

	@Override
	public void onDisable() {
		File log = new File("./logs/latest.log");
		File log2 = new File("./logs/" + Constants.intIdentifier + ".log");
		try {
			log2.createNewFile();
			FileUtils.copyFile(log, log2);
		} catch (IOException e) {
			Log.error("Error while saving log file");
			e.printStackTrace();
		}
	}

	@EventHandler
	public synchronized void onPlayerJoin(final PlayerJoinEvent event) {
		event.setJoinMessage(" " + ChatColor.AQUA + ">" + ChatColor.RED + ">" + ChatColor.GREEN + "> " + ChatColor.GOLD + "[MVP" + ChatColor.WHITE + "++" + ChatColor.GOLD +  "] " + event.getPlayer().getName() + " joined the game! " + ChatColor.GREEN + "<" + ChatColor.RED + "<" + ChatColor.AQUA + "<");
		long time = System.currentTimeMillis();
		World world = Bukkit.getWorld(mapConfig.getString("world", "world"));
		final Scoreboard board = manager.getNewScoreboard();
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
		event.getPlayer().getInventory().setHelmet(new ItemStack(Material.AIR));
		event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
		event.getPlayer().getInventory().setLeggings(new ItemStack(Material.AIR));
		event.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
		new BukkitRunnable() {
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.isDead()) player.spigot().respawn();
					if (player.getGameMode() == GameMode.SURVIVAL) player.setGameMode(GameMode.ADVENTURE);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 1, false, false)); // saturation II
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 9, false, false)); // resistance X
					if (teamMap.get(player.getUniqueId()) == Teams.IT) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 2, false, false), true); // speed III
					} else {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 1, false, false), true); // speed II
					}
				}
			}
		}.runTaskTimer(this, 20, 20);
		new BukkitRunnable() {
			public void run() {
				if (!gameStarted) return;
				if (orange) orange = false; else orange = true;
				Bukkit.getOnlinePlayers().forEach(player -> {
					if (HotPotato.teamMap.get(player.getUniqueId()) == Teams.IT) {
						if (orange) {
							ActionBar.setActionBarWithoutException(player, ChatColor.WHITE + "You're IT! Tag someone!");
						} else {
							ActionBar.setActionBarWithoutException(player, ChatColor.RED + "You're IT! Tag someone!");
						}
					} else if (HotPotato.teamMap.get(player.getUniqueId()) == Teams.PLAYER) {
						ActionBar.setActionBarWithoutException(player, ChatColor.GREEN + "Run away!");
					}
				});
			}
		}.runTaskTimer(this, 20, 20);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + event.getPlayer().getName() + " clear");
		lastScore4Map.put(event.getPlayer().getUniqueId(), "");
		lastScore8Map.put(event.getPlayer().getUniqueId(), "");
		event.getPlayer().getInventory().clear();
		event.getPlayer().setGameMode(GameMode.ADVENTURE);
		event.getPlayer().setScoreboard(board);
		event.getPlayer().setMaxHealth(20);
		final Objective objective = board.registerNewObjective("scoreboard", "dummy");
		Score score9 = objective.getScore("                        " + ChatColor.GRAY + Constants.instanceIdentifier); // 24 spaces
		score9.setScore(9);
		Score score7 = objective.getScore(" ");
		score7.setScore(7);
		Score score5 = objective.getScore("  ");
		score5.setScore(5);
		Score score3 = objective.getScore("   ");
		score3.setScore(3);
		Score score2 = objective.getScore(ChatColor.GREEN + "    Map: " + name);
		score2.setScore(2);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(""+ChatColor.GOLD + ChatColor.BOLD + "Hot Potato");
		scoreboardMap.put(event.getPlayer().getUniqueId(), board);
		new BukkitRunnable() {
			public void run() {
				event.getPlayer().getInventory().clear();
				event.getPlayer().teleport(world.getSpawnLocation());
				event.getPlayer().setGameMode(GameMode.ADVENTURE);
				event.getPlayer().sendMessage(ChatColor.BLUE + "--------------------------------------------------");
				event.getPlayer().sendMessage(ChatColor.GOLD + "          - Hot Potato " + ChatColor.GRAY + "(" + Constants.version + ")" + ChatColor.GOLD + " -");
				event.getPlayer().sendMessage(ChatColor.BLUE + "--------------------------------------------------");
				if (gameStarted) {
					Player player = event.getPlayer();
					player.getInventory().clear();
					teamMap.remove(player.getUniqueId());
					teamMap.put(player.getUniqueId(), Teams.SPECTATOR);
					Location location = new Location(Bukkit.getWorld(mapConfig.getString("world")), spawnX, spawnY, spawnZ);
					player.teleport(location);
					player.setGameMode(GameMode.SPECTATOR);
					player.setPlayerListName(ChatColor.GRAY + player.getName());
					return;
				}
			}
		}.runTaskLater(this, 40);
		if (debug) {
			long end = System.currentTimeMillis() - time;
			Log.debug("onPlayerJoin() took " + end + "ms");
		}
		if (timerStarted) return;
		timerStarted = true;
		new UpdateTask().runTaskTimer(this, 0, 20);
	}

	@EventHandler
	public synchronized void onPlayerLeft(PlayerQuitEvent event) {
		if (teamMap.get(event.getPlayer().getUniqueId()) == Teams.IT) its--;
		teamMap.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory() == null) return;
		if (e.getInventory().getViewers().get(0).getGameMode() == GameMode.ADVENTURE) e.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.getMessage().equalsIgnoreCase("gg") || event.getMessage().equalsIgnoreCase("good game")) {
			event.setMessage(ChatColor.GOLD + event.getMessage());
		}
		if (teamMap.get(event.getPlayer().getUniqueId()) == Teams.IT) {
			Utils.chat(event, Teams.IT, ChatColor.RED + "[IT] ");
		} else if (teamMap.get(event.getPlayer().getUniqueId()) == Teams.PLAYER) {
			Utils.chat(event, Teams.PLAYER, "");
		} else if (teamMap.get(event.getPlayer().getUniqueId()) == Teams.SPECTATOR) {
			Utils.chat(event, Teams.SPECTATOR, ChatColor.GRAY + "[SPECTATOR] ");
		} else {
			Utils.chat(event, Teams.PLAYER, "", true);
		}
	}

	@EventHandler
	public void onPlayerHurt(EntityDamageByEntityEvent event) {
		long time = System.currentTimeMillis();
		if (!(event.getDamager() instanceof Player) || !gameStarted || gameEnded) {
			event.setCancelled(true);
			return;
		}
		if (teamMap.get(event.getDamager().getUniqueId()) == teamMap.get(event.getEntity().getUniqueId())) return;
		if (teamMap.get(event.getEntity().getUniqueId()) == Teams.IT || teamMap.get(event.getDamager().getUniqueId()) == Teams.PLAYER) return;
		Player damager = (Player) event.getDamager();
		Player player = (Player) event.getEntity();
		teamMap.put(player.getUniqueId(), Teams.IT);
		teamMap.put(damager.getUniqueId(), Teams.PLAYER);
		player.setGameMode(GameMode.ADVENTURE);
		player.setPlayerListName(ChatColor.RED + "[IT] " + player.getName());
		damager.setPlayerListName(ChatColor.GRAY + damager.getName());
		damager.getInventory().clear();
		damager.getInventory().setHelmet(new ItemStack(Material.AIR));
		damager.getInventory().setChestplate(new ItemStack(Material.AIR));
		damager.getInventory().setLeggings(new ItemStack(Material.AIR));
		damager.getInventory().setBoots(new ItemStack(Material.AIR));
		Utils.potatoInventory(player);
		damager.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2); // avoid loud sound, it's 80%!
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2); // avoid loud sound, it's 80%!
		damager.sendMessage(ChatColor.GREEN + "You tagged " + player.getName() + "!");
		player.sendMessage(ChatColor.RED + damager.getName() + " tagged you!");
		Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " is IT!");
		Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower(2);
		meta.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.ORANGE).withTrail().build());
		firework.setFireworkMeta(meta);
		firework.detonate();
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + damager.getName() + " clear");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + player.getName() + " clear");
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 2, false, false), true); // speed III
		damager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 1, false, false), true); // speed II
		if (debug) {
			long end = System.currentTimeMillis()-time;
			Log.debug("onPlayerHurt() took " + end + "ms");
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		event.setCancelled(true);
	}
}
