package index.alchemy.block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import index.alchemy.annotation.Change;
import index.alchemy.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.util.FinalFieldSetter;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyBlockLoader {
	
	public static final Block ice_temp = new BlockIceTemp();
	
	public static void init() {
		replaceBlock();
	}
	
	private static void replaceBlock() {
		/*replaceBlock(Blocks.brewing_stand, BlockBrewingStandProxy.class, null,
				(ItemBlockSpecial) Items.brewing_stand, TileEntityBrewingStandProxy.class, "Cauldron");*/
	}
	
	// TODO
	// !!!!> Only in the version 1.9 working <!!!!
	// This is replace block in the Minecraft.
	// Not guaranteed to work in another version, Field name and
	// position will change with the version.
	@Change
	@Deprecated
	public static boolean replaceBlock(Block toReplace, Class<? extends Block> blockClass, ItemBlockSpecial item) {
		return replaceBlock(toReplace, blockClass, null, item, null, null);
	}
	
	// TODO
	@Change
	@Deprecated
	public static boolean replaceBlock(Block toReplace, Class<? extends Block> blockClass, ResourceLocation resource,
			ItemBlockSpecial item, Class<?> tileEntityClass, String tile){
		Field modifiersField = null;
    	try{
    		modifiersField = Tool.setAccessible(Field.class.getDeclaredField("modifiers"));
    		for(Field field : Blocks.class.getDeclaredFields()){
        		if (Block.class.isAssignableFrom(field.getType())){
    				Block block = (Block) field.get(null);
    				if (block == toReplace){
    					ResourceLocation registryName = Block.REGISTRY.getNameForObject(block);
    					int id = Block.getIdFromBlock(block);
    					AlchemyModLoader.logger.info("Replacing block - " + id + " / " + registryName);
    					
    					Block newBlock = blockClass.newInstance();
    					FMLControlledNamespacedRegistry<Block> registry = GameData.getBlockRegistry();
    					registry.putObject(registryName, newBlock);
    					
    					Tool.<IntIdentityHashBiMap>get(RegistryNamespaced.class, 0, registry).put(newBlock, id);
    					
    					Map map = Tool.<Map>get(RegistryNamespaced.class, 1, registry);
    					map.remove(toReplace, registryName);
    					map.put(newBlock, registryName);
    					
    					if (item != null) {
    						Field ItemBlockSpecial = Tool.setAccessible(item.getClass().getDeclaredFields()[0]);
        					modifiersField.setInt(ItemBlockSpecial, modifiersField.getInt(ItemBlockSpecial) & ~Modifier.FINAL);
        					ItemBlockSpecial.set(item, newBlock);
    					}
    					
    					if (tile != null && tileEntityClass != null) {
	    					Tool.<Map>get(TileEntity.class, 1).put(tile, tileEntityClass);
	    					Tool.<Map>get(TileEntity.class, 2).put(tileEntityClass, tile);
    					}
    					
    					newBlock.setRegistryName(resource == null ? registryName : resource);
    					FinalFieldSetter.getInstance().setStatic(field, newBlock);
    				}
        		}
        	}
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    	return true;
	}
}
