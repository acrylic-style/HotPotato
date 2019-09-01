package xyz.acrylicstyle.potato.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import xyz.acrylicstyle.potato.HotPotato;
import xyz.acrylicstyle.potato.Teams;
import xyz.acrylicstyle.tomeito_core.utils.Log;

public class UpdateTask extends BukkitRunnable {
	private boolean once = false;

	@SuppressWarnings("deprecation")
	public synchronized void run() {
		long time = System.currentTimeMillis();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			HotPotato.scoreboardMap.get(player.getUniqueId()).resetScores(HotPotato.lastScore4Map.get(player.getUniqueId()));
		}
		if (!HotPotato.gameStarted) {
			if (Bukkit.getOnlinePlayers().size() == 0) {
				this.cancel();
				HotPotato.timerStarted = false;
			}
			for (final Player player : Bukkit.getOnlinePlayers()) {
				// team ----->
				Scoreboard scoreboard = HotPotato.scoreboardMap.get(player.getUniqueId());
				Objective objective3 = scoreboard.getObjective(DisplaySlot.SIDEBAR);
				//Objective hpobjective2 = scoreboard.getObjective(DisplaySlot.BELOW_NAME);
				//Score hp = hpobjective2.getScore(player.getName());
				//hp.setScore((int) player.getHealth());
				String lastScore8 = HotPotato.lastScore8Map.get(player.getUniqueId());
				scoreboard.resetScores(lastScore8);
				lastScore8 = ChatColor.GREEN + "    Players: " + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers();
				Score score8 = objective3.getScore(lastScore8);
				score8.setScore(8);
				String leftSecond = Integer.toString(HotPotato.timesLeft % 60);
				if (leftSecond.length() == 1) leftSecond = "0" + leftSecond;
				String lastScore4 = HotPotato.lastScore4Map.get(player.getUniqueId());
				scoreboard.resetScores(lastScore4);
				if (Bukkit.getOnlinePlayers().size() >= Constants.mininumPlayers)
					lastScore4 = ChatColor.GREEN + "    Starting in " + Math.round(Math.nextDown(HotPotato.timesLeft/60)) + ":" + leftSecond;
				else
					lastScore4 = "    Waiting...";
				HotPotato.lastScore8Map.put(player.getUniqueId(), lastScore8);
				HotPotato.lastScore4Map.put(player.getUniqueId(), lastScore4);
				Score score4 = objective3.getScore(lastScore4);
				score4.setScore(4);
				// <----- team
				if (HotPotato.timesLeft == 10) {
					player.sendTitle(ChatColor.GREEN + "10", "");
				}
				/* do not edit this line */ player.setScoreboard(HotPotato.scoreboardMap.get(player.getUniqueId()));
				if (HotPotato.timesLeft == 5) {
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 1);
					player.sendTitle(ChatColor.GREEN + "5", "");
				} else if (HotPotato.timesLeft == 4) {
					player.setGameMode(GameMode.ADVENTURE);
					player.getWorld().setGameRuleValue("doMobLoot", "false");
					player.getWorld().setGameRuleValue("doDaylightCycle", "false");
					player.getWorld().setGameRuleValue("keepInventory", "true");
					player.getWorld().setGameRuleValue("doFireTick", "false");
					player.getWorld().setGameRuleValue("naturalRegeneration", "false");
					player.getWorld().setTime(HotPotato.mapConfig.getInt("time", 6000));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 1);
					player.sendTitle(ChatColor.AQUA + "4", "");
				} else if (HotPotato.timesLeft == 3) {
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 1);
					player.sendTitle(ChatColor.BLUE + "3", "");
				} else if (HotPotato.timesLeft == 2) {
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 1);
					player.sendTitle(ChatColor.YELLOW + "2", "");
				} else if (HotPotato.timesLeft == 1) {
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 1);
					player.sendTitle(ChatColor.RED + "1", "");
				} else if (HotPotato.timesLeft == 0) {
					player.setGameMode(GameMode.ADVENTURE);
					if (!once) HotPotato.explodeIn = Utils.getExplodeIn(1);
					if (!once) HotPotato.gameStarted = true;
					if (!once) Utils.teleportAllPlayers();
					if (!once) Utils.roll();
					once = true;
					player.sendTitle("", ""); // delete title
				}
			}
			if (Bukkit.getOnlinePlayers().size() >= Constants.mininumPlayers && HotPotato.timesLeft >= 0) HotPotato.timesLeft--;
		} else if (HotPotato.gameStarted) {
			List<Player> players = new ArrayList<Player>();
			players.addAll(Bukkit.getOnlinePlayers());
			players.removeIf(player -> {
				return HotPotato.teamMap.get(player.getUniqueId()) == Teams.SPECTATOR;
			});
			if (players.size() <= 1) {
				this.cancel();
				Utils.endGame();
				return;
			}
			if (HotPotato.cooldown <= 0) {
				Log.info("Rolling..."); // TODO: debug
				HotPotato.roundEnded = false;
				HotPotato.cooldown = 5;
				Utils.roll();
			}
			if (HotPotato.roundEnded) {
				HotPotato.cooldown--;
				return;
			}
			if (HotPotato.explodeIn <= 0) {
				Utils.explodeITs();
				HotPotato.roundEnded = true;
				HotPotato.cooldown = 5;
				return;
			}
			for (Player player : Bukkit.getOnlinePlayers()) {
				final Scoreboard scoreboard = HotPotato.scoreboardMap.get(player.getUniqueId());
				Objective objective3 = scoreboard.getObjective(DisplaySlot.SIDEBAR);
				String leftSecondPlayed = Integer.toString(HotPotato.playedTime % 60);
				if (leftSecondPlayed.length() == 1) leftSecondPlayed = "0" + leftSecondPlayed;
				String lastScore4 = HotPotato.lastScore4Map.get(player.getUniqueId());
				scoreboard.resetScores(lastScore4);
				lastScore4 = ChatColor.GREEN + "    Explode in " + HotPotato.explodeIn + " seconds";
				HotPotato.lastScore4Map.put(player.getUniqueId(), lastScore4);
				Score score4 = objective3.getScore(lastScore4);
				score4.setScore(4);
				Score score0 = objective3.getScore("     ");
				score0.setScore(0);
				player.setScoreboard(scoreboard);
			}
			HotPotato.playedTime++;
			HotPotato.explodeIn--;
		}
		if (HotPotato.debug) {
			long end = System.currentTimeMillis()-time;
			Log.debug("Scoreboard update tick took " + end + "ms");
		}
	}
}
