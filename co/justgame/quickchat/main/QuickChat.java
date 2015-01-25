package co.justgame.quickchat.main;

import java.util.UUID;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import co.justgame.quickchat.collections.IgnoredPlayers;
import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.collections.MutedPlayers;
import co.justgame.quickchat.listeners.ChatListener;
import co.justgame.quickchat.listeners.LoginLogoutListener;
import co.justgame.quickchat.processors.ConsoleCommandProcessor;
import co.justgame.quickchat.utils.ChannelUtils;
import co.justgame.quickchat.utils.ConfigUtils;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.PrefixSuffixUtils;

public class QuickChat extends JavaPlugin implements MessageData {
    
    public static final UUID ConsoleUUID = UUID.randomUUID();
    
    public static Plugin quickChat;
    public static Chat chat = null;

    @Override
    public void onEnable(){
        getLogger().info("QuickChat has been enabled");
        quickChat = this;
        
        ConfigUtils.setUpConfig(quickChat);
        PrefixSuffixUtils.setupChat();

        for(Player player: Bukkit.getOnlinePlayers()){
            LastPlayers.addLastPlayers(player.getUniqueId(), null);
            IgnoredPlayers.addIgnoredPlayer(player.getUniqueId());
            ChannelUtils.addPlayerToFirstAvailableChannel(player);
        }

        LastPlayers.addLastPlayers(ConsoleUUID, null);

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new LoginLogoutListener(), this);
        getServer().getPluginManager().registerEvents(new MutedPlayers(), this);

    }

    @Override
    public void onDisable(){
        getLogger().info("QuickChat has been disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
       return ConsoleCommandProcessor.processConsoleCommand(sender, cmd, label, args);
    }

    public static synchronized ConsoleCommandSender getConsole(){
        return Bukkit.getServer().getConsoleSender();
    }

    public static synchronized Plugin getInstance(){
        return quickChat;
    }
    
    public static synchronized UUID getConsoleUUID(){
        return ConsoleUUID;
    }
}
