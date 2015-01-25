package co.justgame.quickchat.utils;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.justgame.quickchat.channel.Channel;
import co.justgame.quickchat.collections.IgnoredPlayers;
import co.justgame.quickchat.collections.LastPlayers;
import co.justgame.quickchat.collections.PlayerChannels;
import co.justgame.quickchat.main.QuickChat;

public class ChannelUtils implements MessageData {

    private static LinkedHashMap<String, Channel> channels = new LinkedHashMap<String, Channel>();

    public static synchronized void clear(){
        channels.clear();
    }

    public static synchronized void addNewChannel(String s, Channel c){
        channels.put(s, c);
    }

    public static synchronized void removePlayerFromChannelIfOneExists(UUID p){
        if(getChannel(p).equals("Null")){
            channels.get(getChannel(p)).removePlayer(p);
        }
    }

    public static synchronized void removePlayerFromChannel(String channel, UUID player){
        channels.get(channel).removePlayer(player);
    }

    public static synchronized void removePlayerFromChannel(UUID player){
        getFullChannel(player).removePlayer(player);
    }

    public static synchronized void addPlayerToChannel(String channel, UUID player){
        channels.get(channel).addPlayer(player);
    }

    public static synchronized String getChannel(UUID p){
        for(Channel channel: channels.values()){
            for(UUID playerInChannel: channel.getplayers()){
                if(playerInChannel.equals(p)){
                    return channel.getName();
                }
            }
        }
        return "Null";
    }

    public static synchronized Channel getFullChannel(UUID player){
        for(Channel channel: channels.values()){
            for(UUID playerInChannel: channel.getplayers()){
                if(playerInChannel.equals(player)){
                    return channel;
                }
            }
        }
        return null;
    }

    public static synchronized void addPlayerToFirstAvailableChannel(Player p){
        for(Channel joinChannel: channels.values()){
            if(p.hasPermission("quickchat.channel." + joinChannel.getName())){
                joinChannel.addPlayer(p.getUniqueId());
                break;
            }
        }
    }
    
    public static synchronized String getPlayersInRange(Player p){
        StringBuilder players = new StringBuilder();

        Channel sendersChannel = ChannelUtils.getFullChannel(p.getUniqueId());
        for(Channel channel: channels.values()){
            for(UUID playerID: channel.getplayers()){
                Player realPlayer = Bukkit.getPlayer(playerID);
                if(realPlayer.hasPermission("quickchat.channel." + sendersChannel.getName())
                        || realPlayer.hasPermission("quickchat.channel")){
                    if(inRange(p, realPlayer, sendersChannel.getName())){
                        if(!IgnoredPlayers.isIgnored(p.getUniqueId(), playerID)) players.append(" " + getDisplayName(playerID));
                    }
                }
            }
        }

        Double randomNum = Math.random();
        if(randomNum > 0 && randomNum < .01){
            players.append(" " + "Herobrine!");
        }

        if(players.toString().trim().split(" ").length > 2){
            players = new StringBuilder(" " + players.toString().trim().replace(" ", ", "));
            players.replace(players.lastIndexOf(", "), players.lastIndexOf(", ") + 1, ", and");
        }else if(players.toString().trim().split(" ").length > 1){
            players = new StringBuilder(" " + players.toString().trim().replace(" ", " and "));
        }
        return players.toString();
    }

    public static synchronized boolean inRange(Player sender, Player reciever, String channel){
        if(channels.get(channel).getradius() > -1){
            if(sender.getWorld() == reciever.getWorld()){
                if(sender.getLocation().distance(reciever.getLocation()) <= channels.get(channel).getradius()){
                    return true;
                }
            }
        }else{
            return true;
        }
        return false;
    }
    
    public static StringBuilder getChannelList(Player sender){
        
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
        
        return channelsList;
    }

