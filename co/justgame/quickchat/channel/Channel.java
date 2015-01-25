package co.justgame.quickchat.channel;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;

public class Channel {

    private ArrayList<UUID> players;

    private String name;
    private int radius;
    private ChatColor color;

    public Channel(String name, int radius, ChatColor color){

        this.name = name;
        this.radius = radius;
        this.color = color;
        players = new ArrayList<UUID>();

    }

    public Channel(){

        this.name = "";
        this.radius = 100;
        this.color = ChatColor.WHITE;
        players = new ArrayList<UUID>();
    }

    public String getName(){
        return this.name;
    }

    public synchronized int getradius(){
        return this.radius;
    }

    public ChatColor getColor(){
        return this.color;
    }

    public ArrayList<UUID> getplayers(){
        return this.players;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public void setColor(ChatColor color){
        this.color = color;
    }

    public synchronized void addPlayer(UUID player){
        players.add(player);
    }

    public synchronized void removePlayer(UUID player){
        players.remove(players.indexOf(player));
    }
}
