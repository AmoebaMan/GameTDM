package net.amoebaman.gametdm;

import java.util.List;

import net.amoebaman.gamemaster.api.TeamAutoGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import com.google.common.collect.Lists;

public class GameTDM extends TeamAutoGame implements Listener, MessagerModule, RespawnModule, TimerModule{
	
	public void onEnable(){
		GameMaster.registerGame(this);
		Bukkit.getPluginManager().registerEvents(this, this);
		CommandController.registerCommands(this, GameMaster.plugin());
	}
	
	public String[] getAliases() { return new String[]{ "tdm", "teamdeathmatch" }; }
	
	public boolean team_isCompatible(GameMap map) { return true; }
	
	public Location getWaitingLoc(Player player) { return GameMaster.mainLobby; }
	
	public int getRespawnSeconds(Player player) { return 5; }
	
	public Location getRespawnLoc(Player player) { return Simple.getRespawnLoc(player, this); }
	
	public int getRespawnInvulnSeconds(Player player) { return 5; }
	
	public int getSpawnRadius(Player player) { return 7; }
	
	public int getSpawnReentryDelaySeconds(Player player) { return 5; }
	
	public ChatColor getNameColor(Player player) { return Simple.getNameColor(player, this); }
	
	public List<String> getStatus(Player player) { return Simple.getStatus(this); }
	
	public void addPlayer(Player player) { Simple.addPlayer(player, this); }
	
	public void removePlayer(Player player) { Simple.removePlayer(player, this); }
	
	public void balanceTeams() { Simple.balanceTeams(this); }
	
	public void changeTeam(Player player) { Simple.changeTeam(player, this); }
	
	public List<String> getWelcomeMessage(Player inContext) { return Lists.newArrayList(); }
	
	public List<String> getSpawnMessage(Player inContext) { return Lists.newArrayList(); }
	
	public int getGameLengthMinutes() { return 10; }
	
	public Team getLeader() { return Simple.getLeader(this); }
	
	public void start() { Simple.start(this); }
	
	public void end() { Simple.end(getLeader(), this); }
	
	public void abort() { Simple.abort(this); }
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event){
		if(!(event.getEntity() instanceof Player))
			return;
		Player victim = (Player) event.getEntity();
		if(isActive() && GameMaster.getStatus(victim) == PlayerStatus.PLAYING){
			Player killer = GameMaster.getKiller(victim);
			Team team = getTeam(killer);
			if(team == null)
				return;
			setScore(team, getScore(team) + 1);
			if(getScore(team) % 5 == 0)
				Bukkit.broadcastMessage(ChatUtils.format("The " + team.chat + team + "]] team has [[" + getScore(team) + "]] points", ColorScheme.HIGHLIGHT));
		}
	}
	
}
