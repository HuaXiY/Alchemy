package index.alchemy.block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import index.alchemy.api.Alway;
import index.alchemy.core.Constants;
import index.alchemy.core.Init;
import index.alchemy.item.AlchemyItem;
import index.alchemy.util.FinalFieldSetter;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Init(state = ModState.PREINITIALIZED)
public class AlchemyBlockLoader {
	
	public static final List<AlchemyBlock> ALL_BLOCK= new LinkedList<AlchemyBlock>();
	
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
	@Deprecated
	public static boolean replaceBlock(Block toReplace, Class<? extends Block> blockClass, ItemBlockSpecial item) {
		return replaceBlock(toReplace, blockClass, null, item, null, null);
	}
	
	// TODO
	@Deprecated
	public static boolean replaceBlock(Block toReplace, Class<? extends Block> blockClass, ResourceLocation resource,
			ItemBlockSpecial item, Class<?> tileEntityClass, String tile){
		Field modifiersField = null;
    	try{
    		modifiersField = Field.class.getDeclaredField("modifiers");
    		modifiersField.setAccessible(true);
    		for(Field field : Blocks.class.getDeclaredFields()){
        		if (Block.class.isAssignableFrom(field.getType())){
    				Block block = (Block) field.get(null);
    				if (block == toReplace){
    					ResourceLocation registryName = Block.REGISTRY.getNameForObject(block);
    					int id = Block.getIdFromBlock(block);
    					System.out.println("Replacing block - " + id + " / " + registryName);
    					
    					Block newBlock = blockClass.newInstance();
    					FMLControlledNamespacedRegistry<Block> registry = GameData.getBlockRegistry();
    					registry.putObject(registryName, newBlock);
    					
    					Tool.<IntIdentityHashBiMap>get(RegistryNamespaced.class, 0, registry).put(newBlock, id);
    					
    					Map map = Tool.<Map>get(RegistryNamespaced.class, 1, registry);
    					map.remove(toReplace, registryName);
    					map.put(newBlock, registryName);
    					
    					if (item != null) {
    						Field ItemBlockSpecial = item.getClass().getDeclaredFields()[0];
        					ItemBlockSpecial.setAccessible(true);
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
