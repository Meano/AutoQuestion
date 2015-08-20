package net.meano.AutoQuestion;

import java.util.Random;

import net.meano.AutoQuestion.AutoQuestionMain.PlayerInfo;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AutoQuestionListeners  implements Listener{
	AutoQuestionMain AQM;
	AutoQuestionListeners(AutoQuestionMain GetPlugin){
		AQM=GetPlugin;
	}
	//玩家聊天事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		for(PlayerInfo playerinfo : AQM.WhoIdle){
			if(player.equals(playerinfo.PlayerEntity)){
				if(playerinfo.isIdle){
					playerinfo.PlayerEntity.sendMessage(new StringBuffer().append(ChatColor.YELLOW).append(ChatColor.BOLD).append(AQM.Questions.getString(playerinfo.IdleQuestionNode+".Q")).toString());
					playerinfo.PlayerEntity.sendMessage(new StringBuffer().append(ChatColor.RED).append(ChatColor.BOLD).append("请回答问题，输入/aq 答案 即可作答。").toString());
					event.setCancelled(true);
				}else{
					if(playerinfo.IdleCount>=1){
						playerinfo.IdleCount=0;
					}
				}
				break;
			}
		}
	}
	//玩家输入命令的事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		for(PlayerInfo playerinfo : AQM.WhoIdle){
			if(player.equals(playerinfo.PlayerEntity)){
				if(playerinfo.isIdle){
					if(!(event.getMessage().toLowerCase().startsWith("/aq")||event.getMessage().toLowerCase().startsWith("/ap"))){
						playerinfo.PlayerEntity.sendMessage(new StringBuffer().append(ChatColor.RED).append(ChatColor.BOLD).append("现在只允许输入命令/aq 答案来作答，当然/AQ /aQ /Aq 都可以使用。").toString());		
						event.setCancelled(true);
					}
				}else{
					if(playerinfo.IdleCount>=1){
						playerinfo.IdleCount=0;
					}
				}
				break;
			}
		}
	}
	//玩家破坏方块事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Random CheckRate = new Random();
		if(CheckRate.nextInt(20)==5){
			Player player = event.getPlayer();
			for(PlayerInfo playerinfo : AQM.WhoIdle){
				if(player.equals(playerinfo.PlayerEntity)){
					if(!playerinfo.isIdle){
						if(playerinfo.IdleCount>=1){
							playerinfo.IdleCount=0;
						}
					}
					break;
				}
			}
		}
	}
	//玩家放置方块事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Random CheckRate = new Random();
		if(CheckRate.nextInt(20)==5){
			Player player = event.getPlayer();
			for(PlayerInfo playerinfo : AQM.WhoIdle){
				if(player.equals(playerinfo.PlayerEntity)){
					if(!playerinfo.isIdle){
						if(playerinfo.IdleCount>=1){
							playerinfo.IdleCount=0;
						}
					}
					break;
				}
			}
		}
	}
}
