package co.justgame.quickchat.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import co.justgame.quickchat.channel.Channel;
import co.justgame.quickchat.main.QuickChat;
import co.justgame.quickchat.processors.PrivateMessageProcessor;
import co.justgame.quickchat.processors.ServerBroadcastProcessor;
import co.justgame.quickchat.utils.DuplicateClassException.DuplicateChannelException;


public class ConfigUtils implements MessageData {
    
    static FileConfiguration config;
    static Plugin p;
    
    public static void setUpConfig(Plugin pl){
        p = pl;
        refreshPrimaryConfig();
        refreshMessageConfig();
        
        readConfigs();
    }
    
    private static void refreshPrimaryConfig(){
        config = p.getConfig();

        config.options().header("");

        config.addDefault("broadcast", "<&3Server&r>");
        config.addDefault("private", "LIGHT_PURPLE");
        config.addDefault("ping.format", "ITALIC");
        config.addDefault("ping.sound", "ORB_PICKUP");
        config.addDefault("ping.volume", 60);
        config.addDefault("ping.pitch", 0);

        config.addDefault("channels.global.radius", -1);
        config.addDefault("channels.global.color", "WHITE");
        config.addDefault("channels.local.radius", 100);
        config.addDefault("channels.local.color", "YELLOW");
        config.addDefault("channels.staff.radius", -1);
        config.addDefault("channels.staff.color", "AQUA");

        config.options().copyDefaults(true);
        p.saveConfig();
    }
    
    private static void readConfigs(){
        
        ServerBroadcastProcessor.setBroadCastName(MessageFormatUtils.formatString(config.getString("broadcast")));
        
        try{
           PrivateMessageProcessor.setPrivateColor(ChatColor.valueOf(config.getString("private")));
           PingUtils.setPingSound(Sound.valueOf(config.getString("ping.sound")));
           PingUtils.setPingFormat(ChatColor.valueOf(config.getString("ping.format")));
           PingUtils.setPingVolume(config.getInt("ping.volume"));
           PingUtils.setPingPitch(config.getInt("ping.pitch"));
        }catch (IllegalArgumentException e){
            QuickChat.getConsole().sendMessage(MessageFormatUtils.formatString("[QuickChat] " + "&cError loading config.yml!"
                    + " Unparsable value in config!"));
        }

        Channel channel = new Channel();
        ChannelUtils.clear();
        try{
            for(String message: config.getConfigurationSection("channels").getKeys(true)){

                if(!message.contains(".radius") && !message.contains(".color")){
                    channel.setName(message);
                }else if(message.contains(".radius")){
                    channel.setRadius(config.getInt("channels." + message));
                }else if(message.contains(".color")){
                    channel.setColor(ChatColor.valueOf(config.getString("channels." + message).trim()));
                    if(!ChannelUtils.isDuplicate(channel.getName().toLowerCase()))
                        ChannelUtils.addNewChannel(channel.getName(), channel);
                    else
                        throw new DuplicateChannelException();
                    channel = new Channel();
                }
            }
        }catch (IllegalArgumentException e){
            QuickChat.getConsole().sendMessage(MessageFormatUtils.formatString("[QuickChat] " + "&cError loading config.yml!"
                    + " Unparsable value in config!"));
        }catch (DuplicateChannelException e){
            QuickChat.getConsole().sendMessage(MessageFormatUtils.formatString("[QuickChat] " + "&cError loading config.yml!"
                    + " Duplicate channel in config!"));
        }
        
        File Messages = new File(p.getDataFolder() + File.separator + "messages.yml");
        if(!Messages.exists()) try{
            Messages.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            FileConfiguration config = YamlConfiguration.loadConfiguration(Messages);
            for(String message: config.getConfigurationSection("").getKeys(true)){
                messageData.put(message, MessageFormatUtils.formatString(config.getString(message)));
            }
        }catch (Exception e){
            QuickChat.getConsole().sendMessage(MessageFormatUtils.formatString("[QuickChat] " + "&cError loading messages.yml!"));
        }
    }
    
