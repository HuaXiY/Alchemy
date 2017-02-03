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

import index.alchemy.api.IItemThirst;
import net.minecraft.item.Item;

public class ExThirstLoader {
	
	private static final Logger logger = LogManager.getLogger(ExThirstLoader.class.getSimpleName());
	
	public static class ThirstNode {
		
		public final Item item;
		public int thirst[];
		public float hydration[];
		
		public ThirstNode(Item item) {
			this.item = item;
		}
		
	}
	
	private static final List<ThirstNode> list = Lists.newArrayList();
	
	public static Stream<ThirstNode> stream() { return list.stream(); }
	
	@Nullable
	public static ThirstNode findNode(Item item) {
		for (ThirstNode node : list)
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
				if (args.length > 2 && args.length % 2 == 1) {
					try {
						Item item = Item.getByNameOrId(args[0]);
						if (item != null) {
							if (item instanceof IItemThirst) {
								IItemThirst itemThirst = (IItemThirst) item;
								for (int i = 1, meta = 0; i < args.length; i += 2, meta++) {
									int thirst = Integer.parseInt(args[i]);
									float hydration = Float.parseFloat(args[i + 1]);
									itemThirst.setThirst(meta, thirst);
									itemThirst.setHydration(meta, hydration);
									logger.info("Set: " + args[0] + ":" + meta + " thirst: " + thirst + ", hydration: " + hydration);
								}
							} else {
								logger.info(item.getRegistryName() + " does not implement IItemThirst");
								ThirstNode nowNode = findNode(item);
								if (nowNode == null)
									list.add(nowNode = new ThirstNode(item));
								int len = (args.length - 1) / 2;
								nowNode.thirst = new int[len];
								nowNode.hydration = new float[len];
								for (int i = 1, meta = 0; i < args.length; i += 2, meta++) {
									int thirst = Integer.parseInt(args[i]);
									float hydration = Float.parseFloat(args[i + 1]);
									nowNode.thirst[meta] = thirst;
									nowNode.hydration[meta] = hydration;
									logger.info("Set: " + args[0] + ":" + meta + " thirst: " + thirst + ", hydration: " + hydration);
								}
							}
						} else
							logger.info("Can not find: " + args[0]);
					} catch (Exception e) { logger.info("Can not parst: " + e.getMessage()); }
				}
			}
		});
	}

}
