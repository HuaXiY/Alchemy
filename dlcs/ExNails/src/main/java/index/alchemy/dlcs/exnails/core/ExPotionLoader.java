package index.alchemy.dlcs.exnails.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import index.alchemy.api.IItemPotion;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class ExPotionLoader {
	
	private static final Logger logger = LogManager.getLogger(ExPotionLoader.class.getSimpleName());
	
	public static class EffectNode {
		
		public final Item item;
		public List<PotionEffect> effects[];
		
		public EffectNode(Item item) {
			this.item = item;
		}
		
	}
	
	private static final List<EffectNode> list = Lists.newArrayList();
	
	public static Stream<EffectNode> stream() { return list.stream(); }
	
	@Nullable
	public static EffectNode findNode(Item item) {
		for (EffectNode node : list)
			if (node.item == item)
				return node;
		return null;
	}
	
	public static void loadConfig(File file) throws IOException {
		Files.readLines(file, Charsets.UTF_8).forEach(s -> {
			int index = s.indexOf('#');
			if (index != -1)
				s = s.substring(0, index);
			if (!s.isEmpty()) {
				String args[] = s.split(" ");
				if (args.length > 1) {
					try {
						Item item = Item.getByNameOrId(args[0]);
						if (item != null) {
							if (item instanceof IItemPotion) {
								IItemPotion itemPotion = (IItemPotion) item;
								itemPotion.clearEffects();
								for (int i = 1, meta = 0; i < args.length; i++, meta++) {
									List<PotionEffect> effects = toEffects(args[i]);
									itemPotion.setEffects(meta, effects);
									logger.info("Set: " + args[0] + ":" + meta + " effects: " + effects);
								}
							} else {
								logger.info(item.getRegistryName() + " does not implement IItemPotion");
								EffectNode nowNode = findNode(item);
								if (nowNode == null)
									list.add(nowNode = new EffectNode(item));
								int len = args.length - 1;
								nowNode.effects = new List[len];
								for (int i = 1, meta = 0; i < args.length; i++, meta++) {
									List<PotionEffect> effects = toEffects(args[i]);
									nowNode.effects[meta] = effects;
									logger.info("Set: " + args[0] + ":" + meta + " effects: " + effects);
								}
							}
						} else
							logger.info("Can not find: " + args[0]);
					} catch (Exception e) { logger.info("Can not parst: " + e.getMessage()); }
				}
			}
		});
	}
	
	public static List<PotionEffect> toEffects(String str) {
		List<PotionEffect> effects = Lists.newArrayList();
		try {
			String args[] = str.split("/");
			for (int i = 0; i < args.length - 2; i += 3) {
				Potion potion = Potion.getPotionFromResourceLocation(args[i]);
				if (potion != null) {
					int duration = Integer.parseInt(args[i + 1]);
					int amplifier = Integer.parseInt(args[i + 2]);
					effects.add(new PotionEffect(potion, duration, amplifier));
				}
			}
		} catch (Exception e) { }
		return effects;
	}

}
