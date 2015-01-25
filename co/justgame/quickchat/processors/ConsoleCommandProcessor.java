package co.justgame.quickchat.processors;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.collections.MutedPlayers;
import co.justgame.quickchat.collections.PlayerChannels;
import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.utils.ChannelUtils;
import co.justgame.quickchat.utils.MessageData;
import co.justgame.quickchat.utils.MessageFormatUtils;
import co.justgame.quickchat.utils.PingUtils;
import co.justgame.quickchat.utils.ProcessorUtils;


public class ConsoleCommandProcessor implements MessageData {
    
    public static boolean processConsoleCommand(CommandSender sender, Command cmd, String label, String[] args){
        ChatColor pc = PrivateMessageProcessor.getPrivateColor();
        UUID sendersName;
        
        if(sender instanceof Player){
            sender.sendMessage(messageData.get("quickchat.console.playerusingconsolecommand"));
            return true;
        }else{
            sendersName = QuickChat.getConsoleUUID();
        }
        
        if(cmd.getName().equalsIgnoreCase("!")){

            String trimMessage = StringUtils.join(args, " ");
            trimMessage = MessageFormatUtils.formatString(trimMessage);

            QuickChat.getConsole().sendMessage("[QuickChat] "
                    + messageData.get("quickchat.console.broadcast").replace("%player%", "CONSOLE")
                            .replace("%message%", ServerBroadcastProcessor.getBroadCastName() + " " + trimMessage.trim()));

            for(Player onlinePlayer: Bukkit.getOnlinePlayers()){
                onlinePlayer.sendMessage(ServerBroadcastProcessor.getBroadCastName() + " " + trimMessage.trim());
            }

        }else if(cmd.getName().equalsIgnoreCase(">")){
            // Message: > <Message>

            if(args.length > 1){
                String playerName = args[0];
                String sendMessage = MessageFormatUtils.formatString(StringUtils.join(args, " "));
                if(args.length >= 2) sendMessage = sendMessage.split(" ", 2)[1].trim();

                if(playerName.equalsIgnoreCase("*") && args.length >= 2){
                    QuickChat.getConsole().sendMessage(messageData.get("quickchat.console.rawtext")
                            .replace("%player%", "Console").replace("%message%", sendMessage));

                    for(Player reciever: Bukkit.getOnlinePlayers()){
                        reciever.sendMessage(sendMessage);
                    }
                }else if(playerName.equalsIgnoreCase("?")){
                    sender.sendMessage(messageData.get("quickchat.console.rawhelp"));
                }else if(playerName.equalsIgnoreCase("console")){
                    if(sender instanceof Player) sender.sendMessage("<You (raw text) -> " + ProcessorUtils.getDisplayName(sendersName) + "> " + sendMessage);
                    QuickChat.getConsole().sendMessage(sendMessage);
                    LastPlayers.addLastPlayers(sendersName, sendersName);
                }else if(args.length >= 2){
                    List<Player> players = ProcessorUtils.matchPlayer(playerName);

                    if(players.size() == 0){
                        sender.sendMessage(messageData.get("quickchat.rawtext.noplayer").replace("%player%", playerName));
                    }else if(players.size() > 1){
                        sender.sendMessage(messageData.get("quickchat.rawtext.moreplayer"));
                    }else{
                        Player reciever = players.get(0);
                        if(sender instanceof Player)
                            sender.sendMessage("<You (raw text) -> " + reciever.getDisplayName() + "> " + sendMessage);
                        QuickChat.getConsole().sendMessage("<" + ProcessorUtils.getDisplayName(sendersName) + " (raw text) -> " + reciever.getDisplayName()
                                + "> " + sendMessage);
                        if(!reciever.equals(sender)){
                            reciever.sendMessage(sendMessage);

                            LastPlayers.addLastPlayers(reciever.getUniqueId(), sendersName);
                        }
                    }
                }else{
                    sender.sendMessage(messageData.get("quickchat.rawtext.consoleimproperusage"));
                }
            }else{
                sender.sendMessage(messageData.get("quickchat.rawtext.consoleimproperusage"));
            }

            return true;
            
        }else if(cmd.getName().equalsIgnoreCase("-")){
            if(args.length == 1){
                List<Player> matches = ProcessorUtils.matchPlayer(args[0]);
                if(matches.size() == 1){
                    Player p = matches.get(0);
                    if(MutedPlayers.isMuted(p)){
                        MutedPlayers.unmute(p);
                        sender.sendMessage(messageData.get("quickchat.mute.unmute").replace("%p%", p.getName()));
                        p.sendMessage(messageData.get("quickchat.mute.otherunmute"));
                    }else{ 
                        MutedPlayers.mute(p); 
                        sender.sendMessage(messageData.get("quickchat.mute.mute").replace("%p%", p.getName()));
                        p.sendMessage(messageData.get("quickchat.mute.othermute"));
                    }
                }else if(matches.size() == 0)
                    sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", args[0]));
                else if(matches.size() > 1) 
                    sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
            }else sender.sendMessage(messageData.get("quickchat.mute.usage"));
            return true;
        }else if(cmd.getName().equalsIgnoreCase("@")){
                // Message: @
                if(args.length == 0){

                    StringBuilder buildMessage = new StringBuilder();

                    if(LastPlayers.getLastPlayer(sendersName) != null)
                        buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", 
                                Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersName)).getDisplayName()));
                    else
                        buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", "&4None"));

                    if(PlayerChannels.playerHasPlayerChannel(sendersName)){
                        UUID otherPlayer = PlayerChannels.getPartner(sendersName);
                        buildMessage.append(messageData.get("quickchat.private.conversation").replace("%player%", 
                                Bukkit.getPlayer(otherPlayer).getDisplayName()));
                    }else{
                        buildMessage.append(messageData.get("quickchat.private.conversation").replace("%player%", "&4None"));
                    }

                    ProcessorUtils.sendMultilineMessage(sender, MessageFormatUtils.formatString(buildMessage.toString()));

                    // Message: @<Player>
                }else if(args.length == 1){

                    if(args[0].equalsIgnoreCase("console")){
                        ChannelUtils.removePlayerFromChannelIfOneExists(((Player) sender).getUniqueId());
                        if(PlayerChannels.playerHasPlayerChannel(sendersName)){
                            if(!PlayerChannels.getPartner(sendersName).equals(sendersName)){

                                PlayerChannels.addPlayerChannel(sendersName, sendersName);
                                sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                        .replace("%player%", "the Console"));

                            }else{
                                sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                        .replace("%player%", "the Console"));
                            }
                        }else{
                            PlayerChannels.addPlayerChannel(sendersName, sendersName);

                            sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                    .replace("%player%", "the Console"));
                        }

                    }else if(args[0].equals("?")){

                        sender.sendMessage(messageData.get("quickchat.console.privatehelp"));

                    }else{

                        List<Player> players = ProcessorUtils.matchPlayer(args[0]);

                        if(players.size() == 0){
                            sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", args[0]));
                        }else if(players.size() > 1){
                            sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                        }else{
                            UUID otherPlayersID = players.get(0).getUniqueId();
                            String otherPlayersName = players.get(0).getDisplayName();

                            if(PlayerChannels.playerHasPlayerChannel(sendersName)){
                                if(!PlayerChannels.getPartner(sendersName).equals(otherPlayersName)){

                                    PlayerChannels.addPlayerChannel(sendersName, otherPlayersID);
                                    sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                            .replace("%player%", otherPlayersName));

                                }else{
                                    sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                            .replace("%player%", otherPlayersName));
                                }
                            }else{
                                PlayerChannels.addPlayerChannel(sendersName, otherPlayersID);

                                sender.sendMessage(messageData.get("quickchat.private.joinchannel").replace("%player%", players
                                        .get(0).getDisplayName()));
                            }
                        }
                    }
                    // Message: @<Player> <Message>
                }else if(args.length > 1){
                    String playerName = args[0];
                    String sendMessage = StringUtils.join(args, " ");
                    sendMessage = MessageFormatUtils.formatString(sendMessage.split(" ", 2)[1]);

                    if(playerName.equalsIgnoreCase("Console")){
                        String fullMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> " + "Console" + pc
                                + ">§r " + sendMessage;
                        sender.sendMessage(fullMessage);
                        QuickChat.getConsole().sendMessage(fullMessage);
                        LastPlayers.addLastPlayers(sendersName, sendersName);

                    }else if(playerName.equals("?")){

                        sender.sendMessage(messageData.get("quickchat.console.privatehelp"));

                    }else if(playerName.equalsIgnoreCase("LP")){

                        sendMessage = MessageFormatUtils.formatString(sendMessage);

                        if(!LastPlayers.getLastPlayer(sendersName).equals("Null")){
                            if(LastPlayers.getLastPlayer(sendersName).equals(sendersName)){
                                String fullMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> "
                                        + LastPlayers.getLastPlayer(sendersName) + pc + ">§r " + sendMessage;
                                sender.sendMessage(fullMessage);
                            }else{
                                Player reciever = Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersName));

                                if(reciever != null){
                                    String fullMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> "
                                            + reciever.getDisplayName() + pc + ">§r " + sendMessage;
                                    if(!reciever.equals(sender)) reciever.sendMessage(fullMessage);
                                    sender.sendMessage(fullMessage);

                                }else{
                                    sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                            .replace("%player%", Bukkit.getPlayer(LastPlayers.getLastPlayer(sendersName)).getDisplayName()));
                                }
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.private.nolastplayer"));
                        }

                    }else if(playerName.equalsIgnoreCase("C")){

                        if(PlayerChannels.playerHasPlayerChannel(sendersName)){

                            sendMessage = MessageFormatUtils.formatString(sendMessage);

                            if(PlayerChannels.getPartner(sendersName).equals(sendersName)){

                                String fullMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> " + "Console"
                                        + pc + ">§r " + sendMessage;
                                sender.sendMessage(fullMessage);

                            }else{
                                Player reciever = Bukkit.getPlayer(PlayerChannels.getPartner(sendersName));

                                if(reciever == null){
                                    sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                            .replace("%player%", 
                                                    Bukkit.getPlayer(PlayerChannels.getPartner(sendersName)).getDisplayName()));
                                    PlayerChannels.removePlayerChannel(sendersName);
                                }else{

                                    String reciversMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> "
                                            + reciever.getDisplayName() + pc + ">§r " + sendMessage;
                                    if(!reciever.equals(sender)) sender.sendMessage(reciversMessage);
                                    if(!reciever.equals(sender)) sendMessage = PingUtils.ping(reciever, sendMessage);
                                    sendMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> " + reciever.getDisplayName()
                                            + pc + ">§r " + sendMessage;
                                    reciever.sendMessage(sendMessage);

                                    LastPlayers.addLastPlayers(reciever.getUniqueId(), sendersName);
                                }
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.private.noconversation"));
                        }

                    }else{
                        List<Player> players = ProcessorUtils.matchPlayer(playerName);

                        if(players.size() == 0){
                            sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", playerName));
                        }else if(players.size() > 1){
                            sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                        }else{
                            Player reciever = players.get(0);

                            String sendersMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> " + reciever.getDisplayName()
                                    + pc + ">§r " + sendMessage;
                            if(!reciever.equals(sender)) sender.sendMessage(sendersMessage);
                            if(!reciever.equals(sender)) sendMessage = PingUtils.ping(reciever, sendMessage);
                            sendMessage = pc + "<§r" + ProcessorUtils.getDisplayName(sendersName) + " -> " + reciever.getDisplayName()
                                    + pc + ">§r " + sendMessage;
                            LastPlayers.addLastPlayers(reciever.getUniqueId(), sendersName);
                            reciever.sendMessage(sendMessage);

                        }
                    }
                }
            }else{
                return true;
            }
        return true;
    }

}
