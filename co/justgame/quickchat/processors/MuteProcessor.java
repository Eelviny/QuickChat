package co.justgame.quickchat.processors;

import java.util.List;

import org.bukkit.entity.Player;

import co.justgame.quickchat.collections.MutedPlayers;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.ProcessorUtils;


public class MuteProcessor implements MessageData{
    
    public static void processMuteCommand(String message, Player sender){
        /*
         * This processor statement handles the chat message "-<Player>"
         */
        if(!message.contains(" ")){
            //find all matches of the requested player
            List<Player> matches = ProcessorUtils.matchPlayer(message.replace("-", ""));
            
            //if there is only one match for the requested player
            if(ProcessorUtils.thereIsOnlyOneMatch(matches, sender, message.replace("-", ""))){
                //get the matching player
                Player p = matches.get(0);
                //if the player is already muted
                if(MutedPlayers.isMuted(p)){
                    //unmute the player
                    MutedPlayers.unmute(p);
                    //tell the sender that requested player was unmuted
                    sender.sendMessage(messageData.get("quickchat.mute.unmute").replace("%p%", p.getName()));
                    //tell the player he was unmuted
                    p.sendMessage(messageData.get("quickchat.mute.otherunmute"));
                // else if the player is not already muted
                }else{ 
                    //mute the player
                    MutedPlayers.mute(p); 
                    //tell the sender that requested player was muted
                    sender.sendMessage(messageData.get("quickchat.mute.mute").replace("%p%", p.getName()));
                    //tell the player he was muted
                    p.sendMessage(messageData.get("quickchat.mute.othermute"));
                }
            }
        //if the command is entered with incorect spacing, send an error message
        }else sender.sendMessage(messageData.get("quickchat.mute.usage"));
    }
}
