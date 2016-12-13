package index.alchemy.interacting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Source;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.FinalFieldSetter;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Init(state = ModState.PREINITIALIZED, index = -1)
public class ModBlocks {
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface BlockTransform {
		
		public String value() default "";

	}
	
	@Deprecated
	@Source("biomesoplenty.api.block.BOPBlocks")
	public static final Block
			bop$gem_block = null,
			bop$mushroom = null,
			bop$flower_0 = null,
			bop$flower_1 = null;
	
	@BlockTransform("bop$gem_block")
	public static final IBlockState
			bop$gem_amethyst = null,
			bop$gem_ruby = null,
			bop$gem_peridot = null,
			bop$gem_topaz = null,
			bop$gem_tanzanite = null,
			bop$gem_malachite = null,
			bop$gem_sapphire = null,
			bop$gem_amber = null;
	
	@BlockTransform( "bop$mushroom")
	public static final IBlockState
			bop$mushroom_toadstool = null,
			bop$mushroom_portobello = null,
			bop$mushroom_blue_milk_cap = null,
			bop$mushroom_glowshroom = null,
			bop$mushroom_flat_mushroom = null,
			bop$mushroom_shadow_shroom = null;
	
	@BlockTransform( "bop$flower_0")
	public static final IBlockState
			bop$flower_clover = null,
			bop$flower_swampflower = null,
			bop$flower_deathbloom = null,
			bop$flower_glowflower = null,
			bop$flower_blue_hydrangea = null,
			bop$flower_orange_cosmos = null,
			bop$flower_pink_daffodil = null,
			bop$flower_wildflower = null,
			bop$flower_violet = null,
			bop$flower_white_anemone = null,
			bop$flower_enderlotus = null,
			bop$flower_bromeliad = null,
			bop$flower_wilted_lily = null,
			bop$flower_pink_hibiscus = null,
			bop$flower_lily_of_the_valley = null,
			bop$flower_burning_blossom = null;
	
	@BlockTransform("bop$flower_1")
	public static final IBlockState
			bop$flower_lavender = null,
			bop$flower_goldenrod = null,
			bop$flower_bluebells = null,
			bop$flower_miners_delight = null,
			bop$flower_icy_iris = null,
			bop$flower_rose = null;
	
	
	public static void init() throws Exception {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		String last = null;
		for (Field field : ModBlocks.class.getFields()) {
			Source source = field.getAnnotation(Source.class);
			if (source != null) {
				Class<?> clazz = Tool.forName(source.value(), true);
				if (clazz != null) {
					Object obj = clazz.getField(field.getName().replaceAll(".*\\$", "")).get(null);
					FinalFieldSetter.getInstance().setStatic(field, obj);
				}
			}
		}
		
		int index = 0;
		Block block = null;
		for (Field field : ModBlocks.class.getFields()) {
			BlockTransform transform = field.getAnnotation(BlockTransform.class);
			if (transform != null) {
				if (!transform.value().equals(last)) {
					last = transform.value();
					block = (Block) ModBlocks.class.getField(last).get(null);
					index = 0;
				}
				FinalFieldSetter.getInstance().setStatic(field, block.getStateFromMeta(index++));
			}
		}
		
		for (Field field : ModBlocks.class.getFields()) {
			Object obj = field.get(null);
			AlchemyModLoader.info(field.getType(), field.getName() + " - " + (obj instanceof Block ? 
					((Block) obj).getLocalizedName() : obj instanceof IBlockState ? new ItemStack(((IBlockState) obj).getBlock(),
					1, ((IBlockState) obj).getBlock().getMetaFromState((IBlockState) obj)).getDisplayName() : ""));
		}
	}

}
