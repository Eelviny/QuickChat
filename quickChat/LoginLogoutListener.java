package quickChat;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LoginLogoutListener implements Listener {

	private HashMap<String, String> messageData = QuickChat.getMessageData();

	@EventHandler(priority = EventPriority.NORMAL)
	public synchronized void onLogin(PlayerJoinEvent event){
		synchronized(chatListener.class){
			Player player = event.getPlayer();
			LinkedHashMap<String, Channel> channels = QuickChat.getChannels();
			if(QuickChat.getPlayerChannels().containsKey(player.getDisplayName())){
				QuickChat.removeLastPlayers(player.getDisplayName());
			}
			
			QuickChat.addLastPlayers(player.getDisplayName(), "Null");
			QuickChat.addIgnoredPlayer(player.getDisplayName());
			
			for(Channel channel: channels.values()){
				if(player.hasPermission("quickchat.channel." + channel.getName()) || player.hasPermission("quickchat.channel")){
					channel.addPlayer(player.getDisplayName());
					QuickChat.getConsole().sendMessage("[QuickChat] "
							+ messageData.get("quickchat.console.joinchannel").replace("%player%", player.getDisplayName())
									.replace("%channel%", channel.getName()));
					break;
				}
			}
			if(channels.get(getChannel(player.getDisplayName())) == null){
				QuickChat.getConsole().sendMessage("[QuickChat] "
						+ messageData.get("quickchat.console.joinnull").replace("%player%", player.getDisplayName()));
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public synchronized void onLogout(PlayerQuitEvent event){
		synchronized(chatListener.class){
			Player player = event.getPlayer();

			QuickChat.removeLastPlayers(player.getDisplayName());
			QuickChat.removeIgnoredPlayer(player.getDisplayName());

			String channelName = getChannel(player.getDisplayName());
			if(channelName != "Null") QuickChat.removePlayerFromChannel(channelName, player.getDisplayName());
			QuickChat.getConsole().sendMessage("[QuickChat] "
					+ messageData.get("quickchat.console.remove").replace("%player%", player.getDisplayName()));
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public synchronized void onKick(PlayerKickEvent event){
		synchronized(chatListener.class){
			Player player = event.getPlayer();

			QuickChat.removeLastPlayers(player.getDisplayName());
			QuickChat.removeIgnoredPlayer(player.getDisplayName());

			String channelName = getChannel(player.getDisplayName());
			if(channelName != "Null") QuickChat.removePlayerFromChannel(channelName, player.getDisplayName());
			QuickChat.getConsole().sendMessage("[QuickChat] "
					+ messageData.get("quickchat.console.remove").replace("%player%", player.getDisplayName()));
		}
	}

	private static String getChannel(String player){
		HashMap<String, Channel> channels = QuickChat.getChannels();

		for(Channel channel: channels.values()){
			for(String playerInChannel: channel.getplayers()){
				if(playerInChannel.equals(player)){
					return channel.getName();
				}
			}
		}
		return "Null";
	}

}
