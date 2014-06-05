package co.justgame.quickchat.channel;

import java.util.HashMap;
import java.util.Set;


public class PlayerChannelUtils {
    
    private static HashMap<String, String> playerChannels = new HashMap<String, String>();
    
    public static synchronized void removePlayerChannel(String string){
        playerChannels.remove(string);
    }

    public static synchronized void addPlayerChannel(String string, String string2){
        playerChannels.put(string, string2);
    }
    
    public static synchronized String getPartner(String p){
        return playerChannels.get(p);
    }
    
    public static synchronized Set<String> getPlayersInConversation(){
        return playerChannels.keySet();
    }
    
    public static synchronized boolean playerHasPlayerChannel(String sendersName){
        return playerChannels.containsKey(sendersName);
    }
}
