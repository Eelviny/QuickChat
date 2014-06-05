package co.justgame.quickchat.listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.justgame.quickchat.channel.ChannelUtils;
import co.justgame.quickchat.channel.PlayerChannelUtils;
import co.justgame.quickchat.listeners.utils.MessageUtils;
import co.justgame.quickchat.main.QuickChat;

public class chatListener implements Listener {

    private HashMap<String, String> messageData = QuickChat.getMessageData();

    @EventHandler(priority = EventPriority.HIGH)
    public synchronized void playerChat(AsyncPlayerChatEvent e){
        if(!e.isCancelled()){
            e.setCancelled(true);

            Player sender = e.getPlayer();
            String sendersName = sender.getDisplayName();
            String message = e.getMessage();

            if(message.startsWith("#")){
                // Message: #
                if(message.equals("#")){

                    StringBuilder finalMessage = new StringBuilder();

                    StringBuilder channelsList = new StringBuilder();

                    for(String string: ChannelUtils.getChannelNames()){
                        if(sender.hasPermission("quickchat.channel." + string) || sender.hasPermission("quickchat.channel")){
                            channelsList.append(" " + string);
                        }
                    }

                    if(channelsList.toString().trim().split(" ").length > 2){
                        channelsList = new StringBuilder(" " + channelsList.toString().trim().replace(" ", ", "));
                        channelsList.replace(channelsList.lastIndexOf(", "), channelsList.lastIndexOf(", ") + 1, ", and");
                    }else if(channelsList.toString().trim().split(" ").length > 1){
                        channelsList = new StringBuilder(" " + channelsList.toString().trim().replace(" ", " and "));
                    }

                    if(ChannelUtils.getFullChannel(sendersName) != null){

                        String players = ChannelUtils.getPlayersInRange(sender);

                        finalMessage.append(messageData.get("quickchat.channels.info.channel").replace("%channel%", ChannelUtils
                                .getChannel(sendersName)));

                        if(ChannelUtils.getFullChannel(sendersName).getradius() >= 0){
                            finalMessage.append("    "
                                    + messageData.get("quickchat.channels.info.radius").replace("%radius%", String
                                            .valueOf(ChannelUtils.getFullChannel(sendersName).getradius())));
                        }
                        if(players.isEmpty()){
                            finalMessage.append(messageData.get("quickchat.channels.info.playerlist")
                                    .replace("%playerList%", "&4 No Players Can Hear You"));
                        }else{
                            finalMessage.append(messageData.get("quickchat.channels.info.playerlist")
                                    .replace("%playerList%", players));
                        }
                    }else{
                        if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                            finalMessage.append(messageData.get("quickchat.channels.info.channel")
                                    .replace("%channel%", "&4In Conversation"));
                        }else{
                            finalMessage
                                    .append(messageData.get("quickchat.channels.info.channel").replace("%channel%", "&4Null"));
                        }
                    }

                    if(channelsList.toString().isEmpty()){
                        finalMessage.append(messageData.get("quickchat.channels.info.channellist")
                                .replace("%channelList%", "&4No Other Joinable Channels"));
                    }else{
                        finalMessage.append(messageData.get("quickchat.channels.info.channellist")
                                .replace("%channelList%", channelsList.toString()));
                    }

                    sendMultilineMessage(sender, MessageUtils.formatString(finalMessage.toString()));

                    // Message: #<Channel>
                }else if(!message.contains(" ") || !message.replace("#", "").trim().contains(" ")){

                    String channel = message.replaceFirst("#", "").toLowerCase().trim();

                    if(ChannelUtils.exists(channel)){
                        if(!ChannelUtils.playerIsAlreadyInChannel(channel, sender)){
                            if(sender.hasPermission("quickchat.channel." + channel) || sender.hasPermission("quickchat.channel")){

                                if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                                    PlayerChannelUtils.removePlayerChannel(sendersName);
                                }
                                if(ChannelUtils.getFullChannel(sendersName) != null){
                                    ChannelUtils.removePlayerFromChannel(sendersName);
                                }
                                ChannelUtils.addPlayerToChannel(channel, sendersName);
                                QuickChat.getConsole().sendMessage("[QuickChat] "
                                        + messageData.get("quickchat.console.joinchannel").replace("%player%", sendersName)
                                                .replace("%channel%", channel));
                                sender.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", channel));
                            }else{
                                sender.sendMessage(messageData.get("quickchat.channels.nopermission")
                                        .replace("%channel%", ChannelUtils.getName(channel)));
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.channels.samechannel").replace("%channel%", channel));
                        }
                    }else{
                        sender.sendMessage(messageData.get("quickchat.channels.notexist").replace("%channel%", channel));
                    }
                    // Message: #<Channel> <Message>
                }else if(message.contains(" ")){

                    String[] splitMessage = message.replaceFirst("#", "").trim().split(" ", 2);
                    String channel = splitMessage[0].replaceFirst("#", "").toLowerCase().trim();
                    String sendMessage = "";
                    if(splitMessage.length > 1) sendMessage = splitMessage[1];

                    sendMessage = MessageUtils.formatCodes(sender, sendMessage);

                    if(ChannelUtils.exists(channel)){
                        if(sender.hasPermission("quickchat.channel." + channel) || sender.hasPermission("quickchat.channel")){

                            ChatColor color = ChannelUtils.getColor(channel);
                            ChatColor reset = ChatColor.RESET;

                            sender.sendMessage(color + "<" + reset + sendersName + " -> " + channel + color + "> " + reset
                                    + sendMessage);

                            QuickChat.getConsole().sendMessage(color + "<" + reset + sendersName + " -> " + channel + color
                                    + "> " + reset + sendMessage);

                            if(ChannelUtils.getPlayersWhoHeardYou(channel, sender, sendMessage, 0) == 0){
                                sender.sendMessage(messageData.get("quickchat.info.nobody"));
                            }

                        }else{
                            sender.sendMessage(messageData.get("quickchat.channels.nopermissionsend")
                                    .replace("%channel%", channel));
                        }
                    }else{
                        sender.sendMessage(messageData.get("quickchat.channels.notexist").replace("%channel%", channel));
                    }
                }
            }else if(message.startsWith("@")){
                if(message.startsWith("@")){
                    // Message: @
                    if(message.equals("@")){

                        StringBuilder buildMessage = new StringBuilder();

                        if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                            buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", 
                                    PlayerChannelUtils.getPartner(sendersName)));
                        }else{
                            buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", "&4None"));
                        }

                        if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                            String otherPlayer = PlayerChannelUtils.getPartner(sendersName);
                            buildMessage.append(messageData.get("quickchat.private.conversation")
                                    .replace("%player%", otherPlayer));
                        }else{
                            buildMessage.append(messageData.get("quickchat.private.conversation").replace("%player%", "&4None"));
                        }

