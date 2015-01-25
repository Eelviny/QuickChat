package co.justgame.quickchat.processors;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.MessageFormatUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class AnonymousMessageProcessor implements MessageData{
    
    final static UUID ConsoleUUID = QuickChat.getConsoleUUID();
    
    public static void processAnonymousMessageCommand(String message, Player sender){
        //get sender's ID
        UUID sendersID = sender.getUniqueId();

        //format the message
        message = MessageFormatUtils.formatCodes(sender, message.replaceFirst(">", ""));
        
        if(message.charAt(0) == ' '){
            /*
             * This If statement handles the chat message "> <Message>"
             */
            
            //send the message to console
            QuickChat.getConsole().sendMessage(messageData.get("quickchat.console.rawtext")
                    .replace("%player%", ProcessorUtils.getDisplayName(sendersID)).replace("%message%", message.trim()));
    
            
            //send the message to every player on the server
            for(Player reciever: Bukkit.getOnlinePlayers()){
                reciever.sendMessage(message.trim());
            }
        }else if(message.charAt(0) != ' '){
            /*
             * This If statement handles the chat message "><Player> <Message>"
             */
            
            //split the message into two parts
            String[] splitMessage = message.split(" ", 2);
    
            //if the message is incorrectly formatted, send an error message
            if(splitMessage.length == 1){
                sender.sendMessage(messageData.get("quickchat.rawtext.improperusage"));
            //else if it is formatted correctly, continue
            }else{
                //get the requested player and the message
                String playerName = splitMessage[0];
                String sendMessage = splitMessage[1];
                //if the requested player is the Console
                if(playerName.equalsIgnoreCase("console")){
                    //tell the sender the message sent
                    sender.sendMessage("<You (raw text) -> " + "Console" + "> " + sendMessage);
                    //send the message to the console
                    QuickChat.getConsole().sendMessage(sendMessage);
                    //record the console as the last player the sender messaged
                    LastPlayers.addLastPlayers(ConsoleUUID, sendersID);
                //else if the requested player is not the console
                }else{
                    //get a list of players that match the requested player
                    List<Player> players = ProcessorUtils.matchPlayer(playerName);
    
                    //if there is only one match for the requested player
                    if(ProcessorUtils.thereIsOnlyOneMatch(players, sender, playerName)){
                        //get the requested player
                        Player reciever = players.get(0);
                        //tell the sender that his message was sent
                        sender.sendMessage("<You (raw text) -> " + reciever.getDisplayName() + "> " + sendMessage);
                        //record the message in the console
                        QuickChat.getConsole().sendMessage("<" + ProcessorUtils.getDisplayName(sendersID) + " (raw text) -> "
                                + reciever.getDisplayName() + "> " + sendMessage);
                        //if the reciever is not the same person as the sender, send the message to the reciever
                        if(!reciever.equals(sender)){
                            reciever.sendMessage(sendMessage);
                            //record the reciever as the last player the sender messaged
                            LastPlayers.addLastPlayers(reciever.getUniqueId(), sendersID);
                        }
                    }
                }
            }
        }
    }
}
