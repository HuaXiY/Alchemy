package index.alchemy.development;

import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.item.IItemInventory;
import index.alchemy.item.ItemMagicSolvent;
import index.alchemy.util.Tool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.DamageSource;

@DInit
public class DLang {
	
	public static final String SUFFIX = ".lang";
	
	public static String lang_dir = DMain.resources + "/lang";
	
	public static Set<String> mcSet = new HashSet<String>(); 
	
	public static Map<String, String> itemMap, blockMap, potionMap, enchantmentMap, keyMap, damageMap, inventoryMap, miscMap;
	public static Map<Class<?>, Method> _funcMap;
	static {
		for (Field field : DLang.class.getDeclaredFields())
			if (field.getType() == Map.class)
				try {
					field.set(null, new LinkedHashMap());
				} catch (Exception e) {
					e.printStackTrace();
				}
		
		for (Method method : DLang.class.getDeclaredMethods()) {
			Class[] clazzs = method.getParameterTypes();
			if (method.getName().equals("init") && clazzs.length == 1 && clazzs[0] != Object.class) 
				_funcMap.put(clazzs[0], method);
		}
		
		try {
			mcSet.addAll(getMap(new File(lang_dir, Constants.MC_VERSION + SUFFIX)).keySet());
		} catch (IOException e) {
			AlchemyModLoader.logger.warn("Can't load: " + Constants.MC_VERSION + SUFFIX);
		}
	}
	
	public static Map<String, String> getMap(File file) throws IOException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		String lang = Tool.read(file);
		for (String line : lang.split("\n")) {
			String mapping[] = line.split("=");
			if (mapping.length == 2)
				map.put(mapping[0], mapping[1]);
		}
		return map;
	}
	
	public static void save() {
		File dir = new File(lang_dir);
		for (String file : dir.list())
			if (file.endsWith(SUFFIX))
				save(file);
	}
	
	public static void save(String name) {
		AlchemyModLoader.logger.info("        save: " + name);
		File file = new File(lang_dir, name);
		Map<String, String> map = new LinkedHashMap<String, String>();
		try {
			String lang = Tool.read(file);
			for (String line : lang.split("\n")) {
				String mapping[] = line.split("=");
				if (mapping.length == 2)
					map.put(mapping[0], mapping[1]);
			}
			StringBuilder builder = new StringBuilder("//MODID\n" + Constants.MODID + "=" +
					Tool.isNullOr(map.get(Constants.MODID), Constants.MODID) + "\n");
			for (Field field : DLang.class.getDeclaredFields())
				if (field.getType() == Map.class && !field.getName().startsWith("_")) {
					builder.append("\n//" + field.getName() + "\n");
					for (Entry<String, String> entry : ((Map<String, String>) field.get(null)).entrySet())
						if (!mcSet.contains(entry.getKey()))
							builder.append(entry.getKey() + "=" + Tool.isEmptyOr(map.get(entry.getKey()), entry.getValue()) + "\n");
				}
			Tool.save(file, builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public static void init() {
		save();
	}
	
	public static void init(Object obj) {
		for (Class<?> supers : _funcMap.keySet())
			if (Tool.isInstance(supers, obj.getClass()))
				try {
					_funcMap.get(supers).invoke(null, obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	public static void init(Item item) {
		itemMap.put(item.getUnlocalizedName() + ".name", "");
	}
	
	public static void init(Block block) {
		blockMap.put(block.getUnlocalizedName() + ".name", "");
	}
	
	public static void init(Potion potion) {
		potionMap.put(potion.getName(), "");
	}
	
	public static void init(PotionType potion) {
		potionMap.put(potion.getNamePrefixed("potion.effect."), "");
		potionMap.put(potion.getNamePrefixed("splash_potion.effect."), "");
		potionMap.put(potion.getNamePrefixed("lingering_potion.effect."), "");
		potionMap.put(potion.getNamePrefixed("tipped_arrow.effect."), "");
	}
	
	public static void init(Enchantment enchantment) {
		enchantmentMap.put(enchantment.getName(), "");
	}
	
	public static void init(KeyBinding key) {
		keyMap.put(key.getKeyDescription(), "");
	}
	
	public static void init(DamageSource damage) {
		damageMap.put("death.attack." + damage.getDamageType(), "%1$s");
	}
	
	public static void init(IItemInventory inventory) {
		inventoryMap.put(inventory.getInventoryUnlocalizedName(), "");
	}
	
	public static void init(ItemMagicSolvent.Type type) {
		miscMap.put(type.PREFIX + type.name().toLowerCase(), "");
	}

}
