package co.justgame.quickchat.processors;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.justgame.quickchat.collections.IgnoredPlayers;
import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.MessageFormatUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class IgnoreProcessor implements MessageData{
    
    public static void processIgnoreCommand(String message, Player sender){
        UUID sendersID = sender.getUniqueId();
        
        /*
         * This If statement handles the chat message "~"
         */
        if(message.equals("~")){
            //create base StringBuilder
            StringBuilder buildMessage = new StringBuilder();

          //get IgnoredPlayers and IgnoringPlayers
            StringBuilder playersIgnored = new StringBuilder(IgnoredPlayers.getIgnoredPlayers(sendersID));
            StringBuilder playersIgnoring = new StringBuilder(IgnoredPlayers.getIgnoringPlayers(sendersID));

            //add Ignored players to message base if not empty
            if(playersIgnored.toString().isEmpty()){
                buildMessage.append(messageData.get("quickchat.ignore.listignored")
                        .replace("%playerList%", " &4No Players Ignored"));
            }else{
                buildMessage.append(messageData.get("quickchat.ignore.listignored")
                        .replace("%playerList%", playersIgnored.toString()));
            }

            //add Ignoring players to message base if not empty
            if(playersIgnoring.toString().isEmpty()){
                buildMessage.append(messageData.get("quickchat.ignore.listignoring")
                        .replace("%playerList%", " &4No Players Ignoring You"));
            }else{
                buildMessage.append(messageData.get("quickchat.ignore.listignoring")
                        .replace("%playerList%", playersIgnoring.toString()));
            }

            //send the Message
            ProcessorUtils.sendMultilineMessage(sender, MessageFormatUtils.formatString(buildMessage.toString()));
            
        }else{
            
            /*
             * This else statement handles the chat message "~<Player>"
             */
            
            //seperate the requested player from the rest of the message;
            String requestedPlayer = message.replace("~", "");
            List<Player> players = ProcessorUtils.matchPlayer(requestedPlayer);

            //if there is only one match for the requested player
            if(ProcessorUtils.thereIsOnlyOneMatch(players, sender, requestedPlayer)){
                //get the players UUID
                Player player = players.get(0);
                UUID playersName = player.getUniqueId();

                //if the player requested is not the same as the sender, continue
                if(!player.equals(sender)){
                    //if the player is not an OP and does not have permission to ignore the ignore command, continue 
                    if(!player.isOp() && !player.hasPermission("quickchat.ignore.deny")){
                        //if the player is not already ignored, contniue
                        if(!IgnoredPlayers.isIgnored(sendersID, playersName)){
                            //ignore the player
                            IgnoredPlayers.addIgnoredPlayerToPlayer(sendersID, playersName);
                            //tell the sender the ignore request was completed
                            sender.sendMessage(messageData.get("quickchat.ignore.ignoring")
                                    .replace("%player%", Bukkit.getPlayer(LastPlayers.getLastPlayer(playersName)).getDisplayName()));
                            //tell the un-ignored player he was ignored
                            player.sendMessage(messageData.get("quickchat.ignore.reciever")
                                    .replace("%player%", Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersID)).getDisplayName()));
                            
                        // else if the player is already ignored
                        }else{
                            //un-ignore the player
                            IgnoredPlayers.removeIgnoredPlayerFromPlayer(sendersID, playersName);
                          //tell the sender the ignore un-request was completed
                            sender.sendMessage(messageData.get("quickchat.ignore.notignoring")
                                    .replace("%player%", Bukkit.getPlayer(LastPlayers.getLastPlayer(playersName)).getDisplayName()));
                          //tell the ignored player he was un-ignored
                            player.sendMessage(messageData.get("quickchat.ignore.recieverun")
                                    .replace("%player%", Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersID)).getDisplayName()));
                        }
                    //if the player cannot be ingored, tell the sender that he cannot ignore the requested player
                    }else{
                        sender.sendMessage(messageData.get("quickchat.ignore.op").replace("%player%", 
                                Bukkit.getPlayer(LastPlayers.getLastPlayer(playersName)).getDisplayName()));
                    }
                //else if the player requested is the same as the sender, send an error message
                }else{
                    sender.sendMessage(messageData.get("quickchat.ignore.self"));
                }
            }
        }
    }
}
