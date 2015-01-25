package co.justgame.quickchat.processors;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.justgame.quickchat.channel.Channel;
import co.justgame.quickchat.collections.PlayerChannels;
import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.utils.ChannelUtils;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.MessageFormatUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class ChannelProcessor implements MessageData {
    
    public static void processChannelCommand(String message, Player sender){
        UUID sendersID = sender.getUniqueId();
        
        if(message.equals("#")){
            
            /*
             * This If statement handles the chat message "#"
             */

            StringBuilder finalMessage = new StringBuilder();
            Channel sendersChannel = ChannelUtils.getFullChannel(sendersID);

            //if the sender is in a channel
            if(sendersChannel != null){
                //append the the current channel to the final message
                finalMessage.append(messageData.get("quickchat.channels.info.channel").replace("%channel%", sendersChannel.getName()));
                
                //append the current radius to the final message if the current channel has a radius
                if(ChannelUtils.getFullChannel(sendersID).getradius() >= 0)
                    finalMessage.append("    "+ messageData.get("quickchat.channels.info.radius")
                            .replace("%radius%", ""+sendersChannel.getradius()));
                
                //get a list of all the other players within range of the sender
                String players = ChannelUtils.getPlayersInRange(sender);
                
                //if the list of players is not empty, add it to the final message
                if(players.isEmpty())
                    finalMessage.append(messageData.get("quickchat.channels.info.playerlist")
                            .replace("%playerList%", "&4 No Players Can Hear You"));
                else
                    finalMessage.append(messageData.get("quickchat.channels.info.playerlist")
                            .replace("%playerList%", players));
                
            }else{ // if the sender is in the Null channel, or is in a converstaion
                
                //if the sender is in a conversation, replace the current channel with "In Converstaion"
                if(PlayerChannels.playerHasPlayerChannel(sendersID))
                    finalMessage.append(messageData.get("quickchat.channels.info.channel")
                            .replace("%channel%", "&4In Conversation"));
                else
                    finalMessage.append(messageData.get("quickchat.channels.info.channel").replace("%channel%", "&4Null"));
            }
            
            //get the list of channels the sender is currently authorized to join
            StringBuilder channelsList = ChannelUtils.getChannelList(sender); 
            
            //if the list of channels is not empty, add it to the final message
            if(channelsList.toString().isEmpty())
                finalMessage.append(messageData.get("quickchat.channels.info.channellist")
                        .replace("%channelList%", "&4No Other Joinable Channels"));
            else
                finalMessage.append(messageData.get("quickchat.channels.info.channellist")
                        .replace("%channelList%", channelsList.toString()));

            //send the completed message
            ProcessorUtils.sendMultilineMessage(sender, MessageFormatUtils.formatString(finalMessage.toString()));

        }else if(!message.contains(" ") || !message.replace("#", "").trim().contains(" ")){
            
            /*
             * This If statement handles the chat message "#<Channel>"
             */

            String channel = message.replaceFirst("#", "").toLowerCase().trim();

            //if the channel requested exists
            if(ChannelUtils.exists(channel)){
              //if the sender is not currently in the channel requested
                if(!ChannelUtils.playerIsAlreadyInChannel(channel, sender)){
                    //if the sender has permission to join the channel requested
                    if(sender.hasPermission("quickchat.channel." + channel) || sender.hasPermission("quickchat.channel")){
                        
                        //leave the current conversation if currently in one
                        if(PlayerChannels.playerHasPlayerChannel(sendersID)){
                            PlayerChannels.removePlayerChannel(sendersID);
                        }
                        //leave the current channel if currently in one
                        if(ChannelUtils.getFullChannel(sendersID) != null){
                            ChannelUtils.removePlayerFromChannel(sendersID);
                        }
                        
                        //add sender to the channel requested
                        ChannelUtils.addPlayerToChannel(channel, sendersID);
                        //tell the console that the sender has joined a new channel
                        QuickChat.getConsole().sendMessage("[QuickChat] "
                                + messageData.get("quickchat.console.joinchannel").replace("%player%", 
                                        sender.getDisplayName()).replace("%channel%", channel));
                        
                        //tell the sender that he has successfully joined a new channel
                        sender.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", channel));
                        
                    }else
                        //tell the sender that he does not have permission to join the requested channel
                        sender.sendMessage(messageData.get("quickchat.channels.nopermission")
                                .replace("%channel%", ChannelUtils.getName(channel)));
                }else
                    //tell the sender that he is already in the requested channel
                    sender.sendMessage(messageData.get("quickchat.channels.samechannel").replace("%channel%", channel));
            }else
                //tell the sender that the requested channel does not exist
                sender.sendMessage(messageData.get("quickchat.channels.notexist").replace("%channel%", channel));

        }else{
            
            /*
             * This Else statement handles the chat message "#<Channel> <Message>"
             */

            
            //split the message into it's component parts
            String[] splitMessage = message.replaceFirst("#", "").trim().split(" ", 2);
            
            //set channel and and message equal to their respective components of the split message
            String channel = splitMessage[0].replaceFirst("#", "").toLowerCase().trim();
            message = splitMessage.length > 1 ? MessageFormatUtils.formatCodes(sender,splitMessage[1]) : "";

            //if the channel requested exists
            if(ChannelUtils.exists(channel)){
                //if the sender has permission to send a message to the channel requested
                if(sender.hasPermission("quickchat.channel." + channel) || sender.hasPermission("quickchat.channel")){

                    ChatColor color = ChannelUtils.getColor(channel);
                    ChatColor reset = ChatColor.RESET;
                    
                    //send the message to the requested channel
                    sender.sendMessage(color + "<" + reset +  ProcessorUtils.getDisplayName(sendersID) + " -> " + channel + color + "> " + reset
                            + message);

                    //tell console that sender just sent a message
                    QuickChat.getConsole().sendMessage(color + "<" + reset + ProcessorUtils.getDisplayName(sendersID) + " -> " + channel + color
                            + "> " + reset + message);

                    //tell sender no one heard them if no one heard them
                    if(ChannelUtils.getPlayersWhoHeardYou(channel, sender, message, 0) == 0){
                        sender.sendMessage(messageData.get("quickchat.info.nobody"));
                    }

                }else
                    //tell the sender that he does not have premission to send a message to the channel
                    sender.sendMessage(messageData.get("quickchat.channels.nopermissionsend")
                            .replace("%channel%", channel));
            }else
                //tell the sender that the channel he requested does not exist
                sender.sendMessage(messageData.get("quickchat.channels.notexist").replace("%channel%", channel));
        }    
    }
}
