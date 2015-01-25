package co.justgame.quickchat.collections;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.justgame.quickchat.utils.MessageData;

public class MutedPlayers implements Listener, MessageData {
    
    static ArrayList<Player> muted = new ArrayList<Player>();
    
    @EventHandler(priority = EventPriority.LOW)
    public void playerChat(AsyncPlayerChatEvent e){
            if(!e.getMessage().startsWith("@") && !e.getMessage().startsWith("-"))
               if(muted.contains(e.getPlayer())){ 
                   e.setCancelled(true); e.getPlayer().sendMessage(messageData.get("quickchat.mute.message"));
               }
    }
    
    public static void mute(Player p){
        muted.add(p);
    }
    
    public static void unmute(Player p){
        muted.remove(p);
    }
    
    public static boolean isMuted(Player p){
        return muted.contains(p);
    }
}
