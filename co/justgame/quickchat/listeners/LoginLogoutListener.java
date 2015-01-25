package co.justgame.quickchat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import co.justgame.quickchat.collections.IgnoredPlayers;
import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.collections.PlayerChannels;
import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.utils.ChannelUtils;
import co.justgame.quickchat.utils.MessageData;

public class LoginLogoutListener implements Listener, MessageData {

    @EventHandler(priority = EventPriority.NORMAL)
    public synchronized void onLogin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        LastPlayers.addLastPlayers(player.getUniqueId(), null);
        IgnoredPlayers.addIgnoredPlayer(player.getUniqueId());

        ChannelUtils.addPlayerToFirstAvailableChannel(player);
        if(ChannelUtils.getFullChannel(player.getUniqueId()) == null){
            QuickChat.getConsole().sendMessage("[QuickChat] "
                    + messageData.get("quickchat.console.joinnull").replace("%player%", player.getDisplayName()));
            player.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", "Null"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public synchronized void onLogout(PlayerQuitEvent event){
        Player player = event.getPlayer();

        LastPlayers.removeLastPlayers(player.getDisplayName());
        IgnoredPlayers.removeIgnoredPlayer(player.getDisplayName());

        String channelName = ChannelUtils.getChannel(player.getUniqueId());
        if(PlayerChannels.playerHasPlayerChannel(player.getUniqueId()))
            PlayerChannels.removePlayerChannel(player.getUniqueId());
        if(channelName != "Null") ChannelUtils.removePlayerFromChannel(player.getUniqueId());
        QuickChat.getConsole().sendMessage("[QuickChat] "
                + messageData.get("quickchat.console.remove").replace("%player%", player.getDisplayName()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public synchronized void onKick(PlayerKickEvent event){
        Player player = event.getPlayer();

        LastPlayers.removeLastPlayers(player.getDisplayName());
        IgnoredPlayers.removeIgnoredPlayer(player.getDisplayName());

        String channelName = ChannelUtils.getChannel(player.getUniqueId());
        if(channelName != "Null") ChannelUtils.removePlayerFromChannel(player.getUniqueId());
        if(PlayerChannels.playerHasPlayerChannel(player.getUniqueId()))
            PlayerChannels.removePlayerChannel(player.getUniqueId());
        QuickChat.getConsole().sendMessage("[QuickChat] "
                + messageData.get("quickchat.console.remove").replace("%player%", player.getDisplayName()));
    }
}
