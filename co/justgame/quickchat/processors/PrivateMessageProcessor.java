package co.justgame.quickchat.processors;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.justgame.quickchat.collections.IgnoredPlayers;
import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.collections.PlayerChannels;
import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.utils.ChannelUtils;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.MessageFormatUtils;
import co.justgame.quickchat.utils.PingUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class PrivateMessageProcessor implements MessageData{
    
    final static UUID ConsoleUUID = QuickChat.getConsoleUUID();
    public static ChatColor privateColor = ChatColor.LIGHT_PURPLE;

    public static void processPrivateMessageCommand(String message, Player sender){
        //get the sender's ID
        UUID sendersID = sender.getUniqueId();
        
        if(message.equals("@")){
        /*
         * This processor handles the chat message "@"
         */
    
            //create the base message on which to build
            StringBuilder buildMessage = new StringBuilder();
    
            //if the the sender has a last player add it to the message
            if(LastPlayers.getLastPlayer(sendersID) != null){
                buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", 
                        ProcessorUtils.getDisplayName(LastPlayers.getLastPlayer(sendersID))));
            }else{
                buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", "&4None"));
            }
    
            //if the sender is currently in a conversation add the sender's partner to the list
            if(PlayerChannels.playerHasPlayerChannel(sendersID)){
                UUID otherPlayer = PlayerChannels.getPartner(sendersID);
                buildMessage.append(messageData.get("quickchat.private.conversation")
                        .replace("%player%", ProcessorUtils.getDisplayName(otherPlayer)));
            }else{
                buildMessage.append(messageData.get("quickchat.private.conversation").replace("%player%", "&4None"));
            }
    
            //send the final message
            ProcessorUtils.sendMultilineMessage(sender, MessageFormatUtils.formatString(buildMessage.toString()));
    
        }else if(!message.contains(" ")){
        /*
         * This processor handles the chat message "@<Player>"
         */
            
            //seperate the reciever from the rest of the message
            String reciever = message.replaceFirst("@", "");
    
            //if the reciever is the console
            if(reciever.equalsIgnoreCase("console")){
                
                //if the sender is in a conversation and the conversation partner is the Console, send an error message 
                if(PlayerChannels.playerHasPlayerChannel(sendersID) && PlayerChannels.getPartner(sendersID).equals(ConsoleUUID)){
                    sender.sendMessage(messageData.get("quickchat.private.samechannel")
                            .replace("%player%", "the Console"));
                //else if there are any other circumstances, continue
                }else{
                    //start a conversation with the console
                    PlayerChannels.addPlayerChannel(sendersID, ConsoleUUID);
                    //tell the sender that he successfully joined a conversation
                    sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                            .replace("%player%", "the Console"));
                    //record to the console that the sender joined a conversation
                    QuickChat.getConsole().sendMessage("[QuickChat] "
                            + messageData.get("quickchat.console.joinplayerchannel")
                            .replace("%player%", ProcessorUtils.getDisplayName(sendersID))
                            .replace("%otherplayer%", "the Console"));
                }
            //else if the reciever is not the console
            }else{
    
                //find all matches of the requested player
                List<Player> players = ProcessorUtils.matchPlayer(reciever);
                
                //if there is only one match for the requested player
                if(ProcessorUtils.thereIsOnlyOneMatch(players, sender, reciever)){
                    //if the sender is in a channel, remove the sender from the channel
                    if(ChannelUtils.getChannel(sendersID) != "Null"){
                        ChannelUtils.getFullChannel(sendersID).removePlayer(sendersID);
                    }
    
                    //get the requested player
                    UUID otherPlayersName = players.get(0).getUniqueId();
                    
                    //if the sender is in a conversation and the conversation partner is the sender, send an error message 
                    if(PlayerChannels.playerHasPlayerChannel(sendersID) && PlayerChannels.getPartner(sendersID).equals(otherPlayersName)){
                        sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                .replace("%player%", ProcessorUtils.getDisplayName(otherPlayersName)));
                    //else if there are any other circumstances, continue
                    }else{
                        //if the sender is not being ignored by the other player
                        if(!IgnoredPlayers.isIgnored(sendersID, otherPlayersName)){
                            //create a new converstaion
                            PlayerChannels.addPlayerChannel(sendersID, otherPlayersName);

                            //record the creation of a new converstaion to the console
                            QuickChat.getConsole().sendMessage("[QuickChat] "
                                    + messageData.get("quickchat.console.joinplayerchannel")
                                            .replace("%player%", Bukkit.getPlayer(sendersID).getDisplayName())
                                            .replace("%otherplayer%", ProcessorUtils.getDisplayName(otherPlayersName)));
                            //tell the sender that the conversation was created
                            sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                    .replace("%player%", players.get(0).getDisplayName()));
                        //else if the sender is being ignored by the other player, tell the sender
                        }else{
                            sender.sendMessage(messageData.get("quickchat.ignore.channel")
                                    .replace("%player%", players.get(0).getDisplayName()));
                        }
                    }
                }
            }
            // Message: @ <Message>
        }else if(message.charAt(1) == ' '){
            String sendMessage = message.replaceFirst("@", "");
    
            sendMessage = MessageFormatUtils.formatCodes(sender, sendMessage);
    
            if(LastPlayers.getLastPlayer(sendersID) != null){
                if(LastPlayers.getLastPlayer(sendersID).equals(ConsoleUUID)){
                    String fullMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> "
                            + "Console" + privateColor + ">§r " + sendMessage;
                    sender.sendMessage(fullMessage);
                    QuickChat.getConsole().sendMessage(fullMessage);
                    LastPlayers.addLastPlayers(ConsoleUUID, sendersID);
                }else{
                    Player reciever = Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersID));
    
                    if(reciever != null){
                        if(IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId())){
                            sender.sendMessage(messageData.get("quickchat.ignore.message")
                                    .replace("%player%", reciever.getDisplayName()));
                        }else{
                            if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                    && !IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId()))
                                sendMessage = PingUtils.ping(reciever, sendMessage);
    
                            String fullMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> "
                                    + reciever.getDisplayName() + privateColor + ">§r" + sendMessage;
                            if(!reciever.equals(sender) && !IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId()))
                                reciever.sendMessage(fullMessage);
                            else if(IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId()))
                                sender.sendMessage(messageData.get("quickchat.ignore.message")
                                        .replaceAll("%player%", reciever.getDisplayName()));
    
                            sender.sendMessage(fullMessage);
    
                            QuickChat.getConsole().sendMessage(fullMessage);
                            LastPlayers.addLastPlayers(reciever.getUniqueId(), sendersID);
                        }
                    }else{
                        sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                .replace("%player%", Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersID)).getDisplayName()));
                    }
                }
            }else{
                sender.sendMessage(messageData.get("quickchat.private.nolastplayer"));
            }
            // Message: @<Player> <Message>
        }else{
            String[] splitMessage = message.split(" ", 2);
            String playerName = splitMessage[0].replaceFirst("@", "");
            String sendMessage = splitMessage[1];
    
            sendMessage = MessageFormatUtils.formatCodes(sender, sendMessage);
    
            if(playerName.equalsIgnoreCase("Console")){
                String fullMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> " + "Console"
                        + privateColor + ">§r " + sendMessage;
                sender.sendMessage(fullMessage);
                QuickChat.getConsole().sendMessage(fullMessage);
                LastPlayers.addLastPlayers(sendersID, ConsoleUUID);
            }else{
                List<Player> players = ProcessorUtils.matchPlayer(playerName);
    
                if(players.size() == 0){
                    sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", playerName));
                }else if(players.size() > 1){
                    sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                }else{
                    Player reciever = players.get(0);
    
                    if(IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId())){
                        sender.sendMessage(messageData.get("quickchat.ignore.message").replace("%player%", reciever
                                .getDisplayName()));
                    }else{
    
                        String sendersMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> "
                                + reciever.getDisplayName() + privateColor + ">§r " + sendMessage;
                        if(!reciever.equals(sender)) sender.sendMessage(sendersMessage);
                        if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping"))
                            sendMessage = PingUtils.ping(reciever, sendMessage);
                        sendMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> "
                                + reciever.getDisplayName() + privateColor + ">§r " + sendMessage;
                        LastPlayers.addLastPlayers(reciever.getUniqueId(), sendersID);
                        if(LastPlayers.getLastPlayer(sendersID).equals("Null"))
                            LastPlayers.addLastPlayers(sendersID, reciever.getUniqueId());
                        reciever.sendMessage(sendMessage);
    
                        QuickChat.getConsole().sendMessage(sendMessage);
                    }
                }
            }
        }
    }
    
    public static void setPrivateColor(ChatColor c){
        privateColor = c;
    }
    
    public static ChatColor getPrivateColor(){
        return privateColor;
    }
}
