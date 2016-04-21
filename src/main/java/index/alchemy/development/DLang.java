package index.alchemy.development;

import index.alchemy.item.IItemInventory;
import index.alchemy.item.ItemMagicSolvent;
import index.alchemy.util.Tool;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;

public class DLang {
	
	public static Map<String, String> itemMap, blockMap, potionMap, enchantmentMap, keyMap, inventoryMap, miscMap;
	public static Map<Class<?>, Method> funcMap;
	static {
		for (Field field : DLang.class.getDeclaredFields())
			try {
				field.set(null, new LinkedHashMap());
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		for (Method method : DLang.class.getDeclaredMethods()) {
			Class[] clazzs = method.getParameterTypes();
			if (clazzs.length == 1 && clazzs[0] != Object.class) 
				funcMap.put(clazzs[0], method);
		}
	}
	
	public static void init(Object obj) {
		for (Class<?> supers : funcMap.keySet())
			if (Tool.isInstance(supers, obj.getClass()))
				try {
					funcMap.get(supers).invoke(null, obj);
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
	
	public static void init(IItemInventory inventory) {
		inventoryMap.put(inventory.getInventoryUnlocalizedName(), "");
	}
	
	public static void init(ItemMagicSolvent.Type type) {
		miscMap.put(key, value);
	}

}
