package net.amoebaman.gametdm;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import net.amoebaman.gamemaster.GameMaster;
import net.amoebaman.gamemaster.api.GameMap;
import net.amoebaman.gamemaster.api.Simple;
import net.amoebaman.gamemaster.api.TeamAutoGame;
import net.amoebaman.gamemaster.enums.PlayerStatus;
import net.amoebaman.gamemaster.enums.Team;
import net.amoebaman.gamemaster.modules.RespawnModule;
import net.amoebaman.gamemaster.modules.TimerModule;
import net.amoebaman.utils.CommandController;
import net.amoebaman.utils.GenUtil;
import net.amoebaman.utils.chat.Chat;
import net.amoebaman.utils.chat.Message;
import net.amoebaman.utils.chat.Scheme;


// This is a simple Team-Deathmatch gametype, to illustrate how the GameMaster API can be used
// to create games quickly and simply, adding depth and intricacies only where needed.
public class GameTDM extends TeamAutoGame implements Listener, RespawnModule, TimerModule{
	
	public void onEnable(){
		
		// This registers the game with GameMaster, letting it know we're here and ready to go
		GameMaster.registerGame(this);
		
		// Obviously you still need to register your events
		Bukkit.getPluginManager().registerEvents(this, this);
		
		// GameMaster includes a class called CommandController, which allows you to write your commands
		// as methods identified by an annotation, just like how event handlers are.  Register your commands
		// with the command controller like this.
		//
		// Yes, if you like you can actually hijack and override commands defined by other plugins using this
		// system, so long as those plugins are loaded before yours (so that their commands are loaded).
		CommandController.registerCommands(this);
	}
	
	// GameMaster lets us define "aliases" for our game, which can be used when looking it up
	// in addition to its "real" name.  Basically, any one of the aliases can be used as a
	// substitute for the game's full name in commands, most noticeably when voting for a game.
	public String[] getAliases() { return new String[]{ "tdm", "teamdeathmatch" }; }
	
	// GameMaster doesn't explicitly bind maps to games - instead, it leaves it to the game
	// to decide based on the map's data whether it has the right data to run on said map.
	//
	// In our case, the prerequisites for TDM to run on a map are very simple: we need two or
	// more teams to be defined, with spawn points set for them.  As it happens, TeamAutoGame
	// already checks this for us before consulting us, so there's nothing more we need
	// to verify.
	public boolean hasCompatibility(GameMap map) { return true; }
	
	// The RespawnModule will automatically send players to this location when they die, while
	// they wait to respawn.  You should make this the location to some kind of lobby, or
	// observation platform or something like that.
	//
	// There's nothing wrong with just snagging a conveniently already-defined waiting
	// location, now is there?  ;)
	public Location getWaitingLoc(Player player) { return GameMaster.mainLobby; }
	
	// This is the time in seconds that it should take a player to respawn.  Note that you don't
	// necessarily have to return just a number - you could return a different value based on which
	// team the player is on, whether their team is in the lead, etc.
	public int getRespawnDelay(Player player) { return 5; }
	
	// This is the location that the player will respawn to after the waiting time has fully
	// elapsed.  GameMaster's provides a Simple method for doing this, which will simply return
	// the spawn point for the player's team, defined by the map.
	public Location getRespawnLoc(Player player) { return Simple.getRespawnLoc(player, this); }
	
	// We also need to specify a location that the SafeSpawnModule uses for applying spawn
	// invulnerability.  Some games might want to respawn the player outside of their traditional
	// spawn location without actually giving them protection.
	public Location getSafeLoc(Player player){ return getRespawnLoc(player); }
	
	// This is the time period in seconds after the player respawns when they will be fully
	// invulnerable to any damage whatsoever.  GameMaster uses Minecraft's built-in no-damage-ticks
	// function for this, which is basically impossible for anything to override.
	public int getRespawnInvuln(Player player) { return 5; }
	
	// TeamAutoGame automatically implements the SafeSpawnModule, which protects players that are
	// inside their spawns.  The location of their spawn is the same location given for respawning,
	// taken from the method above.  This is the radius of the spawn, spherical, within which
	// spawn protection is active.
	public int getSafeRadius(Player player) { return 7; }
	
