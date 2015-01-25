package co.justgame.quickchat.processors;

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
import co.justgame.quickchat.utils.PrefixSuffixUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class DefaultChatProcessor implements MessageData {
    final static UUID ConsoleUUID = QuickChat.getConsoleUUID();
    
    public static void processChat(String message, Player sender){
        ChatColor privateColor = PrivateMessageProcessor.getPrivateColor();
        UUID sendersID = sender.getUniqueId();
        
        if(PlayerChannels.playerHasPlayerChannel(sendersID)){

            String sendMessage = message;

            sendMessage = MessageFormatUtils.formatCodes(sender, sendMessage);

            if(PlayerChannels.getPartner(sendersID).equals(ConsoleUUID)){

                String fullMessage = "§5<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> " + "Console" + "§5>§r " + sendMessage;
                QuickChat.getConsole().sendMessage(fullMessage);
                sender.sendMessage(fullMessage);
                
                LastPlayers.addLastPlayers(sendersID, ConsoleUUID);
            }else{
                Player reciever = Bukkit.getPlayer(PlayerChannels.getPartner(sendersID));

                if(reciever == null){
                    sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                            .replace("%player%", Bukkit.getPlayer(PlayerChannels.getPartner(sendersID)).getDisplayName()));
                    PlayerChannels.removePlayerChannel(sendersID);
                    ChannelUtils.joinLoginChannel(sender);
                }else{
                    String reciversMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> "
                            + reciever.getDisplayName() + privateColor + ">§r " + sendMessage;
                    if(!reciever.equals(sender)) sender.sendMessage(reciversMessage);
                    if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                            && !IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId()))
                        sendMessage = PingUtils.ping(reciever, sendMessage);

                    sendMessage = privateColor + "<§r" + ProcessorUtils.getDisplayName(sendersID) + " -> " + reciever.getDisplayName()
                            + privateColor + ">§r " + sendMessage;

                    if(!IgnoredPlayers.isIgnored(sendersID, reciever.getUniqueId())) reciever.sendMessage(sendMessage);

                    LastPlayers.addLastPlayers(sendersID, reciever.getUniqueId());
                    QuickChat.getConsole().sendMessage(sendMessage);
                }

            }
        }else{

            String channel = ChannelUtils.getChannel(sendersID);
            if(!channel.equals("Null")){

                ChatColor color = ChannelUtils.getColor(channel);
                ChatColor reset = ChatColor.RESET;
                String consoleMessage = color + "<" + reset + PrefixSuffixUtils.getPlayerPrefix(sender) + ProcessorUtils.getDisplayName(sendersID)
                        + PrefixSuffixUtils.getPlayerSuffix(sender) + color + "> " + reset
                        + MessageFormatUtils.formatCodes(sender, message);

                QuickChat.getConsole().sendMessage(consoleMessage);
                sender.sendMessage(consoleMessage);

                if(ChannelUtils.getPlayersWhoHeardYou(channel, sender, MessageFormatUtils.formatCodes(sender, message) , 0) == 0){
                    sender.sendMessage(messageData.get("quickchat.info.nobody"));
                }
            }else{
                sender.sendMessage(messageData.get("quickchat.channels.null"));
            }
        }
    }
}
