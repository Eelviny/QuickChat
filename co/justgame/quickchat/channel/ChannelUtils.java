package co.justgame.quickchat.channel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import co.justgame.quickchat.listeners.utils.MessageUtils;
import co.justgame.quickchat.main.QuickChat;

public class ChannelUtils {

    private static HashMap<String, String> messageData = QuickChat.getMessageData();

    private static LinkedHashMap<String, Channel> channels = new LinkedHashMap<String, Channel>();

    /*
     * Return Type Void
     */

    public static synchronized void clear(){
        channels.clear();
    }

    public static synchronized void addNewChannel(String s, Channel c){
        channels.put(s, c);
    }

    public static synchronized void removePlayerFromChannelIfOneExists(String p){
        if(getChannel(p).equals("Null")){
            channels.get(getChannel(p)).removePlayer(p);
        }
    }

    public static synchronized void removePlayerFromChannel(String channel, String player){
        channels.get(channel).removePlayer(player);
    }

    public static synchronized void removePlayerFromChannel(String player){
        getFullChannel(player).removePlayer(player);
    }

    public static synchronized void addPlayerToChannel(String channel, String player){
        channels.get(channel).addPlayer(player);
    }

    public static synchronized String getChannel(String player){
        for(Channel channel: channels.values()){
            for(String playerInChannel: channel.getplayers()){
                if(playerInChannel.equals(player)){
                    return channel.getName();
                }
            }
        }
        return "Null";
    }

    public static synchronized Channel getFullChannel(String player){
        for(Channel channel: channels.values()){
            for(String playerInChannel: channel.getplayers()){
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
                joinChannel.addPlayer(p.getDisplayName());
                p.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", joinChannel.getName()));
                break;
            }
        }
    }

    /*
     * Return Type Not Void
     */
    public static synchronized String getPlayersInRange(Player p){
        StringBuilder players = new StringBuilder();

        Channel sendersChannel = ChannelUtils.getFullChannel(p.getName());
        for(Channel channel: channels.values()){
            for(String player: channel.getplayers()){
                Player realPlayer = Bukkit.getPlayerExact(player);
                if(realPlayer.hasPermission("quickchat.channel." + sendersChannel.getName())
                        || realPlayer.hasPermission("quickchat.channel")){
                    if(inRange(p, realPlayer, sendersChannel.getName())){
                        if(!QuickChat.isIgnored(p.getName(), player)) players.append(" " + player);
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

    public static synchronized void joinLoginChannel(Player player){
        for(Channel channel: channels.values()){
            QuickChat.addLastPlayers(player.getDisplayName(), "Null");
            if(player.hasPermission("quickchat.channels." + channel.getName())){
                channel.addPlayer(player.getDisplayName());
                QuickChat.getConsole().sendMessage("[QuickChat] "
                        + messageData.get("quickchat.console.joinchannel").replace("%player%", player.getDisplayName())
                                .replace("%channel%", channel.getName()));
                player.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", channel.getName()));
                break;
            }
        }
        if(channels.get(getChannel(player.getDisplayName())) == null){
            QuickChat.getConsole().sendMessage("[QuickChat] "
                    + messageData.get("quickchat.console.joinnull").replace("%player%", player.getDisplayName()));
            player.sendMessage(messageData.get("quickchat.channels.join").replace("%channel%", "Null"));
        }
    }

    public static synchronized int getPlayersWhoHeardYou(String channel, Player sender, String sendMessage, int i){

        ChatColor color = ChannelUtils.getColor(channel);
        ChatColor reset = ChatColor.RESET;
        String sendersName = sender.getName();

        int PlayersWhoHeardYou = i;
        for(Channel channelInList: channels.values()){
            for(String playerInChannel: channelInList.getplayers()){
                Player reciever = Bukkit.getPlayerExact(playerInChannel);

                if(reciever != null)
                    if(reciever.hasPermission("quickchat.channel." + channel) || reciever.hasPermission("quickchat.channel")){

                        if(inRange(sender, reciever, channel)){
                            String finalMessage = sendMessage;
                            if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                    && !QuickChat.isIgnored(sendersName, reciever.getDisplayName()))
                                finalMessage = MessageUtils.ping(reciever, finalMessage);

                            finalMessage = color + "<" + reset + QuickChat.getPlayerPrefix(sender) + sendersName
                                    + QuickChat.getPlayerSuffix(sender) + color + "> " + reset + finalMessage;

                            if(!QuickChat.isIgnored(sendersName, reciever.getDisplayName()) && !reciever.equals(sender)){
                                reciever.sendMessage(finalMessage);
                                PlayersWhoHeardYou++;
                            }
                        }
                    }
            }
        }

        for(String PlayerInConversation: PlayerChannelUtils.getPlayersInConversation()){
            Player reciever = Bukkit.getPlayerExact(PlayerInConversation);
            if(reciever != null)
                if(reciever.hasPermission("quickchat.channel." + channel) || reciever.hasPermission("quickchat.channel")){

                    if(inRange(sender, reciever, channel)){
                        String finalMessage = sendMessage;
                        if(!reciever.equals(sender) && sender.hasPermission("quickchat.ping")
                                && !QuickChat.isIgnored(sendersName, reciever.getDisplayName()))
                            finalMessage = MessageUtils.ping(reciever, finalMessage);

                        finalMessage = color + "<" + reset + QuickChat.getPlayerPrefix(sender) + sendersName
                                + QuickChat.getPlayerSuffix(sender) + color + "> " + reset + finalMessage;

                        if(!QuickChat.isIgnored(sendersName, reciever.getDisplayName()) && !reciever.equals(sender))
                            reciever.sendMessage(finalMessage);
                        PlayersWhoHeardYou++;
                    }
                }
        }
        return PlayersWhoHeardYou;
    }

    public static synchronized boolean playerIsAlreadyInChannel(String channel, Player p){
        if(getFullChannel(p.getDisplayName()) == null){
            return false;
        }else{
             return channels.get(channel).getName().equals(getFullChannel(p.getDisplayName()).getName());
        }
    }

    public static synchronized boolean isDuplicate(String channel){
        return channels.containsKey(channel);
    }

    public static synchronized boolean exists(String channel){
        return channels.containsKey(channel);
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
