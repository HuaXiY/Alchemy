package index.alchemy.interacting;

import java.lang.reflect.Field;

import index.alchemy.core.Init;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import net.minecraft.item.Item;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.AVAILABLE)
public class ModItems {
	
	@Source(clazz = "biomesoplenty.api.item.BOPItems")
	public static Item 
		a;
	
	public static void init() throws Exception {
		printItems("biomesoplenty.api.item.BOPItems");
		for (Field field : ModItems.class.getFields()) {
			System.out.println(field.getName() + " - " + field.get(null));
			Source source = field.getAnnotation(Source.class);
			if (source != null)
				try {
					field.set(null, Class.forName(source.clazz()).getField(field.getName()).get(null));
				} catch (Exception e) {
					//throw new AlchemyRuntimeExcption(e);
				}
		}
	}
	
	public static void printItems(String clazz) throws Exception {
		for (Field field : Class.forName(clazz).getFields()) {
			Item item = ((Item) field.get(null));
			System.out.println(field.getName() + " - " + I18n.translateToLocal(item == null ? "" : item.getUnlocalizedName() + ".name"));
		}
	}

}
