package index.alchemy.interacting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import index.alchemy.core.Init;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import index.alchemy.util.FinalFieldSetter;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.INITIALIZED)
public class ModItems {
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ItemTransform {
		
		public String name() default "";

	}
	
	//  Biomes O' Plenty

	@Source(clazz = "biomesoplenty.api.item.BOPItems")
	public static final Item 
			bop$gem = null;
	
	@ItemTransform(name = "bop$gem")
	public static final ItemStack
			bop$gem_amethyst = null,						//  末影紫晶
			bop$gem_ruby = null,							//  红宝石
			bop$gem_peridot = null,						//  橄榄石
			bop$gem_topaz = null,							//  黄玉
			bop$gem_tanzanite = null,						//  坦桑石
			bop$gem_malachite = null,					//  孔雀石
			bop$gem_sapphire = null,						//  蓝宝石
			bop$gem_amber = null;							//  琥珀
	
	@Source(clazz = "biomesoplenty.api.block.BOPBlocks")
	public static final  Item 
			bop$flower_0 = null,
			bop$flower_1 = null;
	
	@ItemTransform(name = "bop$flower_0")
	public static final ItemStack
			bop$flower_clover = null,						//  苜蓿
			bop$flower_swampflower = null,			//  沼泽花
			bop$flower_deathbloom = null,			//  死亡花
			bop$flower_glowflower = null,			//  闪光花
			bop$flower_blue_hydrangea = null,		//  蓝绣球花
			bop$flower_orange_cosmos = null,		//  黄波斯菊
			bop$flower_pink_daffodil = null,			//  粉水仙
			bop$flower_wildflower = null,				//  野花
			bop$flower_violet = null,						//  紫罗兰
			bop$flower_white_anemone = null,		//  银莲花
			bop$flower_enderlotus = null,				//  末影莲花
			bop$flower_bromeliad = null,				//  凤梨
			bop$flower_wilted_lily = null,				//  凋零百合
			bop$flower_pink_hibiscus = null,			//  粉木槿
			bop$flower_lily_of_the_valley = null,	//  谷百合
			bop$flower_burning_blossom = null;	//  火焰花
	
	@ItemTransform(name = "bop$flower_1")
	public static final ItemStack
			bop$flower_lavender = null,					//  熏衣草
			bop$flower_goldenrod = null,				//  秋麒麟草
			bop$flower_bluebells = null,					//  蓝铃花
			bop$flower_miners_delight = null,		//  乐矿花
			bop$flower_icy_iris = null,					//  冰虹膜花
			bop$flower_rose = null;							//  玫瑰
	
	public static void init() throws Exception {
		String last = null;
		Class<?> clazz =null;
		for (Field field : ModItems.class.getFields()) {
			Source source = field.getAnnotation(Source.class);
			if (source != null) {
				if (!source.clazz().equals(last)) {
					last = source.clazz();
					clazz = Class.forName(last);
				}
				Object obj = clazz.getField(field.getName().replaceAll(".*\\$", "")).get(null);
				FinalFieldSetter.getInstance().setStatic(field, obj instanceof Block ? Item.getItemFromBlock((Block) obj) : obj);
			}
		}
		
		int index = 0;
		Item item = null;
		for (Field field : ModItems.class.getFields()) {
			ItemTransform transform = field.getAnnotation(ItemTransform.class);
			if (transform != null) {
				if (!transform.name().equals(last)) {
					last = transform.name();
					item = (Item) ModItems.class.getField(last).get(null);
					index = 0;
				}
				FinalFieldSetter.getInstance().setStatic(field, new ItemStack(item, 1, index++));
			}
		}
		
		for (Field field : ModItems.class.getFields()) {
			Object obj = field.get(null);
			System.out.println(field.getName() + " - " + (obj instanceof Item ? ((Item) obj).getUnlocalizedName() :
				obj instanceof ItemStack ? ((ItemStack) obj).getDisplayName() : ""));
		}
	}
	
}
