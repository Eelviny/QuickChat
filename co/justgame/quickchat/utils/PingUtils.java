package co.justgame.quickchat.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PingUtils {
    
    private static Sound pingSound = Sound.ORB_PICKUP;
    private static ChatColor pingFormat = ChatColor.ITALIC;
    private static int pingVolume = 60;
    private static int pingPitch = 0;
    
    public static String ping(Player player, String sendMessage){
        if(player.hasPermission("quickchat.ping") && StringUtils.containsIgnoreCase(sendMessage, player.getDisplayName())
                || StringUtils.containsIgnoreCase(sendMessage, removeNums(player.getDisplayName()))){

            sendMessage = pingFormat(sendMessage, player.getDisplayName());
            player.playSound(player.getLocation(), pingSound, pingVolume, pingPitch);
        }
        return sendMessage;
    }

    private static String pingFormat(String scource, String name){
        StringBuilder formatprefix = new StringBuilder();
        formatprefix.append("§r");
        for(int i = 0; i < scource.length(); i++){
            if(scource.charAt(i) == '&' || scource.charAt(i) == '§'){
                try{
                    formatprefix.append(String.valueOf(scource.charAt(i)));
                    formatprefix.append(String.valueOf(scource.charAt(i + 1)));
                }catch (StringIndexOutOfBoundsException e){
                    formatprefix.append(String.valueOf(scource.charAt(i)));
                }
            }
        }

        if(StringUtils.contains(scource, name)){
            return scource.replaceAll("(?i)" + name, pingFormat + name + formatprefix.toString());
        }else{
            return scource.replaceAll("(?i)" + removeNums(name), pingFormat + removeNums(name)
                    + formatprefix.toString());
        }
    }
    
    private static String removeNums(String name){
        String nameWithoutNums = "";
        for(int i = 0; i < name.length(); i++){
            if(name.charAt(i) < '0' || name.charAt(i) > '9') nameWithoutNums = nameWithoutNums + name.charAt(i);
        }
        return nameWithoutNums;
    }
    
    public static void setPingSound(Sound s){
        pingSound = s;
    }

    public static void setPingFormat(ChatColor pf){
        pingFormat = pf;
    }
    
    public static void setPingVolume(int i){
        pingVolume = i;
    }

    public static void setPingPitch(int i){
        pingPitch = i;
    }
}
