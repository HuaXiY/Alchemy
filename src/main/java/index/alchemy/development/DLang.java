package index.alchemy.development;

import index.alchemy.item.IItemInventory;
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
		//potionMap.put(potion.get, value)
	}
	
	public static void init(Enchantment enchantment) {
		
	}
	
	public static void init(KeyBinding key) {
		
	}
	
	public static void init(IItemInventory inventory) {
		
	}

}
