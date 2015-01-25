package co.justgame.quickchat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.processors.AnonymousMessageProcessor;
import co.justgame.quickchat.processors.ChannelProcessor;
import co.justgame.quickchat.processors.DefaultChatProcessor;
import co.justgame.quickchat.processors.IgnoreProcessor;
import co.justgame.quickchat.processors.MuteProcessor;
import co.justgame.quickchat.processors.PlayerChannelInformationProcessor;
import co.justgame.quickchat.processors.PrivateMessageProcessor;
import co.justgame.quickchat.processors.ServerBroadcastProcessor;


public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public synchronized void playerChat(AsyncPlayerChatEvent e){
        if(!e.isCancelled()){
            e.setCancelled(true);
            process(e); 
        }
    };
    
    private void process(final AsyncPlayerChatEvent e){
        Bukkit.getScheduler().scheduleSyncDelayedTask(QuickChat.getInstance(), new Runnable(){
            public void run(){
                
                Player sender = e.getPlayer();
                String message = e.getMessage();

                if(message.startsWith("#")){
                   ChannelProcessor.processChannelCommand(message, sender);
                }else if(message.startsWith("@")){
                    PrivateMessageProcessor.processPrivateMessageCommand(message, sender);                    
                }else if(message.startsWith("!") && !message.equals("!") && sender.hasPermission("quickchat.broadcast")){
                    ServerBroadcastProcessor.processServerBroadcastCommand(message, sender);
                }else if(message.startsWith(">") && sender.hasPermission("quickchat.rawtext") && !message.equals(">")){
                    AnonymousMessageProcessor.processAnonymousMessageCommand(message, sender);
                }else if(message.startsWith("?") && !message.equals("?") && !message.contains(" ")){
                    PlayerChannelInformationProcessor.processPlayerChannelInformationCommand(message, sender);
                }else if(message.startsWith("~") && !message.contains(" ") && sender.hasPermission("quickchat.ignore")){
                   IgnoreProcessor.processIgnoreCommand(message, sender);
                }else if(message.startsWith("-") && sender.hasPermission("quickchat.mute") && !message.equals("-")){
                   MuteProcessor.processMuteCommand(message, sender);
                }else{
                    DefaultChatProcessor.processChat(message, sender);
                }
            }
        });
    }
}
