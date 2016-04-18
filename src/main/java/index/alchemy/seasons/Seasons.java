package index.alchemy.seasons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

public class Seasons {
	
	public static final int SPRING = 0, SUMMER = 1, AUTUMN = 2, WINTER = 3, NULL = -1;
	public static final List<Seasons> seasons_list = new ArrayList<Seasons>();
	private World world;
	private int last_seasons, seasons, cycle;
	
	public static Seasons findSeasons(World world) {
		for (Seasons s : seasons_list) 
			if (s.world == world) return s;
		Seasons s;
		seasons_list.add(s = new Seasons(world));
		return s;
	}
	
	public Seasons(World world) {
		this(world, 28 * 24000);
	}
	
	public Seasons(World world, int cycle) {
		this.world = world;
		this.cycle = cycle;
		
		nextTick();
		last_seasons = seasons;
		seasons_list.add(this);
	}
	
	public void onTick() {
		nextTick();
		if (last_seasons != seasons) {
			
			
		}
	}
	
	public void nextTick() {
		last_seasons = seasons;
		seasons = (int) (world.getWorldTime() % cycle) % 4;
	}
	
}