                        sendMultilineMessage(sender, MessageUtils.formatString(buildMessage.toString()));

                        // Message: @<Player>
                    }else if(message.charAt(1) != ' ' && !message.contains(" ")){

                        String reciever = message.replaceFirst("@", "");

                        if(reciever.equalsIgnoreCase("console")){
                            if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                                if(!PlayerChannelUtils.getPartner(sendersName).equalsIgnoreCase("Console")){
                                    PlayerChannelUtils.addPlayerChannel(sendersName, "Console");
                                    sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                            .replace("%player%", "the Console"));
                                    QuickChat.getConsole().sendMessage("[QuickChat] "
                                            + messageData.get("quickchat.console.joinplayerchannel")
                                                    .replace("%player%", sendersName).replace("%otherplayer%", "the Console"));
                                }else{
                                    sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                            .replace("%player%", "the Console"));
                                }
                            }else{
                                PlayerChannelUtils.addPlayerChannel(sendersName, "Console");
                                QuickChat.getConsole().sendMessage("[QuickChat] "
                                        + messageData.get("quickchat.console.joinplayerchannel").replace("%player%", sendersName)
                                                .replace("%otherplayer%", "the Console"));
                                sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                        .replace("%player%", "the Console"));
                            }

                        }else{

                            List<Player> players = Bukkit.matchPlayer(reciever);

                            if(players.size() == 0){
                                sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", reciever));
                            }else if(players.size() > 1){
                                sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                            }else{

                                if(ChannelUtils.getChannel(sendersName) != "Null"){
                                    ChannelUtils.getFullChannel(sendersName).removePlayer(sendersName);
                                }

                                String otherPlayersName = players.get(0).getDisplayName();

                                if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                                    if(!PlayerChannelUtils.getPartner(sendersName).equals(otherPlayersName)){

                                        if(!QuickChat.isIgnored(sendersName, players.get(0).getDisplayName())){
                                            PlayerChannelUtils.addPlayerChannel(sendersName, otherPlayersName);

                                            QuickChat.getConsole().sendMessage("[QuickChat] "
                                                    + messageData.get("quickchat.console.joinplayerchannel")
                                                            .replace("%player%", sendersName)
                                                            .replace("%otherplayer%", otherPlayersName));
                                            sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                                    .replace("%player%", players.get(0).getDisplayName()));
                                        }else{
                                            sender.sendMessage(messageData.get("quickchat.ignore.channel")
                                                    .replace("%player%", players.get(0).getDisplayName()));
                                        }
                                    }else{
                                        sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                                .replace("%player%", otherPlayersName));
                                    }
                                }else{
                                    if(!QuickChat.isIgnored(sendersName, players.get(0).getDisplayName())){
                                        PlayerChannelUtils.addPlayerChannel(sendersName, otherPlayersName);

                                        QuickChat.getConsole().sendMessage("[QuickChat] "
                                                + messageData.get("quickchat.console.joinplayerchannel")
                                                        .replace("%player%", sendersName)
                                                        .replace("%otherplayer%", otherPlayersName));
                                        sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                                .replace("%player%", players.get(0).getDisplayName()));
                                    }else{
                                        sender.sendMessage(messageData.get("quickchat.ignore.channel")
                                                .replace("%player%", players.get(0).getDisplayName()));
                                    }
                                }
                            }
                        }
                        // Message: @<Player> <Message>
                    }else if(message.charAt(1) != ' ' && message.contains(" ")){
                        String[] splitMessage = message.split(" ", 2);
                        String playerName = splitMessage[0].replaceFirst("@", "");
                        String sendMessage = splitMessage[1];

                        sendMessage = MessageUtils.formatCodes(sender, sendMessage);

                        if(playerName.equalsIgnoreCase("Console")){
                            String fullMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> " + "Console"
                                    + QuickChat.getPrivateColor() + ">§r " + sendMessage;
                            sender.sendMessage(fullMessage);
                            QuickChat.getConsole().sendMessage(fullMessage);
                            QuickChat.addLastPlayers("Console", sendersName);
                        }else{
                            List<Player> players = Bukkit.matchPlayer(playerName);

                            if(players.size() == 0){
                                sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", playerName));
                            }else if(players.size() > 1){
                                sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                            }else{
                                Player reciever = players.get(0);

                                if(QuickChat.isIgnored(sendersName, reciever.getDisplayName())){
                                    sender.sendMessage(messageData.get("quickchat.ignore.message").replace("%player%", reciever
                                            .getDisplayName()));
                                }else{

                                    String sendersMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> "
                                            + reciever.getDisplayName() + QuickChat.getPrivateColor() + ">§r " + sendMessage;
                                    if(!reciever.equals(sender)) sender.sendMessage(sendersMessage);
                                    if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping"))
                                        sendMessage = MessageUtils.ping(reciever, sendMessage);
                                    sendMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> "
                                            + reciever.getDisplayName() + QuickChat.getPrivateColor() + ">§r " + sendMessage;
                                    QuickChat.addLastPlayers(reciever.getDisplayName(), sendersName);
                                    if(QuickChat.getLastPlayer(sendersName).equals("Null"))
                                        QuickChat.addLastPlayers(sendersName, reciever.getDisplayName());
                                    reciever.sendMessage(sendMessage);

                                    QuickChat.getConsole().sendMessage(sendMessage);
                                }
                            }
                        }
                        // Message: @ <Message>
                    }else if((message.charAt(1) == ' ')){
                        String sendMessage = message.replaceFirst("@", "");

                        sendMessage = MessageUtils.formatCodes(sender, sendMessage);

                        if(!QuickChat.getLastPlayer(sendersName).equals("Null")){
                            if(QuickChat.getLastPlayer(sendersName).equalsIgnoreCase("console")){
                                String fullMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> "
                                        + QuickChat.getLastPlayer(sendersName) + QuickChat.getPrivateColor() + ">§r " + sendMessage;
                                sender.sendMessage(fullMessage);
                                QuickChat.getConsole().sendMessage(fullMessage);
                                QuickChat.addLastPlayers("Console", sendersName);
                            }else{
                                Player reciever = Bukkit.getPlayerExact(QuickChat.getLastPlayer(sendersName));

                                if(reciever != null){
                                    if(QuickChat.isIgnored(sendersName, reciever.getDisplayName())){
                                        sender.sendMessage(messageData.get("quickchat.ignore.message")
                                                .replace("%player%", reciever.getDisplayName()));
                                    }else{
                                        if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                                && !QuickChat.isIgnored(sendersName, reciever.getDisplayName()))
                                            sendMessage = MessageUtils.ping(reciever, sendMessage);

                                        String fullMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> "
                                                + reciever.getDisplayName() + QuickChat.getPrivateColor() + ">§r" + sendMessage;
                                        if(!reciever.equals(sender) && !QuickChat.isIgnored(sendersName, reciever.getDisplayName()))
                                            reciever.sendMessage(fullMessage);
                                        else if(QuickChat.isIgnored(sendersName, reciever.getDisplayName()))
                                            sender.sendMessage(messageData.get("quickchat.ignore.message")
                                                    .replaceAll("%player%", reciever.getDisplayName()));

                                        sender.sendMessage(fullMessage);

                                        QuickChat.getConsole().sendMessage(fullMessage);
                                        QuickChat.addLastPlayers(reciever.getDisplayName(), sendersName);
                                    }
                                }else{
                                    sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                            .replace("%player%", QuickChat.getLastPlayer(sendersName)));
                                }
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.private.nolastplayer"));
                        }
                    }
                }
                // Message: ! <Message>
            }else if(message.startsWith("!") && !message.equals("!") && sender.hasPermission("quickchat.broadcast")){
                String sendMessage = message.replaceFirst("!", "");

                sendMessage = MessageUtils.formatCodes(sender, sendMessage);

                QuickChat.getConsole().sendMessage(messageData.get("quickchat.console.broadcast")
                        .replace("%player%", sendersName)
                        .replace("%message%", QuickChat.getBroadCastName() + " " + sendMessage.trim()));

                for(Player reciever: Bukkit.getOnlinePlayers()){
                    reciever.sendMessage(QuickChat.getBroadCastName() + " " + sendMessage.trim());
                }

            }else if(message.startsWith(">") && sender.hasPermission("quickchat.rawtext") && !message.equals(">")){

                message = MessageUtils.formatCodes(sender, message);
                // Message: > <Message>
                if(message.charAt(1) == ' '){
                    String sendMessage = message.replaceFirst("> ", "");

                    QuickChat.getConsole().sendMessage(messageData.get("quickchat.console.rawtext")
                            .replace("%player%", sendersName).replace("%message%", sendMessage));

                    for(Player reciever: Bukkit.getOnlinePlayers()){
                        reciever.sendMessage(sendMessage);
                    }
                    // Message: ><Player> <Message>
                }else if(message.charAt(1) != ' '){
                    String[] splitMessage = message.split(" ", 2);

                    if(splitMessage.length == 1){
                        sender.sendMessage(messageData.get("quickchat.rawtext.improperusage"));
                    }else{

                        String playerName = splitMessage[0].replaceFirst(">", "");
                        String sendMessage = splitMessage[1];
                        if(playerName.equalsIgnoreCase("console")){
                            sender.sendMessage("<You (raw text) -> " + "Console" + "> " + sendMessage);
                            QuickChat.getConsole().sendMessage(sendMessage);
                            QuickChat.addLastPlayers("Console", sendersName);
                        }else{
                            List<Player> players = Bukkit.matchPlayer(playerName);

                            if(players.size() == 0){
                                sender.sendMessage(messageData.get("quickchat.rawtext.noplayer").replace("%player%", playerName));
                            }else if(players.size() > 1){
                                sender.sendMessage(messageData.get("quickchat.rawtext.moreplayer"));
                            }else{
                                Player reciever = players.get(0);
                                sender.sendMessage("<You (raw text) -> " + reciever.getDisplayName() + "> " + sendMessage);
                                QuickChat.getConsole().sendMessage("<" + sendersName + " (raw text) -> "
                                        + reciever.getDisplayName() + "> " + sendMessage);
                                if(!reciever.equals(sender)){
                                    reciever.sendMessage(sendMessage);

                                    QuickChat.addLastPlayers(reciever.getDisplayName(), sendersName);
                                }
                            }
                        }
                    }
                }
                // Message: ?<Player>
            }else if(message.startsWith("?") && !message.equals("?") && !message.contains(" ")){

                String requestedPlayer = message.replace("?", "");
                if(requestedPlayer.equalsIgnoreCase("console")){
                    sender.sendMessage(messageData.get("quickchat.info.console"));
                }else{

                    List<Player> players = Bukkit.matchPlayer(requestedPlayer);

                    if(players.size() > 1){
                        sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                    }else if(players.size() == 0){
                        sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", requestedPlayer));
                    }else{
                        Player reciever = players.get(0);

                        if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){
                            sender.sendMessage(messageData.get("quickchat.info.playersconversation").replace("%player%", reciever
                                    .getDisplayName()));

                        }else{
                            sender.sendMessage(messageData.get("quickchat.info.playerschannel")
                                    .replace("%player%", reciever.getDisplayName())
                                    .replace("%channel%", ChannelUtils.getChannel(reciever.getDisplayName())));
                        }
                    }
                }

            }else if(message.startsWith("~") && !message.contains(" ") && sender.hasPermission("quickchat.ignore")){
                // Message: ~
                if(message.equals("~")){
                    StringBuilder buildMessage = new StringBuilder();

                    StringBuilder playersIgnored = new StringBuilder(QuickChat.getIgnoredPlayers(sendersName));
                    StringBuilder playersIgnoring = new StringBuilder(QuickChat.getIgnoringPlayers(sendersName));

                    if(playersIgnored.toString().isEmpty()){
                        buildMessage.append(messageData.get("quickchat.ignore.listignored")
                                .replace("%playerList%", " &4No Players Ignored"));
                    }else{
                        buildMessage.append(messageData.get("quickchat.ignore.listignored")
                                .replace("%playerList%", playersIgnored.toString()));
                    }

                    if(playersIgnoring.toString().isEmpty()){
                        buildMessage.append(messageData.get("quickchat.ignore.listignoring")
                                .replace("%playerList%", " &4No Players Ignoring You"));
                    }else{
                        buildMessage.append(messageData.get("quickchat.ignore.listignoring")
                                .replace("%playerList%", playersIgnoring.toString()));
                    }

                    sendMultilineMessage(sender, MessageUtils.formatString(buildMessage.toString()));
                    // Message: ~<Player>
                }else{
                    String requestedPlayer = message.replace("~", "");
                    List<Player> players = Bukkit.matchPlayer(requestedPlayer);

                    if(players.size() > 1){
                        sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                    }else if(players.size() == 0){
                        sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", requestedPlayer));
                    }else{
                        Player player = players.get(0);
                        String playersName = player.getDisplayName();

                        if(!player.equals(sender)){
                            if(!player.isOp() && !player.hasPermission("quickchat.ignore.deny")){
                                if(!QuickChat.isIgnored(sendersName, playersName)){
                                    QuickChat.addIgnoredPlayerToPlayer(sendersName, playersName);
                                    sender.sendMessage(messageData.get("quickchat.ignore.ignoring")
                                            .replace("%player%", playersName));
                                    player.sendMessage(messageData.get("quickchat.ignore.reciever")
                                            .replace("%player%", sendersName));
                                }else{
                                    QuickChat.removeIgnoredPlayerFromPlayer(sendersName, playersName);
                                    sender.sendMessage(messageData.get("quickchat.ignore.notignoring")
                                            .replace("%player%", playersName));
                                    player.sendMessage(messageData.get("quickchat.ignore.recieverun")
                                            .replace("%player%", sendersName));
                                }
                            }else{
                                sender.sendMessage(messageData.get("quickchat.ignore.op").replace("%player%", playersName));
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.ignore.self"));
                        }
                    }
                }
            }else{

                if(PlayerChannelUtils.playerHasPlayerChannel(sendersName)){

                    String sendMessage = message;

                    sendMessage = MessageUtils.formatCodes(sender, sendMessage);

                    if(PlayerChannelUtils.getPartner(sendersName).equalsIgnoreCase("Console")){

                        String fullMessage = "§5<§r" + sendersName + " -> " + "Console" + "§5>§r " + sendMessage;
                        QuickChat.getConsole().sendMessage(fullMessage);
                        sender.sendMessage(fullMessage);

                    }else{
                        Player reciever = Bukkit.getPlayerExact(PlayerChannelUtils.getPartner(sendersName));

                        if(reciever == null){
                            sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                    .replace("%player%", PlayerChannelUtils.getPartner(sendersName)));
                            PlayerChannelUtils.removePlayerChannel(sendersName);
                            ChannelUtils.joinLoginChannel(sender);
                        }else{
                            String reciversMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> "
                                    + reciever.getDisplayName() + QuickChat.getPrivateColor() + ">§r " + sendMessage;
                            if(!reciever.equals(sender)) sender.sendMessage(reciversMessage);
                            if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                    && !QuickChat.isIgnored(sendersName, reciever.getDisplayName()))
                                sendMessage = MessageUtils.ping(reciever, sendMessage);

                            sendMessage = QuickChat.getPrivateColor() + "<§r" + sendersName + " -> " + reciever.getDisplayName()
                                    + QuickChat.getPrivateColor() + ">§r " + sendMessage;

                            if(!QuickChat.isIgnored(sendersName, reciever.getDisplayName())) reciever.sendMessage(sendMessage);

                            QuickChat.addLastPlayers(reciever.getDisplayName(), sendersName);
                            QuickChat.getConsole().sendMessage(sendMessage);
                        }

                    }
                }else{

                    String channel = ChannelUtils.getChannel(sendersName);
                    if(!channel.equals("Null")){

                        ChatColor color = ChannelUtils.getColor(channel);
                        ChatColor reset = ChatColor.RESET;
                        String consoleMessage = color + "<" + reset + QuickChat.getPlayerPrefix(sender) + sendersName
                                + QuickChat.getPlayerSuffix(sender) + color + "> " + reset
                                + MessageUtils.formatCodes(sender, message);

                        QuickChat.getConsole().sendMessage(consoleMessage);
                        sender.sendMessage(consoleMessage);

                        if(ChannelUtils.getPlayersWhoHeardYou(channel, sender, MessageUtils.formatCodes(sender, message) , 0) == 0){
                            sender.sendMessage(messageData.get("quickchat.info.nobody"));
                        }
                    }else{
                        sender.sendMessage(messageData.get("quickchat.channels.null"));
                    }
                }
            }
        }
    };

    private void sendMultilineMessage(Player player, String message){
        if(player != null && message != null && player.isOnline()){
            String[] s = message.split("/n");
            for(String m: s){
                player.sendMessage(m);
            }
        }
    }

}
