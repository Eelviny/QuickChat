package co.justgame.quickchat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.justgame.quickchat.main.QuickChat;


public class ProcessorUtils implements MessageData {
    
    public static void sendMultilineMessage(Player player, String message){
        if(player != null && message != null && player.isOnline()){
            String[] s = message.split("/n");
            for(String m: s){
                player.sendMessage(m);
            }
        }
    }
    
    public static void sendMultilineMessage(CommandSender sender, String message){
        if(sender != null && message != null){
            String[] s = message.split("/n");
            for(String m: s){
                sender.sendMessage(m);
            }
        }
    }
    
    public static boolean thereIsOnlyOneMatch(List<Player> pl, Player sender, String reciever){
      //if there are no matches, send an error message
        if(pl.size() == 0){
            sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", reciever));
        //if there is more than one match, send an error report
        }else if(pl.size() > 1){
            sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
        //if there is only one match, continue
        }else{
            return true;
        }
        return false;
    }
    
    public static String getDisplayName(UUID player){
        if(player.equals(QuickChat.getConsoleUUID()))
            return"Console";
        return Bukkit.getPlayer(player).getDisplayName();
    }
    
    public static ArrayList<Player> matchPlayer(String pn){
        ArrayList<Player> matches = new ArrayList<Player>();
        for(Player p: Bukkit.getOnlinePlayers()){
            if(p.getDisplayName().toLowerCase().startsWith(pn.toLowerCase()))
                matches.add(p);
        }
        return matches;
    }
    
}
