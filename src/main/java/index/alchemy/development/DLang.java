package index.alchemy.development;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import index.alchemy.api.IInventoryProvider;
import index.alchemy.api.annotation.DInit;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.FinalFieldSetter;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemRecord;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Alpha
@DInit
@SideOnly(Side.CLIENT)
public class DLang {
	
	public static final String SUFFIX = ".lang";
	
	private static final String lang_dir = DMain.resources + "/lang";
	
	private static Set<String> mcSet = new HashSet<String>(); 
	
	public static final Map<String, String> itemMap = null, blockMap = null, potionMap = null, enchantmentMap = null,
			keyMap = null, damageMap = null, inventoryMap = null, miscMap = null;
	public static final Map<Class<?>, Method> _funcMap = null;
	static {
		for (Field field : DLang.class.getDeclaredFields())
			if (field.getType() == Map.class)
				try {
					FinalFieldSetter.instance().setStatic(field, new LinkedHashMap<>());
				} catch (Exception e) {
					AlchemyRuntimeException.onException(e);
				}
		
		for (Method method : DLang.class.getDeclaredMethods()) {
			Class[] clazzs = method.getParameterTypes();
			if (method.getName().equals("init") && clazzs.length == 1 && clazzs[0] != Object.class) 
				_funcMap.put(clazzs[0], method);
		}
		
		try {
			mcSet.addAll(getMap(new File(lang_dir, AlchemyConstants.MC_VERSION + SUFFIX)).keySet());
		} catch (IOException e) {
			AlchemyModLoader.logger.warn("Can't load: " + AlchemyConstants.MC_VERSION + SUFFIX);
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
		for (String name : dir.list())
			if (name.endsWith(SUFFIX) && !name.matches(".*\\d.*"))
				save(name);
	}
	
	public static void save(String name) {
		AlchemyModLoader.info("Save", name);
		File file = new File(lang_dir, name);
		try {
			Map<String, String> map = getMap(file);
			StringBuilder builder = new StringBuilder("//MODID\n" + AlchemyConstants.MOD_ID + "=" +
					Tool.isNullOr(map.get(AlchemyConstants.MOD_ID), AlchemyConstants.MOD_ID) + "\n");
			for (Field field : DLang.class.getDeclaredFields())
				if (Tool.setAccessible(field).getType() == Map.class && !field.getName().startsWith("_")) {
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
	
	public static String getName(String str) {
		return getName(str, 0);
	}
	
	public static String getName(String str, int offset) {
		String temp[] = str.split("\\.");
		return temp.length < offset + 1 ? "" : temp[temp.length - offset - 1];
	}
	
	public static void init(Object obj) {
		for (Class<?> supers : _funcMap.keySet())
			if (supers.isInstance(obj))
				try {
					_funcMap.get(supers).invoke(null, obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
	}
	
	public static void init(Item item) {
		if (item instanceof ItemRecord) {
			String key = Tool.<String>$(item, "displayName");
			miscMap.put(key, getName(key, 1));
		} else if (!(item instanceof ItemBlock))
			itemMap.put(item.getUnlocalizedName() + ".name", getName(item.getUnlocalizedName()));
	}
	
	public static void init(CreativeTabs tab) {
		miscMap.put("itemGroup." + tab.getTabLabel(), AlchemyConstants.MOD_ID);
	}
	
	public static void init(Block block) {
		blockMap.put(block.getUnlocalizedName() + ".name", getName(block.getUnlocalizedName()));
	}
	
	public static void init(Potion potion) {
		potionMap.put(potion.getName(), getName(potion.getName()));
	}
	
	public static void init(PotionType potion) {
		String name = getName(potion.getNamePrefixed("."));
		potionMap.put(potion.getNamePrefixed("potion.effect."), name);
		potionMap.put(potion.getNamePrefixed("splash_potion.effect."), name);
		potionMap.put(potion.getNamePrefixed("lingering_potion.effect."), name);
		potionMap.put(potion.getNamePrefixed("tipped_arrow.effect."), name);
	}
	
	public static void init(Enchantment enchantment) {
		enchantmentMap.put(enchantment.getName(), getName(enchantment.getName()));
	}
	
	public static void init(KeyBinding key) {
		keyMap.put(key.getKeyDescription(), getName(key.getKeyDescription()));
	}
	
	public static void init(DamageSource damage) {
		damageMap.put("death.attack." + damage.getDamageType(), "%1$s");
	}
	
	public static void init(IInventoryProvider inventory) {
		inventoryMap.put(inventory.getInventoryUnlocalizedName(), getName(inventory.getInventoryUnlocalizedName()));
	}
	
}
