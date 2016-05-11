package index.alchemy.interacting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import index.alchemy.core.Init;
import index.alchemy.util.FinalFieldSetter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED)
public class ModItems {
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ItemTransform {
		
		public String value() default "";

	}
	
	//  Biomes O' Plenty

	@Source(BOPItems.class)
	public static final Item 
			bop$gem = null;
	
	@ItemTransform("bop$gem")
	public static final ItemStack
			bop$gem_amethyst = null,							//  末影紫晶 ---- 空间
			bop$gem_ruby = null,								//  红宝石 ---- 生命
			bop$gem_peridot = null,							//  橄榄石 ---- 坚韧
			bop$gem_topaz = null,								//  黄玉 ---- 守护
			bop$gem_tanzanite = null,							//  坦桑石 ---- 同化
			bop$gem_malachite = null,						//  孔雀石 ---- 祝福
			bop$gem_sapphire = null,							//  蓝宝石  ---- 净化
			bop$gem_amber = null;								//  琥珀  ---- 时间
	
	@Source(BOPBlocks.class)
	public static final  Item 
			bop$flower_0 = null,
			bop$flower_1 = null;
	
	@ItemTransform( "bop$flower_0")
	public static final ItemStack
			bop$flower_clover = null,							//  苜蓿 ---- 混沌
			bop$flower_swampflower = null,				//  沼泽花 ---- 黑暗
			bop$flower_deathbloom = null,				//  死亡花 ---- 堕化
			bop$flower_glowflower = null,				//  闪光花 ---- 光明
			bop$flower_blue_hydrangea = null,			//  蓝绣球花 ---- 亲和
			bop$flower_orange_cosmos = null,			//  黄波斯菊 ---- 奇迹
			bop$flower_pink_daffodil = null,				//  粉水仙 ---- 净化
			bop$flower_wildflower = null,					//  野花 ---- 时间
			bop$flower_violet = null,							//  紫罗兰  ---- 牺牲
			bop$flower_white_anemone = null,			//  银莲花 ---- 包容
			bop$flower_enderlotus = null,					//  末影莲花 ----空间
			bop$flower_bromeliad = null,					//  凤梨 --- 破瘴
			bop$flower_wilted_lily = null,					//  凋零百合 ---- 绝望
			bop$flower_pink_hibiscus = null,				//  粉木槿 ---- 真理
			bop$flower_lily_of_the_valley = null,		//  谷百合 ---- 祝福
			bop$flower_burning_blossom = null;		//  火焰花 ---- 狂暴
	
	@ItemTransform("bop$flower_1")
	public static final ItemStack
			bop$flower_lavender = null,						//  熏衣草 ---- 和平
			bop$flower_goldenrod = null,					//  秋麒麟草 ---- 永恒
			bop$flower_bluebells = null,						//  蓝铃花 ---- 活性
			bop$flower_miners_delight = null,			//  乐矿花 ---- 幸运
			bop$flower_icy_iris = null,						//  冰虹膜花 ----神性
			bop$flower_rose = null;								//  玫瑰 ---- 爱情
	
	public static void init() throws Exception {
		String last = null;
		for (Field field : ModItems.class.getFields()) {
			Source source = field.getAnnotation(Source.class);
			if (source != null && source.value() != null) {
				Object obj = source.value().getField(field.getName().replaceAll(".*\\$", "")).get(null);
				FinalFieldSetter.getInstance().setStatic(field, obj instanceof Block ? Item.getItemFromBlock((Block) obj) : obj);
			}
		}
		
		int index = 0;
		Item item = null;
		for (Field field : ModItems.class.getFields()) {
			ItemTransform transform = field.getAnnotation(ItemTransform.class);
			if (transform != null) {
				if (!transform.value().equals(last)) {
					last = transform.value();
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
