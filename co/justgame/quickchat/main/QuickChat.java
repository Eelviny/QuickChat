package co.justgame.quickchat.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.chat.Chat;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import co.justgame.quickchat.channel.Channel;
import co.justgame.quickchat.channel.ChannelUtils;
import co.justgame.quickchat.listeners.LoginLogoutListener;
import co.justgame.quickchat.listeners.chatListener;
import co.justgame.quickchat.listeners.utils.MessageUtils;

public class QuickChat extends JavaPlugin {

    public static Plugin quickChat;

    public static HashMap<String, String> messageData = new HashMap<String, String>();
    private static HashMap<String, String> playerChannels = new HashMap<String, String>();
    private static HashMap<String, String> lastPlayers = new HashMap<String, String>();
    private static HashMap<String, ArrayList<String>> ignoredPlayers = new HashMap<String, ArrayList<String>>();

    public static String broadCastName = "";
    public static ChatColor privateColor = ChatColor.LIGHT_PURPLE;

    public static Sound pingSound = Sound.ORB_PICKUP;
    public static ChatColor pingFormat = ChatColor.ITALIC;
    public static int pingVolume = 60;
    public static int pingPitch = 0;

    public static Chat chat = null;

    FileConfiguration config;

    @Override
    public void onEnable(){
        getLogger().info("QuickChat has been enabled");
        quickChat = this;

        config = this.getConfig();

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
        saveConfig();

        File Messages = new File(getDataFolder() + File.separator + "messages.yml");
        if(!Messages.exists()) try{
            Messages.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        setUpMessagesYML();

        broadCastName = MessageUtils.formatString(config.getString("broadcast"));
        try{
            privateColor = ChatColor.valueOf(config.getString("private"));
            pingSound = Sound.valueOf(config.getString("ping.sound"));
            pingFormat = ChatColor.valueOf(config.getString("ping.format"));
            pingVolume = config.getInt("ping.volume");
            pingPitch = config.getInt("ping.pitch");
        }catch (IllegalArgumentException e){
            getConsole().sendMessage(MessageUtils.formatString("[QuickChat] " + "&cError loading config.yml!"
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
            getConsole().sendMessage(MessageUtils.formatString("[QuickChat] " + "&cError loading config.yml!"
                    + " Unparsable value in config!"));
        }catch (DuplicateChannelException e){
            getConsole().sendMessage(MessageUtils.formatString("[QuickChat] " + "&cError loading config.yml!"
                    + " Duplicate channel in config!"));
        }

        try{
            FileConfiguration config = YamlConfiguration.loadConfiguration(Messages);
            for(String message: config.getConfigurationSection("").getKeys(true)){
                messageData.put(message, MessageUtils.formatString(config.getString(message)));
            }
        }catch (Exception e){
            getConsole().sendMessage(MessageUtils.formatString("[QuickChat] " + "&cError loading messages.yml!"));
        }

        setupChat();

        for(Player player: Bukkit.getOnlinePlayers()){
            lastPlayers.put(player.getDisplayName(), "Null");
            QuickChat.addIgnoredPlayer(player.getDisplayName());
            ChannelUtils.addPlayerToFirstAvailableChannel(player);
        }

        lastPlayers.put("Console", "Null");

        getServer().getPluginManager().registerEvents(new chatListener(), this);
        getServer().getPluginManager().registerEvents(new LoginLogoutListener(), this);

    }

    @Override
    public void onDisable(){
        getLogger().info("QuickChat has been disabled");
    }

    @Override
    public synchronized boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(cmd.getName().equalsIgnoreCase("!")){

            if(sender instanceof Player){
                sender.sendMessage(messageData.get("quickchat.console.broadcastplayer"));
                return true;
            }

            String trimMessage = StringUtils.join(args, " ");
            trimMessage = MessageUtils.formatString(trimMessage);

            getConsole().sendMessage("[QuickChat] "
                    + messageData.get("quickchat.console.broadcast").replace("%player%", "CONSOLE")
                            .replace("%message%", QuickChat.getBroadCastName() + " " + trimMessage.trim()));

            for(Player onlinePlayer: Bukkit.getOnlinePlayers()){
                onlinePlayer.sendMessage(QuickChat.getBroadCastName() + " " + trimMessage.trim());
            }

        }else if(cmd.getName().equalsIgnoreCase(">")){
            // Message: > <Message>
            String sendersName;
            if(sender instanceof Player){
                sender.sendMessage(messageData.get("quickchat.console.rawtextplayer"));
                return true;
            }else{
                sendersName = "Console";
            }

            if(args.length > 1){
                String playerName = args[0];
                String sendMessage = MessageUtils.formatString(StringUtils.join(args, " "));
                if(args.length >= 2) sendMessage = sendMessage.split(" ", 2)[1].trim();

                if(playerName.equalsIgnoreCase("*") && args.length >= 2){
                    QuickChat.getConsole().sendMessage(messageData.get("quickchat.console.rawtext")
                            .replace("%player%", sendersName).replace("%message%", sendMessage));

                    for(Player reciever: Bukkit.getOnlinePlayers()){
                        reciever.sendMessage(sendMessage);
                    }
                }else if(playerName.equalsIgnoreCase("?")){
                    sender.sendMessage(messageData.get("quickchat.console.rawhelp"));
                }else if(playerName.equalsIgnoreCase("console")){
                    if(sender instanceof Player) sender.sendMessage("<You (raw text) -> " + sendersName + "> " + sendMessage);
                    QuickChat.getConsole().sendMessage(sendMessage);
                    QuickChat.getlastPlayers().put("Console", sendersName);
                }else if(args.length >= 2){
                    List<Player> players = Bukkit.matchPlayer(playerName);

                    if(players.size() == 0){
                        sender.sendMessage(messageData.get("quickchat.rawtext.noplayer").replace("%player%", playerName));
                    }else if(players.size() > 1){
                        sender.sendMessage(messageData.get("quickchat.rawtext.moreplayer"));
                    }else{
                        Player reciever = players.get(0);
                        if(sender instanceof Player)
                            sender.sendMessage("<You (raw text) -> " + reciever.getDisplayName() + "> " + sendMessage);
                        QuickChat.getConsole().sendMessage("<" + sendersName + " (raw text) -> " + reciever.getDisplayName()
                                + "> " + sendMessage);
                        if(!reciever.equals(sender)){
                            reciever.sendMessage(sendMessage);

                            QuickChat.getlastPlayers().put(reciever.getDisplayName(), "Console");
                        }
                    }
                }else{
                    sender.sendMessage(messageData.get("quickchat.rawtext.consoleimproperusage"));
                }
            }else{
                sender.sendMessage(messageData.get("quickchat.rawtext.consoleimproperusage"));
            }

            return true;
        }else if(cmd.getName().equalsIgnoreCase("@")){
            if(sender instanceof Player){
                sender.sendMessage(messageData.get("quickchat.console.privateplayer"));
                return true;
            }else{
                String sendersName = "Console";
                // Message: @
                if(args.length == 0){
                    HashMap<String, String> playerChannels = QuickChat.getPlayerChannels();
                    HashMap<String, String> lastPlayers = QuickChat.getlastPlayers();

                    StringBuilder buildMessage = new StringBuilder();

                    if(!lastPlayers.get(sendersName).equals("Null"))
                        buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", lastPlayers
                                .get(sendersName)));
                    else
                        buildMessage.append(messageData.get("quickchat.private.lastplayer").replace("%player%", "&4None"));

                    if(playerHasPlayerChannel(sendersName)){
                        String otherPlayer = playerChannels.get(sendersName);
                        buildMessage.append(messageData.get("quickchat.private.conversation").replace("%player%", otherPlayer));
                    }else{
                        buildMessage.append(messageData.get("quickchat.private.conversation").replace("%player%", "&4None"));
                    }

                    sendMultilineMessage(sender, MessageUtils.formatString(buildMessage.toString()));

                    // Message: @<Player>
                }else if(args.length == 1){
                    HashMap<String, String> playerChannels = QuickChat.getPlayerChannels();

                    if(args[0].equalsIgnoreCase("console")){
                        ChannelUtils.removePlayerFromChannelIfOneExists(sendersName);
                        if(playerChannels.containsKey(sendersName)){
                            if(!playerChannels.get(sendersName).equalsIgnoreCase("Console")){

                                playerChannels.put(sendersName, "Console");
                                sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                        .replace("%player%", "the Console"));

                            }else{
                                sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                        .replace("%player%", "the Console"));
                            }
                        }else{
                            playerChannels.put(sendersName, "Console");

                            sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                    .replace("%player%", "the Console"));
                        }

                    }else if(args[0].equals("?")){

                        sender.sendMessage(messageData.get("quickchat.console.privatehelp"));

                    }else{

                        List<Player> players = Bukkit.matchPlayer(args[0]);

                        if(players.size() == 0){
                            sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", args[0]));
                        }else if(players.size() > 1){
                            sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                        }else{
                            ChannelUtils.removePlayerFromChannelIfOneExists(sendersName);
                            String otherPlayersName = players.get(0).getDisplayName();

                            if(playerChannels.containsKey(sendersName)){
                                if(!playerChannels.get(sendersName).equals(otherPlayersName)){

                                    playerChannels.put(sendersName, otherPlayersName);
                                    sender.sendMessage(messageData.get("quickchat.private.joinchannel")
                                            .replace("%player%", otherPlayersName));

                                }else{
                                    sender.sendMessage(messageData.get("quickchat.private.samechannel")
                                            .replace("%player%", otherPlayersName));
                                }
                            }else{
                                playerChannels.put(sendersName, otherPlayersName);

                                sender.sendMessage(messageData.get("quickchat.private.joinchannel").replace("%player%", players
                                        .get(0).getDisplayName()));
                            }
                        }
                    }
                    // Message: @<Player> <Message>
                }else if(args.length > 1){
                    String playerName = args[0];
                    String sendMessage = StringUtils.join(args, " ");
                    sendMessage = MessageUtils.formatString(sendMessage.split(" ", 2)[1]);

                    if(playerName.equalsIgnoreCase("Console")){
                        String fullMessage = getPrivateColor() + "<§r" + sendersName + " -> " + "Console" + getPrivateColor()
                                + ">§r " + sendMessage;
                        sender.sendMessage(fullMessage);
                        QuickChat.getConsole().sendMessage(fullMessage);

                    }else if(playerName.equals("?")){

                        sender.sendMessage(messageData.get("quickchat.console.privatehelp"));

                    }else if(playerName.equalsIgnoreCase("LP")){

                        sendMessage = MessageUtils.formatString(sendMessage);

                        if(!lastPlayers.get(sendersName).equals("Null")){
                            if(lastPlayers.get(sendersName).equalsIgnoreCase("console")){
                                String fullMessage = getPrivateColor() + "<§r" + sendersName + " -> "
                                        + lastPlayers.get(sendersName) + getPrivateColor() + ">§r " + sendMessage;
                                sender.sendMessage(fullMessage);
                            }else{
                                Player reciever = Bukkit.getPlayerExact(lastPlayers.get(sendersName));

                                if(reciever != null){
                                    String fullMessage = getPrivateColor() + "<§r" + sendersName + " -> "
                                            + reciever.getDisplayName() + getPrivateColor() + ">§r " + sendMessage;
                                    if(!reciever.equals(sender)) reciever.sendMessage(fullMessage);
                                    sender.sendMessage(fullMessage);

                                }else{
                                    sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                            .replace("%player%", lastPlayers.get(sendersName)));
                                }
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.private.nolastplayer"));
                        }

                    }else if(playerName.equalsIgnoreCase("C")){

                        if(playerChannels.containsKey(sendersName)){

                            sendMessage = MessageUtils.formatString(sendMessage);

                            if(playerChannels.get(sendersName).equalsIgnoreCase("Console")){

                                String fullMessage = getPrivateColor() + "<§r" + sendersName + " -> " + "Console"
                                        + getPrivateColor() + ">§r " + sendMessage;
                                sender.sendMessage(fullMessage);

                            }else{
                                Player reciever = Bukkit.getPlayerExact(playerChannels.get(sendersName));

                                if(reciever == null){
                                    sender.sendMessage(messageData.get("quickchat.private.lastPlayerLeft")
                                            .replace("%player%", playerChannels.get(sendersName)));
                                    QuickChat.getPlayerChannels().remove("Console");
                                }else{

                                    String reciversMessage = getPrivateColor() + "<§r" + sendersName + " -> "
                                            + reciever.getDisplayName() + getPrivateColor() + ">§r " + sendMessage;
                                    if(!reciever.equals(sender)) sender.sendMessage(reciversMessage);
                                    if(!reciever.equals(sender)) sendMessage = MessageUtils.ping(reciever, sendMessage);
                                    sendMessage = getPrivateColor() + "<§r" + sendersName + " -> " + reciever.getDisplayName()
                                            + getPrivateColor() + ">§r " + sendMessage;
                                    reciever.sendMessage(sendMessage);

                                    QuickChat.getlastPlayers().put(reciever.getDisplayName(), sendersName);
                                }
                            }
                        }else{
                            sender.sendMessage(messageData.get("quickchat.private.noconversation"));
                        }

                    }else{
                        List<Player> players = Bukkit.matchPlayer(playerName);

                        if(players.size() == 0){
                            sender.sendMessage(messageData.get("quickchat.private.noplayer").replace("%player%", playerName));
                        }else if(players.size() > 1){
                            sender.sendMessage(messageData.get("quickchat.private.moreplayer"));
                        }else{
                            Player reciever = players.get(0);

                            String sendersMessage = getPrivateColor() + "<§r" + sendersName + " -> " + reciever.getDisplayName()
                                    + getPrivateColor() + ">§r " + sendMessage;
                            if(!reciever.equals(sender)) sender.sendMessage(sendersMessage);
                            if(!reciever.equals(sender)) sendMessage = MessageUtils.ping(reciever, sendMessage);
                            sendMessage = getPrivateColor() + "<§r" + sendersName + " -> " + reciever.getDisplayName()
                                    + getPrivateColor() + ">§r " + sendMessage;
                            QuickChat.getlastPlayers().put(reciever.getDisplayName(), sendersName);
                            reciever.sendMessage(sendMessage);

                        }
                    }
                }
            }
        }else{
            return true;
        }
        return true;
    }

    private void setupChat(){
        if(getServer().getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager()
                    .getRegistration(net.milkbowl.vault.chat.Chat.class);
            if(rsp != null){
                chat = rsp.getProvider();
            }
        }
    }

    public static synchronized String getBroadCastName(){
        return broadCastName;
    }

    public static synchronized ChatColor getPrivateColor(){
        return privateColor;
    }

    public static synchronized Sound getPingSound(){
        return pingSound;
    }

    public static synchronized ChatColor getPingFormat(){
        return pingFormat;
    }

    public static synchronized int getPingVolume(){
        return pingVolume;
    }

    public static synchronized int getPingPitch(){
        return pingPitch;
    }

    public static synchronized ConsoleCommandSender getConsole(){
        return Bukkit.getServer().getConsoleSender();
    }

    public static synchronized Plugin getInstance(){
        return quickChat;
    }

    public static synchronized HashMap<String, String> getPlayerChannels(){
        return playerChannels;
    }

    public static synchronized void removePlayerChannel(String string){
        playerChannels.remove(string);
    }

    public static synchronized void addPlayerChannel(String string, String string2){
        playerChannels.put(string, string2);
    }

    public static synchronized HashMap<String, ArrayList<String>> getIgnoredPlayer(){
        return ignoredPlayers;
    }

    public static synchronized void addIgnoredPlayer(String string){
        ignoredPlayers.put(string, new ArrayList<String>());
    }

    public static synchronized void removeIgnoredPlayer(String string){
        ignoredPlayers.remove(string);
    }

    public static synchronized void addIgnoredPlayerToPlayer(String string, String string2){
        ignoredPlayers.get(string).add(string2);
    }

    public static synchronized void removeIgnoredPlayerFromPlayer(String string, String string2){
        ignoredPlayers.get(string).remove(string2);
    }

    public static synchronized HashMap<String, String> getlastPlayers(){
        return lastPlayers;
    }

    public static synchronized void removeLastPlayers(String string){
        if(lastPlayers.containsKey(string)) lastPlayers.remove(string);
    }

    public static synchronized void addLastPlayers(String string, String string2){
        lastPlayers.put(string, string2);
    }

    public static synchronized HashMap<String, String> getMessageData(){
        return messageData;
    }

    public static synchronized String getPlayerPrefix(Player player){
        if(chat != null){
            if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine") != null){
                if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine").isEnabled()){
                    String world = null;
                    try{
                        if(chat.getPlayerInfoString(world, player.getDisplayName(), "prefix", "") != null){
                            return MessageUtils
                                    .formatString(chat.getPlayerInfoString(world, player.getDisplayName(), "prefix", ""));
                        }else{
                            return "";
                        }
                    }catch (Exception e){
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] "
                                        + MessageUtils.formatString("&cCaught Exception: " + e.toString() + " Message:"
                                                + e.getMessage()));
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] " + MessageUtils.formatString("&cCause: " + e.getCause()));
                        return "";
                    }
                }
            }

            if(chat.getPlayerPrefix(player) != null){
                return MessageUtils.formatString(chat.getPlayerPrefix(player));
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public static synchronized String getPlayerSuffix(Player player){
        if(chat != null){
            if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine") != null){
                if(Bukkit.getServer().getPluginManager().getPlugin("CubeEngine").isEnabled()){
                    String world = null;
                    try{
                        if(chat.getPlayerInfoString(world, player.getDisplayName(), "suffix", "") != null){
                            return MessageUtils
                                    .formatString(chat.getPlayerInfoString(world, player.getDisplayName(), "suffix", ""));
                        }else{
                            return "";
                        }
                    }catch (Exception e){
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] "
                                        + MessageUtils.formatString("&cCaught Exception: " + e.toString() + " Message:"
                                                + e.getMessage()));
                        QuickChat.getConsole()
                                .sendMessage("[QuickChat] " + MessageUtils.formatString("&cCause: " + e.getCause()));
                        return "";
                    }
                }
            }

            if(chat.getPlayerSuffix(player) != null){
                return MessageUtils.formatString(chat.getPlayerSuffix(player));
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    private void setUpMessagesYML(){
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
        setMessage("quickchat.private.noplayer", "&cPlayer &4%player%&c not found!");
        setMessage("quickchat.private.moreplayer", "&cPlease be more specific. More than one player found!");
        setMessage("quickchat.private.playerleft", "&aYou are no longer in a private conversation with %player%");

        setMessage("quickchat.rawtext.noplayer", "&cPlayer &4%player%&c not found!");
        setMessage("quickchat.rawtext.moreplayer", "&cPlease be more specific. More than one player found!");
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

    private void setMessage(String name, String message){
        File f = new File(getDataFolder() + File.separator + "messages.yml");
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

    private boolean playerHasPlayerChannel(String Player){
        HashMap<String, String> playerChannels = QuickChat.getPlayerChannels();

        for(String playersChannel: playerChannels.keySet()){
            if(playersChannel.equals(Player)){
                return true;
            }
        }
        return false;
    }

    private void sendMultilineMessage(CommandSender sender, String message){
        if(sender != null && message != null){
            String[] s = message.split("/n");
            for(String m: s){
                sender.sendMessage(m);
            }
        }
    }

    class DuplicateChannelException extends Exception {

        private static final long serialVersionUID = 1L;

        public DuplicateChannelException(){
        }
    }

}
