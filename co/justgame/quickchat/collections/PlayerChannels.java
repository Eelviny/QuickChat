package co.justgame.quickchat.collections;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;


public class PlayerChannels {
    
    private static HashMap<UUID, UUID> playerChannels = new HashMap<UUID, UUID>();
    
    public static synchronized void removePlayerChannel(UUID string){
        playerChannels.remove(string);
    }

    public static synchronized void addPlayerChannel(UUID string, UUID string2){
        playerChannels.put(string, string2);
    }
    
    public static synchronized UUID getPartner(UUID p){
        return playerChannels.get(p);
    }
    
    public static synchronized Set<UUID> getPlayersInConversation(){
        return playerChannels.keySet();
    }
    
    public static synchronized boolean playerHasPlayerChannel(UUID sendersName){
        return playerChannels.containsKey(sendersName);
    }
}
