package net.meano.AutoQuestion;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoQuestionMain extends JavaPlugin {
	public static AutoQuestionMain Self ;
	private FileConfiguration PluginConfig;
	public ConfigurationSection Questions;
	private int Version;
	private int CheckMinutes;
	private int WaitSeconds;
	private int IdleAllowCounts;
	private int CheckLocationDifference;
	private String IdleKickMessage;
	private String CorrectMessage;
	private String WrongMessage;
	public String[] QuestionsNode;
	public String[] ExemptPlayers;
	public List<PlayerInfo> WhoIdle;
	public boolean hasIdle;
	public boolean OPExempt;
	public class PlayerInfo{
		PlayerInfo(Player P,String N,Location L,int I,boolean i){
			PlayerEntity = P;
			Name = N;
			PlayerLocation = L;
			IdleCount = I;
			isIdle = i;
		}
		Player PlayerEntity;
		String Name;
		Location PlayerLocation;
		int IdleCount;
		boolean isIdle;
		String IdleQuestionNode;
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("AutoQuestion")){
			if(args.length>0){
				if(sender instanceof Player){
					Player player = (Player) sender;
					for(PlayerInfo playerinfo : WhoIdle){
						if(player.equals(playerinfo.PlayerEntity)){
							if(player.isOp()){
								if(args[0].toLowerCase().toLowerCase().equals("test")){
									CheckIdle();
								}
							}
							if(playerinfo.isIdle){
								if(args[0].toLowerCase().equals(Questions.getString(playerinfo.IdleQuestionNode+".A").toLowerCase())){
									playerinfo.isIdle = false;
									playerinfo.IdleCount = 0;
									player.sendMessage(ChatColor.GREEN+CorrectMessage);
								}else{
									player.sendMessage(ChatColor.RED+WrongMessage);
								}
							}else{
								player.sendMessage(ChatColor.RED+"命令不适用");
							}
						}
					}
				}
			}else{
				sender.sendMessage(ChatColor.RED+"如需回答问题请输入/aq 答案");
			}
			return true;
		}else if(cmd.getName().equalsIgnoreCase("AutoQuestionAdmin")){
			if(args.length>0){
				if(args[0].toLowerCase().equals("exempt")){
					if(args.length==2){
						AddAnExemptPlayer(args[1].toLowerCase());
						sender.sendMessage(ChatColor.GREEN+"玩家"+args[1]+"已加入免检查列表。");
						return true;
					}else{
						sender.sendMessage(ChatColor.RED+"参数长度不正确！");
						return true;
					}
				}else if(args[0].toLowerCase().equals("check")){
					if(args.length==2){
						DueAnExemptPlayer(args[1].toLowerCase());
						sender.sendMessage(ChatColor.GREEN+"玩家"+args[1]+"已加入检查列表。");
						return true;
					}else{
						sender.sendMessage(ChatColor.RED+"参数长度不正确！");
						return true;
					}
				}else{
					sender.sendMessage(ChatColor.RED+"没有这项功能！");
					return true;
				}
			}else{
				sender.sendMessage(ChatColor.RED+"使用/aqa exempt 玩家名，将玩家名加入免检查列表");
				return true;
			}
		}
		return false;
	}
	public void onEnable() {
		Self = this;
		File PluginConfigFile = new File(getDataFolder(), "config.yml");
	        if (!PluginConfigFile.exists()) {
	        	saveDefaultConfig();
	        }
	        PluginConfig = getConfig();
	        Version = PluginConfig.getInt("Config.Version");
	        if(Version!=3){
	        	PluginConfig.set("Config.Version", 3);
	        	PluginConfig.set("Config.OPExempt", true);
	        	PluginConfig.createSection("ExemptPlayers");
	        	PluginConfig.set("ExemptPlayers.meano",true);
	        	PluginConfigFile.renameTo(new File(getDataFolder(),"Version."+Version+".bak.cofig.yml"));
	        	saveConfig();
	        	getLogger().info("配置文件更新！原配置文件已备份，请更新题库！");
	        }
	        Questions = PluginConfig.getConfigurationSection("Questions");
	        CheckMinutes = PluginConfig.getInt("Config.CheckMinutes");
	        WaitSeconds = PluginConfig.getInt("Config.WaitSeconds");
	        IdleAllowCounts =  PluginConfig.getInt("Config.IdleAllowCounts");
	        CheckLocationDifference = PluginConfig.getInt("Config.CheckLocationDifference");
	        IdleKickMessage = PluginConfig.getString("Config.IdleKickMessage");
	        CorrectMessage = PluginConfig.getString("Config.CorrectMessage");
	        WrongMessage = PluginConfig.getString("Config.WrongMessage");
	        QuestionsNode = Questions.getKeys(false).toArray(new String[0]);
	        OPExempt = PluginConfig.getBoolean("Config.OPExempt");
	        ExemptPlayers = PluginConfig.getConfigurationSection("ExemptPlayers").getKeys(false).toArray(new String[0]);
	        WhoIdle = new ArrayList<PlayerInfo>();
	        
	        Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
	        	new Runnable() {
				public void run() {
					CheckIdle();
				}
			}
		,1*5*20, CheckMinutes*60*21);
	        PluginManager PM = Bukkit.getServer().getPluginManager();
		PM.registerEvents(new AutoQuestionListeners(this), this);
        }
	public void AddAnExemptPlayer(String Name){
		PluginConfig.set("ExemptPlayers."+Name,true);
		saveConfig();
	}
	public void DueAnExemptPlayer(String Name){
		PluginConfig.set("ExemptPlayers."+Name,false);
		saveConfig();
	}
	public void CheckIdle(){
		//建立Idlelist的迭代器
		Iterator<PlayerInfo> PlayerIterator = WhoIdle.iterator();
		//移除Idlelist已下线的玩家
		while(PlayerIterator.hasNext()){
			if(!PlayerIterator.next().PlayerEntity.isOnline()){
				PlayerIterator.remove();
			}
		}
		for(Player player : Bukkit.getOnlinePlayers()){
			if(OPExempt){
				if(player.isOp())continue;
			}
			if(PluginConfig.contains("ExemptPlayers."+player.getName().toLowerCase())){
				if(PluginConfig.getBoolean("ExemptPlayers."+player.getName().toLowerCase())){
					continue;
				}
			}
			boolean isInIdleList = false;
			for(final PlayerInfo playerinfo : WhoIdle){
				if(player.equals(playerinfo.PlayerEntity)){
					//查询是否是在线玩家
					boolean isMayIdle = false;
					int Difference = CalculateLocationDifference(playerinfo.PlayerLocation,player.getLocation());
					//getLogger().info("格数"+Difference+"Check"+CheckLocationDifference+player.getLocation().getBlock().getType().toString());
					if(Difference>=CheckLocationDifference){
						if((!player.isInsideVehicle())){
							if(!player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER)){
								playerinfo.IdleCount=0;
								playerinfo.isIdle = false;
							}else{
								isMayIdle = true;
							}
						}else{
							//getLogger().info(player.getVehicle().getType().toString());
							if(player.getVehicle().getType().equals(EntityType.MINECART)){
								if(Difference<450*CheckMinutes){
									isMayIdle = true;
								}else{
									playerinfo.IdleCount=0;
									playerinfo.isIdle = false;
								}
							}else if(player.getVehicle().getType().equals(EntityType.BOAT)){
								if(Difference<300*CheckMinutes){
									isMayIdle = true;
								}else{
									playerinfo.IdleCount=0;
									playerinfo.isIdle = false;
								}
							}else{
								if(Difference<200*CheckMinutes){
									isMayIdle = true;
								}else{
									playerinfo.IdleCount=0;
									playerinfo.isIdle = false;
								}
							}
						}
					}else{
						isMayIdle = true;
					}
					if(isMayIdle){
						playerinfo.IdleCount++;
						if(playerinfo.IdleCount>=IdleAllowCounts){
							playerinfo.isIdle = true;
							playerinfo.PlayerEntity.sendMessage(new StringBuffer().append(ChatColor.GREEN).append(ChatColor.BOLD).append(playerinfo.Name).append(",你有").append(WaitSeconds).append("秒的时间输入以下问题的答案，否则你将被断开连接！").toString());
							Random RandSelect = new Random();
							playerinfo.IdleQuestionNode = QuestionsNode[RandSelect.nextInt(QuestionsNode.length)];
							playerinfo.PlayerEntity.sendMessage(new StringBuffer().append(ChatColor.YELLOW).append(ChatColor.BOLD).append(Questions.getString(playerinfo.IdleQuestionNode+".Q")).toString());
							playerinfo.PlayerEntity.sendMessage(new StringBuffer().append(ChatColor.RED).append(ChatColor.BOLD).append("输入/aq 答案 即可作答。").toString());
							Bukkit.getScheduler().scheduleSyncDelayedTask(AutoQuestionMain.Self, 
								new Runnable(){
									public void run(){
										if(playerinfo.isIdle){
											playerinfo.PlayerEntity.kickPlayer(IdleKickMessage);
											playerinfo.isIdle = false;
										}
									}
								}, WaitSeconds*20L);
							getLogger().info(playerinfo.Name+"[被判定挂机]");
						}
					}
					playerinfo.PlayerLocation = playerinfo.PlayerEntity.getLocation();
					isInIdleList = true;
					break;
				}
			}
			if(!isInIdleList){
				WhoIdle.add(new PlayerInfo(player,player.getName(),player.getLocation(),0,false));
			}
		}
	}
	public int CalculateLocationDifference(Location A,Location B){
		int Difference;
		Difference=Math.abs(A.getBlockX()-B.getBlockX())+Math.abs(A.getBlockY()-B.getBlockY())*2+Math.abs(A.getBlockZ()-B.getBlockZ());
		return Difference;
	}
}
