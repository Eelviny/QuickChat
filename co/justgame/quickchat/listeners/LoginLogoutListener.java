package co.justgame.quickchat.listeners;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import co.justgame.quickchat.channel.ChannelUtils;
import co.justgame.quickchat.main.QuickChat;

public class LoginLogoutListener implements Listener {

    private HashMap<String, String> messageData = QuickChat.getMessageData();

    @EventHandler(priority = EventPriority.NORMAL)
    public synchronized void onLogin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        QuickChat.addLastPlayers(player.getDisplayName(), "Null");
        QuickChat.addIgnoredPlayer(player.getDisplayName());

        ChannelUtils.addPlayerToFirstAvailableChannel(player);
        if(ChannelUtils.getFullChannel(player.getName()) == null){
            QuickChat.getConsole().sendMessage("[QuickChat] "
                    + messageData.get("quickchat.console.joinnull").replace("%player%", player.getDisplayName()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public synchronized void onLogout(PlayerQuitEvent event){
        Player player = event.getPlayer();

        QuickChat.removeLastPlayers(player.getDisplayName());
        QuickChat.removeIgnoredPlayer(player.getDisplayName());

        String channelName = ChannelUtils.getChannel(player.getDisplayName());
        if(QuickChat.getPlayerChannels().containsKey(player.getDisplayName()))
            QuickChat.removePlayerChannel(player.getDisplayName());
        if(channelName != "Null") ChannelUtils.removePlayerFromChannel(player.getName());
        QuickChat.getConsole().sendMessage("[QuickChat] "
                + messageData.get("quickchat.console.remove").replace("%player%", player.getDisplayName()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public synchronized void onKick(PlayerKickEvent event){
        Player player = event.getPlayer();

        QuickChat.removeLastPlayers(player.getDisplayName());
        QuickChat.removeIgnoredPlayer(player.getDisplayName());

        String channelName = ChannelUtils.getChannel(player.getDisplayName());
        if(channelName != "Null") ChannelUtils.removePlayerFromChannel(player.getName());
        if(QuickChat.getPlayerChannels().containsKey(player.getDisplayName()))
            QuickChat.removePlayerChannel(player.getDisplayName());
        QuickChat.getConsole().sendMessage("[QuickChat] "
                + messageData.get("quickchat.console.remove").replace("%player%", player.getDisplayName()));
    }
}
