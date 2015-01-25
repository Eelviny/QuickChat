package co.justgame.quickchat.processors;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import co.justgame.quickchat.collections.PlayerChannels;
import co.justgame.quickchat.utils.ChannelUtils;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.ProcessorUtils;


public class PlayerChannelInformationProcessor implements MessageData {
    
    public static void processPlayerChannelInformationCommand(String message, Player sender){
        
        /*
         * This processor handles the chat message "?<Player>"
         */
        
        //get sender ID
        UUID sendersID = sender.getUniqueId();
        
        //seprete the requested player from the rest of the message
        String requestedPlayer = message.replace("?", "");
        
        //if the the requested player is console, send a message
        if(requestedPlayer.equalsIgnoreCase("console")){
            sender.sendMessage(messageData.get("quickchat.info.console"));
        // else if the requested player is not the console, continue
        }else{
            
            //get a list of players that match the requested player
            List<Player> players = ProcessorUtils.matchPlayer(requestedPlayer);
            
            //if there is only one match for the requested player
            if(ProcessorUtils.thereIsOnlyOneMatch(players, sender, message.replace("-", ""))){
                //get the requested player
                Player reciever = players.get(0);

                //if the requested player is in a conversation, tell the sender he is in a conversation
                if(PlayerChannels.playerHasPlayerChannel(sendersID)){
                    sender.sendMessage(messageData.get("quickchat.info.playersconversation").replace("%player%", reciever
                            .getDisplayName()));

                // else return the channel that the requested player is currently in.    
                }else{
                    sender.sendMessage(messageData.get("quickchat.info.playerschannel")
                            .replace("%player%", reciever.getDisplayName())
                            .replace("%channel%", ChannelUtils.getChannel(reciever.getUniqueId())));
                }
            }
        }

    }
}
