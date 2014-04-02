package quickChat;

import java.util.ArrayList;

import org.bukkit.ChatColor;

public class Channel {

	private ArrayList<String> players;

	private String name;
	private int radius;
	private ChatColor color;

	public Channel(String name, int radius, ChatColor color){

		this.name = name;
		this.radius = radius;
		this.color = color;
		players = new ArrayList<String>();

	}

	public Channel(){

		this.name = "";
		this.radius = 100;
		this.color = ChatColor.WHITE;
		players = new ArrayList<String>();
	}

	public String getName(){
		return this.name;
	}

	public int getradius(){
		return this.radius;
	}

	public ChatColor getColor(){
		return this.color;
	}

	public ArrayList<String> getplayers(){
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

	public void addPlayer(String player){
		players.add(player);
	}

	public void removePlayer(String player){
		players.remove(players.indexOf(player));
	}
}
