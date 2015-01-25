package co.justgame.quickchat.utils;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import co.justgame.quickchat.main.QuickChat;


public class PrefixSuffixUtils {
    
    private static Chat chat = null;
    
    public static void setupChat(){
        Server s = QuickChat.getInstance().getServer();
        
        if(s.getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Chat> rsp = s.getServicesManager()
                    .getRegistration(net.milkbowl.vault.chat.Chat.class);
            if(rsp != null){
                chat = rsp.getProvider();
            }
        }
    }
    
    public static synchronized String getPlayerPrefix(Player player){
        if(chat != null){
            if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine") != null){
                if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine").isEnabled()){
                    String world = null;
                    try{
                        if(chat.getPlayerInfoString(world, player.getDisplayName(), "prefix", "") != null){
                            return MessageFormatUtils
                                    .formatString(chat.getPlayerInfoString(world, player.getDisplayName(), "prefix", ""));
                        }else{
                            return "";
                        }
                    }catch (Exception e){
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] "
                                        + MessageFormatUtils.formatString("&cCaught Exception: " + e.toString() + " Message:"
                                                + e.getMessage()));
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] " + MessageFormatUtils.formatString("&cCause: " + e.getCause()));
                        return "";
                    }
                }
            }

            if(chat.getPlayerPrefix(player) != null){
                return MessageFormatUtils.formatString(chat.getPlayerPrefix(player));
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public static synchronized String getPlayerSuffix(Player player){
        if(chat != null){
            if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine") != null){
                if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine").isEnabled()){
                    String world = null;
                    try{
                        if(chat.getPlayerInfoString(world, player.getDisplayName(), "suffix", "") != null){
                            return MessageFormatUtils
                                    .formatString(chat.getPlayerInfoString(world, player.getDisplayName(), "suffix", ""));
                        }else{
                            return "";
                        }
                    }catch (Exception e){
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] "
                                        + MessageFormatUtils.formatString("&cCaught Exception: " + e.toString() + " Message:"
                                                + e.getMessage()));
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] " + MessageFormatUtils.formatString("&cCause: " + e.getCause()));
                        return "";
                    }
                }
            }

            if(chat.getPlayerSuffix(player) != null){
                return MessageFormatUtils.formatString(chat.getPlayerSuffix(player));
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

}