	// Because spawns are protected, GameMaster asks you for this to prevent people from running and
	// hiding from spawn immediately after combat.  If a player tries to re-enter spawn too soon after
	// being damaged by an enemy, they'll be slapped back out.
	public int getSafeReentryTimeout(Player player) { return 5; }
	
	// This returns the color that the player's name should be in the TAB list.  The Simple method
	// for this just returns the player's team color.  This color is NOT the color over the player's
	// head - that color is reserved for the player's team color.
	public ChatColor getNameColor(Player player) { return Simple.getNameColor(player, this); }
	
	// This lets you return the lines that will be displayed after the basic game and team info
	// when a player uses /game (or /g).  The Simple method for this returns a line for
	// each team plainly stating how many points they have, which is fine for our purposes.
	// If your game had other objectives, you could add text to inform the player about
	// their statuses here.
	//
	// GameMaster automatically centers these messages, and formats them to the Highlight color
	// scheme.  Check out the ChatUtils documentation for details.
	public List<?> getStatus(Player player) { return Simple.getStatus(this); }
	
	// This is called to add a player to the game, either at the very beginning of the game,
	// or when they've just logged in.  The Simple method for this just finds the team with
	// the least players, adds them to it, and sends them to its spawn point.
	public void addPlayer(Player player) { Simple.addPlayer(player, this); }
	
	// This is called to remove a player from the game, either when it ends or when they
	// leave the server.  The Simple method for this just removes the player from their
	// team, and does nothing else.
	public void removePlayer(Player player) { Simple.removePlayer(player, this); }
	
	// This is called to make an attempt to balance the teams.  The Simple method for this will
	// make a standard effort to make sure all teams have nearly the same amount of players, which
	// is fine for most games.  You can modify this if you have a game where one team is supposed
	// to have more players than another.
	public void balanceTeams() { Simple.balanceTeams(this); }
	
	// This is really only used by admin actions at this point, and there's not really a point
	// to modifying it.  Don't worry about this, and just use the Simple method.
	public void changeTeam(Player player) { Simple.changeTeam(player, this); }
	
	// This gets the time limit of the game in minutes, used by the TimerModule.  After this many
	// minutes have elapsed, the game will be ended by the GameMaster.
	public int getGameLength() { return 10; }
	
	// This method is called to start the game, and perform any operations necessary for doing so.
	// All the simple method does is set all teams' scores to zero, assign all players to a team,
	// warp them in to their spawn location, and broadcast a big message announcing the start.
	public void start() { Simple.start(this); }
	
	// This method is called when the game's time limit has elapsed, as part of TimerModule.  The
	// Simple method for getting the leader just looks for the team with the highest score, and
	// returns Neutral if it's a draw.  The Simple method for ending the game ends the game, returns
	// everybody to the lobby, awards wins, losses, and victory charges via the StatMaster, sends
	// out a big fat message with the winning team, removes teams from the scoreboard, shoots off
	// some colored fireworks for the winning team, and launches the intermission period.
	//
	// That's a mouthfull, but if you're going to re-write this method without the Simple bit,
	// you really need to do most of that stuff.  Also, you need to do stuff like cleanup if
	// you've adjusted the map at all.
	public void end() { Simple.end(Simple.getLeader(this), this); }
	
	// This is the panic method, which is used when the server is shutting down, something has gone
	// horribly wrong, or an admin has ended the game.  Use this to do stuff like reparing the map
	// (unless you're smart and used GameMaster's auto-repairing wizardry), and general cleanup.
	// The Simple method just broadcasts a message saying that the game was aborted.
	public void abort() { Simple.abort(this); }
	
	// This is Team Deathmatch, which is about the simplest game type you can possibly conceive of.  The only
	// thing we actually have to to do is listen for player deaths, and increment their killer's team's score
	// when they happen.  Additionally, here we're sending out notifications of progress every 5 kills.
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
				new Message(Scheme.HIGHLIGHT)
					.then("The ")
					.then(team).color(team.chat)
						.tooltip(Chat.format("&zMembers:~" + GenUtil.concat(getPlayers(team), "&x  ", "~&x  ", ""), Scheme.NORMAL).split("~"))
					.then(" team has ")
					.then(getScore(team)).strong()
					.then(" points")
					.broadcast();
		}
	}
	
}
