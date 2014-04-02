package quickChat;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageUtils {
	
	public static String ping(Player player, String sendMessage){
		if(player.hasPermission("quickchat.ping") && StringUtils.containsIgnoreCase(sendMessage, player.getDisplayName())
				|| StringUtils.containsIgnoreCase(sendMessage, removeNums(player.getDisplayName()))){

			sendMessage = pingFormat(sendMessage, player.getDisplayName());
			player.playSound(Bukkit.getPlayerExact(player.getDisplayName()).getLocation(), QuickChat.getPingSound(), QuickChat
					.getPingVolume(), QuickChat.getPingPitch());
		}
		return sendMessage;
	}
	
	public static String pingFormat(String scource, String name){
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
			return scource.replaceAll("(?i)" + name, QuickChat.getPingFormat() + name + formatprefix.toString());
		}else{
			return scource.replaceAll("(?i)" + removeNums(name), QuickChat.getPingFormat() + removeNums(name)
					+ formatprefix.toString());
		}
	}
	
	private static String removeFormatCodes(String string){
		string = string.replace("&l", "").replace("&n", "").replace("&o", "").replace("&k", "").replace("&m", "")
				.replace("&r", "");
		return string;
	}
	
	private static String removeColorCodes(String string){
		string = string.replace("&1", "").replace("&2", "").replace("&3", "").replace("&4", "")
				.replace("&5", "").replace("&6", "").replace("&7", "").replace("&8", "").replace("&9", "")
				.replace("&0", "").replace("&a", "").replace("&b", "").replace("&c", "").replace("&d", "")
				.replace("&e", "").replace("&f", "").replace("&r", "");
		return string;
	}
	
	public static String removeNums(String name){
		String nameWithoutNums = "";
		for(int i = 0; i < name.length(); i++){
			if(name.charAt(i) < '0' || name.charAt(i) > '9') nameWithoutNums = nameWithoutNums + name.charAt(i);
		}
		return nameWithoutNums;
	}
	
	public static String formatCodes(Player player, String sendMessage){

		if(player.hasPermission("quickchat.code.format")){
			sendMessage = FormatStringFormats(sendMessage);
		}else{
			sendMessage = removeFormatCodes(sendMessage);
		}
		if(player.hasPermission("quickchat.code.color")){
			sendMessage = FormatStringColors(sendMessage);
		}else{
			sendMessage = removeColorCodes(sendMessage);
		}
		return sendMessage;
	}

	public static String formatString(String string){
		return string.replace("&", "§");
	}

	public static String deFormatString(String string){

		string = string.replace("§", "&");
		return string;
	}

	public static String FormatStringFormats(String string){

		string = string.replace("&l", "§l").replace("&n", "§n").replace("&o", "§o").replace("&k", "§k").replace("&m", "§m")
				.replace("&r", "§r");
		return string;
	}

	public static String FormatStringColors(String string){

		string = string.replace("&1", "§1").replace("&2", "§2").replace("&3", "§3").replace("&4", "§4")
				.replace("&5", "§5").replace("&6", "§6").replace("&7", "§7").replace("&8", "§8").replace("&9", "§9")
				.replace("&0", "§0").replace("&a", "§a").replace("&b", "§b").replace("&c", "§c").replace("&d", "§d")
				.replace("&e", "§e").replace("&f", "§f").replace("&r", "§r");
		return string;
	}
}
