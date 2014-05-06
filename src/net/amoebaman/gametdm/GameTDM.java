package net.amoebaman.gametdm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.amoebaman.amoebautils.CommandController;
import net.amoebaman.amoebautils.AmoebaUtils;
import net.amoebaman.amoebautils.chat.Chat;
import net.amoebaman.amoebautils.chat.Message;
import net.amoebaman.amoebautils.chat.Scheme;
import net.amoebaman.gamemasterv3.api.TeamAutoGame;
import net.amoebaman.gamemasterv3.enums.PlayerState;
import net.amoebaman.gamemasterv3.enums.Team;
import net.amoebaman.gamemasterv3.modules.RespawnModule;
import net.amoebaman.gamemasterv3.modules.SafeSpawnModule;
import net.amoebaman.gamemasterv3.modules.TimerModule;

// This is a simple Team-Deathmatch gametype, to illustrate how the GameMaster API can be used
// to create games quickly and simply, adding depth and intricacies only where needed.
//
// With the release of GameMaster v3, TeamAutoGame already implements just about everything, so
// so there's no need for redundant stub methods that just redirect to the old Simple static
// methods.  You can still override them if you want though, and you can call on the default
// behaviors with super calls.
public class GameTDM extends TeamAutoGame implements Listener, SafeSpawnModule, RespawnModule, TimerModule{
	
	public void onEnable(){
		
		// This registers the game with GameMaster, letting it know we're here and ready to go
		register();
		
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
	
	// TeamAutoGame provides a default for this value as well, but it's ten seconds and for a game like TDM
	// that's a bit on the long side.  We override it so we can let players respawn sooner.
	public int getRespawnDelay(Player player) { return 5; }
	
	// This is Team Deathmatch, which is about the simplest game type you can possibly conceive of.  The only
	// thing we actually have to to do is listen for player deaths, and increment their killer's team's score
	// when they happen.  Additionally, here we're sending out notifications of progress every 5 kills.
	@EventHandler
	public void onEntityDeath(PlayerDeathEvent event){
		Player victim = event.getEntity();
		/*
		 * We don't actually care unless the game is active and the victim is playing
		 */
		if(isActive() && master.getState(victim) == PlayerState.PLAYING){
			/*
			 * Our GameMaster has a reference to the PlayerManager, which has a convenient
			 * method for check who actually damaged the player last.
			 */
			Player culprit = master.getPlayerManager().getLastDamager(victim);
			Team team = getTeam(culprit);
			/*
			 * Don't want to glitch out if they were killed by some admin who isn't actually
			 * in the game and doesn't have a team.
			 */
			if(team == null)
				return;
			/*
			 * Increment the score.
			 */
			setScore(team, getScore(team) + 1);
			/*
			 * If the new score is a multiple of 5, broadcast the score.
			 */
			if(getScore(team) % 5 == 0)
				new Message(Scheme.HIGHLIGHT)
					.then("The ")
					.then(team).color(team.chat)
					/*
					 * Messages can do fancy JSON messaging stuff. :D
					 */
						.tooltip(Chat.format("&zMembers:~" + AmoebaUtils.concat(getPlayers(team), "&x  ", "~&x  ", ""), Scheme.NORMAL).split("~"))
					.then(" team has ")
					.then(getScore(team)).strong()
					.then(" points")
					.broadcast();
		}
	}
	
}
