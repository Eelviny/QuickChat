package co.justgame.quickchat.collections;

import java.util.HashMap;
import java.util.UUID;


public class LastPlayers {
    
    private static HashMap<UUID, UUID> lastPlayers = new HashMap<UUID, UUID>();
    
    public static synchronized UUID getLastPlayer(UUID sendersName){
        UUID op = lastPlayers.get(sendersName);
        return op;
    }

    public static synchronized void removeLastPlayers(String string){
        if(lastPlayers.containsKey(string)) lastPlayers.remove(string);
    }

    public static synchronized void addLastPlayers(UUID uuid, UUID sendersName){
        lastPlayers.put(uuid, sendersName);
    }

}
