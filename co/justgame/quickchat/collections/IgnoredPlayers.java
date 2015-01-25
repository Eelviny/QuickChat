package co.justgame.quickchat.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import co.justgame.quickchat.utils.ProcessorUtils;


public class IgnoredPlayers {
    
    private static HashMap<UUID, ArrayList<UUID>> ignoredPlayers = new HashMap<UUID, ArrayList<UUID>>();
    
    public static synchronized String getIgnoringPlayers(UUID sendersName){
        StringBuilder playersIgnoring = new StringBuilder();

        for(UUID id: ignoredPlayers.keySet()){
            if(ignoredPlayers.get(id).contains(sendersName)){
                playersIgnoring.append(" " + ProcessorUtils.getDisplayName(id));
            }
        }
        if(playersIgnoring.toString().trim().split(" ").length > 2){
            playersIgnoring = new StringBuilder(" " + playersIgnoring.toString().trim().replace(" ", ", "));
            playersIgnoring
                    .replace(playersIgnoring.lastIndexOf(", "), playersIgnoring.lastIndexOf(", ") + 1, ", and");
        }else if(playersIgnoring.toString().trim().split(" ").length > 1){
            playersIgnoring = new StringBuilder(" " + playersIgnoring.toString().trim().replace(" ", " and "));
        }
        return playersIgnoring.toString();
    }
    
    public static synchronized String getIgnoredPlayers(UUID p){
        StringBuilder playersIgnored = new StringBuilder();

        for(UUID id: ignoredPlayers.get(p)){
              playersIgnored.append(" " + ProcessorUtils.getDisplayName(id));
        }
        
        if(playersIgnored.toString().trim().split(" ").length > 2){
            playersIgnored = new StringBuilder(" " + playersIgnored.toString().trim().replace(" ", ", "));
            playersIgnored.replace(playersIgnored.lastIndexOf(", "), playersIgnored.lastIndexOf(", ") + 1, ", and");
        }else if(playersIgnored.toString().trim().split(" ").length > 1){
            playersIgnored = new StringBuilder(" " + playersIgnored.toString().trim().replace(" ", " and "));
        }
        return playersIgnored.toString();
    }

    public static synchronized void addIgnoredPlayer(UUID string){
        ignoredPlayers.put(string, new ArrayList<UUID>());
    }

    public static synchronized void removeIgnoredPlayer(String string){
        ignoredPlayers.remove(string);
    }

    public static synchronized void addIgnoredPlayerToPlayer(UUID string, UUID string2){
        ignoredPlayers.get(string).add(string2);
    }

    public static synchronized void removeIgnoredPlayerFromPlayer(UUID sendersName, UUID playersName){
        ignoredPlayers.get(sendersName).remove(playersName);
    }
    
    public static synchronized boolean isIgnored(UUID sendersName, UUID player){
        return ignoredPlayers.get(player).contains(sendersName);
    }

}