    public static synchronized void joinLoginChannel(Player player){
        for(Channel channel: channels.values()){
            LastPlayers.addLastPlayers(player.getUniqueId(), null);
            if(player.hasPermission("quickchat.channels." + channel.getName())){
                channel.addPlayer(player.getUniqueId());
                QuickChat.getConsole().sendMessage("[QuickChat] "
                        + messageData.get("quickchat.console.joinchannel").replace("%player%", player.getDisplayName())
                                .replace("%channel%", channel.getName()));
                player.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", channel.getName()));
                break;
            }
        }
        if(channels.get(getChannel(player.getUniqueId())) == null){
            QuickChat.getConsole().sendMessage("[QuickChat] "
                    + messageData.get("quickchat.console.joinnull").replace("%player%", player.getDisplayName()));
            player.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", "Null"));
        }
    }

    public static synchronized int getPlayersWhoHeardYou(String channel, Player sender, String sendMessage, int i){

        ChatColor color = ChannelUtils.getColor(channel);
        ChatColor reset = ChatColor.RESET;
        UUID sendersName = sender.getUniqueId();

        int PlayersWhoHeardYou = i;
        for(Channel channelInList: channels.values()){
            for(UUID playerInChannel: channelInList.getplayers()){
                Player reciever = Bukkit.getPlayer(playerInChannel);

                if(reciever != null)
                    if(reciever.hasPermission("quickchat.channel." + channel) || reciever.hasPermission("quickchat.channel")){

                        if(inRange(sender, reciever, channel)){
                            String finalMessage = sendMessage;
                            if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                    && !IgnoredPlayers.isIgnored(sendersName, reciever.getUniqueId()))
                                finalMessage = PingUtils.ping(reciever, finalMessage);

                            finalMessage = color + "<" + reset + PrefixSuffixUtils.getPlayerPrefix(sender) + sendersName
                                    + PrefixSuffixUtils.getPlayerSuffix(sender) + color + "> " + reset + finalMessage;

                            if(!IgnoredPlayers.isIgnored(sendersName, reciever.getUniqueId()) && !reciever.equals(sender)){
                                reciever.sendMessage(finalMessage);
                                PlayersWhoHeardYou++;
                            }
                        }
                    }
            }
        }

        for(UUID PlayerInConversation: PlayerChannels.getPlayersInConversation()){
            Player reciever = Bukkit.getPlayer(PlayerInConversation);
            if(reciever != null)
                if(reciever.hasPermission("quickchat.channel." + channel) || reciever.hasPermission("quickchat.channel")){

                    if(inRange(sender, reciever, channel)){
                        String finalMessage = sendMessage;
                        if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                && !IgnoredPlayers.isIgnored(sendersName, reciever.getUniqueId()))
                            finalMessage = PingUtils.ping(reciever, finalMessage);

                        finalMessage = color + "<" + reset + PrefixSuffixUtils.getPlayerPrefix(sender) + sendersName
                                + PrefixSuffixUtils.getPlayerSuffix(sender) + color + "> " + reset + finalMessage;

                        if(!IgnoredPlayers.isIgnored(sendersName, reciever.getUniqueId()) && !reciever.equals(sender))
                            reciever.sendMessage(finalMessage);
                        PlayersWhoHeardYou++;
                    }
                }
        }
        return PlayersWhoHeardYou;
    }

    public static synchronized boolean playerIsAlreadyInChannel(String channel, Player p){
        if(getFullChannel(p.getUniqueId()) == null){
            return false;
        }else{
             return channels.get(channel).getName().equals(getFullChannel(p.getUniqueId()).getName());
        }
    }

    public static synchronized boolean isDuplicate(String channel){
        return channels.containsKey(channel);
    }

    public static synchronized boolean exists(String channel){
        return channels.containsKey(channel);
    }
    
    public static synchronized String getDisplayName(UUID player){
        return Bukkit.getPlayer(player).getDisplayName();
    }

    public static synchronized String getName(String channel){
        return channels.get(channel).getName();
    }

    public static synchronized ChatColor getColor(String channel){
        return channels.get(channel).getColor();
    }

    public static synchronized Set<String> getChannelNames(){
        return channels.keySet();
    }
}
