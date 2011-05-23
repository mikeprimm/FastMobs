
package com.mikeprimm.bukkit.FastMobs;
import java.util.HashMap;

import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Random;
import org.bukkit.entity.Monster;
import org.bukkit.util.Vector;
import java.util.LinkedList;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;


/**
 * FastMobs plugin - make monster mobs move faster when targetting a player
 *
 * @author MikePrimm
 */
public class FastMobsPlugin extends JavaPlugin {
	public static Logger log = Logger.getLogger("Minecraft");
	
    private final FastMobsEntityListener entityListener = new FastMobsEntityListener(this);
        
    private Random rnd = new Random(System.currentTimeMillis());
    
    private HashMap<Integer, Monster> watchedmobs = new HashMap<Integer, Monster>();    
    
    private double creeper_speedup = 1.0;
    private double spider_speedup = 1.0;
    private double zombie_speedup = 1.0;
    private double skeleton_speedup = 1.0;
    private double ghast_speedup = 1.0;
    
    private class ProcessTargettedMobs implements Runnable {
    	int tick = 0;
    	LinkedList<Monster> deadpool = new LinkedList<Monster>();
    	
    	public void run() {    		
    		for(Monster m : watchedmobs.values()) {
    			if(m.isDead()) {
					deadpool.add(m);
					continue;
    			}
    			Vector v = m.getVelocity();
				double speed = v.length();
				if(speed > 0) {
	    			double newspeed = getSpeedForMob(m, speed);
					v.setX(v.getX() * newspeed / speed);
					v.setZ(v.getZ() * newspeed / speed);
					m.setVelocity(v);
				}
    		}
    		
    		while(deadpool.isEmpty() == false) {
    			Monster m = deadpool.poll();
    			watchedmobs.remove(m.getEntityId());
    		}
    	}
    }
    
    private double getSpeedForMob(Monster m, double cur_speed) {
    	double scale = 1.0;
    	double base = 0.4;
    	if(m instanceof Creeper) {
    		scale = creeper_speedup;
    		base = 0.4;
    	}
    	else if(m instanceof Zombie) {
    		scale = zombie_speedup;
    		base = 0.4;
    	}
    	else if(m instanceof Spider) {
    		scale = spider_speedup;
    		base = 0.5;
    	}
    	else if(m instanceof Ghast) {
    		scale = ghast_speedup;
    		base = 0.4;
    	}
    	else if(m instanceof Skeleton) {
    		scale = skeleton_speedup;
    		base = 0.4;
    	}
    	double tgtspeed = scale * base;
    	if(cur_speed < tgtspeed) {
    		cur_speed = cur_speed + ((tgtspeed-cur_speed) * 0.2);	/* 20% towards target */
    	}
    	return cur_speed;
    }
    
    /* On disable, stop doing our function */
    public void onDisable() {
    	/* Since our registered listeners are disabled by default, we don't need to do anything */
    }

    public void onEnable() {
    	/* Read in our configuration */
        readConfig();

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[FastMobs] version " + pdfFile.getVersion() + " is enabled" );
        /* Start job to handle targetted mobs */
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new ProcessTargettedMobs(), 0, 1);
    }
    
    private void readConfig() {    	
    	File configdir = getDataFolder();	/* Get our data folder */
    	if(configdir.exists() == false) {	/* Not yet defined? */
    		configdir.mkdirs();				/* Create it */
    	}
    	/* Initialize configuration object */
    	File configfile = new File(configdir, "FastMobs.yml");	/* Our YML file */
    	Configuration cfg = new Configuration(configfile);
    	if(configfile.exists() == false) {	/* Not defined yet? */
    		PrintWriter fos = null;
    		try {
    			fos = new PrintWriter(new FileWriter(configfile));
    			fos.println("# Configuration file for FastMobs");
    			fos.println("creeper-speedup: 1.5");
    			fos.println("zombie-speedup: 1.5");
    			fos.println("skeleton-speedup: 1.5");
    			fos.println("ghast-speedup: 1.5");
    			fos.println("spider-speedup: 1.5");
    			fos.close();
    		} catch (IOException iox) {
    			System.out.println("ERROR writing default configuration for FastMobs");
    			return;
    		}
    	}
    	cfg.load();		/* Load it */
    	
    	spider_speedup = cfg.getDouble("spider-speedup", 1.0);
    	zombie_speedup = cfg.getDouble("zombie-speedup", 1.0);
    	skeleton_speedup = cfg.getDouble("skeleton-speedup", 1.0);
    	creeper_speedup = cfg.getDouble("creeper-speedup", 1.0);
    	ghast_speedup = cfg.getDouble("ghast-speedup", 1.0);
    }
    
    void watchMob(Monster mob) {
    	watchedmobs.put(mob.getEntityId(), mob);
    }
    
    void clearMob(Monster mob) {
    	watchedmobs.remove(mob.getEntityId());
    }
}
