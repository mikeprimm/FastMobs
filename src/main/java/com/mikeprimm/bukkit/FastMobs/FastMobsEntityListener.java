
package com.mikeprimm.bukkit.FastMobs;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityListener;
import java.util.Random;

/**
 * Entity listener - listen for spawns of wolves
 * @author MikePrimm
 */
public class FastMobsEntityListener extends EntityListener {
    private final FastMobsPlugin plugin;
    private final Random rnd = new Random(System.currentTimeMillis());
    
    public FastMobsEntityListener(final FastMobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {
    	if(event.isCancelled())
    		return;
    	/* See if its a mob */
    	Entity e = event.getEntity();
    	if(!(e instanceof Monster)) {
    		return;
    	}
    	/* See if its target is a player */
    	Entity t = event.getTarget();
    	if(t == null) {
    		/* Clear the mob, if we were tracking it */
    		plugin.clearMob((Monster)e);
    	}
    	else if(t instanceof Player) {
    		/* Set mob to being tracked */
    		plugin.watchMob((Monster)e);
    	}
    }
}
