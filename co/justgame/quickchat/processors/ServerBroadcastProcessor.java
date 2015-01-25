package co.justgame.quickchat.processors;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.MessageFormatUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class ServerBroadcastProcessor implements MessageData {
    
    public static String broadCastName = "";

    public static void processServerBroadcastCommand(String message, Player sender){
        
        /*
         * This processor handles the chat message "!<Message>"
         */
        
        //get the players UUID
        UUID sendersID = sender.getUniqueId();
        //format the sent message
        String formatedMessage = MessageFormatUtils.formatCodes(sender, message.replaceFirst("!", ""));

        //send the formated message to the console
        QuickChat.getConsole().sendMessage(messageData.get("quickchat.console.broadcast")
                .replace("%player%", ProcessorUtils.getDisplayName(sendersID))
                .replace("%message%", broadCastName + " " + formatedMessage.trim()));

        //send the formatted message to everyone on the server 
        for(Player reciever: Bukkit.getOnlinePlayers()){
            reciever.sendMessage(broadCastName + " " + formatedMessage.trim());
        }
    }
    
    public static void setBroadCastName(String name){
        broadCastName = name;
    }
    
    public static String getBroadCastName(){
        return broadCastName;
    }
}