    private static void refreshMessageConfig(){
        
        setMessage("quickchat.requests.noplayer", "&cPlayer &4%player%&c not found!");
        setMessage("quickchat.requests.moreplayer", "&cPlease be more specific. More than one player found!");
        
        setMessage("quickchat.channels.info.channel", "&aCurrent Channel:&2 %channel% /n");
        setMessage("quickchat.channels.info.radius", "&aRadius:&2 %radius% /n");
        setMessage("quickchat.channels.info.playerlist", "&aPlayers Who Can Hear You:&2%playerList% /n");
        setMessage("quickchat.channels.info.channellist", "&aChannels You Can Join:&2 %channelList%.");

        setMessage("quickchat.channels.join", "&aYou are now in %channel% channel");
        setMessage("quickchat.channels.samechannel", "&cYou are already in %channel% channel!");
        setMessage("quickchat.channels.nopermission", "&cYou do not have permission to join the %channel% channel!");
        setMessage("quickchat.channels.nopermissionsend", "&cYou do not have permission to send a message to the %channel% channel!");
        setMessage("quickchat.channels.notexist", "&cChannel&4 %channel%&c does not exist!");
        setMessage("quickchat.channels.null", "&cCannot send messages while in Null channel! ");

        setMessage("quickchat.private.lastPlayerLeft", "&c%player% is no longer online!");
        setMessage("quickchat.private.lastplayer", "&aLast Player: &2%player%/n");
        setMessage("quickchat.private.nolastplayer", "&cNo player has messaged you!");
        setMessage("quickchat.private.conversation", "&aCurrent Conversation With: &2%player%/n");
        setMessage("quickchat.private.noconversation", "&cYou are not currently in a conversation!");
        setMessage("quickchat.private.joinchannel", "&aYou began a private conversation with %player%.");
        setMessage("quickchat.private.samechannel", "&cYou are already in a conversation with %player%!");
        setMessage("quickchat.private.playerleft", "&aYou are no longer in a private conversation with %player%");

        setMessage("quickchat.rawtext.improperusage", "&cUsage: >Player <Message> or > <Message>");
        setMessage("quickchat.rawtext.consoleimproperusage", "&cUsage: > <Player> <Message> or > * <Message>");
        setMessage("quickchat.rawtext.nopermission", "&cYou do not have permission to use this command!");

        setMessage("quickchat.broadcast.nopermission", "&cYou do not have permission to use this command!");

        setMessage("quickchat.ignore.channel", "&cCannot join channel with %player%! %player% is are ignoring you.");
        setMessage("quickchat.ignore.message", "&cCannot send message to %player%! %player% is ignoring you.");
        setMessage("quickchat.ignore.op", "&cYou cannot ignore %player%!");
        setMessage("quickchat.ignore.reciever", "&a%player% is now ignoring you!");
        setMessage("quickchat.ignore.recieverun", "&a%player% is no longer ignoring you!");
        setMessage("quickchat.ignore.self", "&cYou cannot ignore yourself!");
        setMessage("quickchat.ignore.ignoring", "&aYou are now ignoring %player%");
        setMessage("quickchat.ignore.improperusage", "&cUsage: ~<Player>");
        setMessage("quickchat.ignore.notignoring", "&aYou are no longer ignoring %player%");
        setMessage("quickchat.ignore.listignored", "&aPlayers You Are Ignoring:&2%playerList%/n");
        setMessage("quickchat.ignore.listignoring", "&aPlayers Ignoring You:&2%playerList%");
        
        setMessage("quickchat.mute.usage", "&c-<Player>");
        setMessage("quickchat.mute.message", "&cYou may not chat now, You are muted!");
        setMessage("quickchat.mute.mute", "&eMuted %p%!");
        setMessage("quickchat.mute.othermute", "&eYou have been muted!");
        setMessage("quickchat.mute.unmute", "&aUnmuted %p%!");
        setMessage("quickchat.mute.otherunmute", "&aYou have been Unmuted!");

        setMessage("quickchat.info.nobody", "&cNobody heard you! No players in channel or within range.");
        setMessage("quickchat.info.playerschannel", "&a%player% is in %channel% channel.");
        setMessage("quickchat.info.playersconversation", "&a%player% is in a private conversation.");
        setMessage("quickchat.info.console", "&aThe Console is &4&oEverywhere&c... &calways watching you...");

        setMessage("quickchat.console.joinplayerchannel", "%player% joined conversation with %otherplayer%.");
        setMessage("quickchat.console.privatehelp", "&aUse 'LP' as the recieving player to send the message to the last player that sent a message to you. "
                + "Use 'C' as the recieving player to send a message to the player you are currently in a conversation  with.");
        setMessage("quickchat.console.rawhelp", "&aUse '*' as the recieving player to send the raw text to the whole server.");
        setMessage("quickchat.console.joinchannel", "%player% joined %channel% channel.");
        setMessage("quickchat.console.joinnull", "&4WARNING:&C %player% was forced to join &4Null&c channel.");
        setMessage("quickchat.console.remove", "Removed %player% from all channels");
        setMessage("quickchat.console.privateplayer", "&cYou do not have permission to use this command. Remove the '/'.");
        setMessage("quickchat.console.broadcast", "%player% broadcasted: %message%");
        setMessage("quickchat.console.broadcastplayer", "&cYou do not have permission to use this command. Remove the '/'.");
        setMessage("quickchat.console.rawtext", "%player% sent raw text to server: %message%");
        setMessage("quickchat.console.rawtextplayer", "&cYou do not have permission to use this command. Remove the '/'.");
    }
    
    private static void setMessage(String name, String message){
        File f = new File(p.getDataFolder() + File.separator + "messages.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(f);
        if(!config.isSet(name)){
            config.set(name, message);
            try{
                config.save(f);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
