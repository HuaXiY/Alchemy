package index.alchemy.core;

import java.util.concurrent.Callable;

import index.alchemy.api.Alway;
import index.alchemy.capability.AlchemyCapability;
import index.alchemy.client.AlchemyColorLoader;
import index.alchemy.client.IColorBlock;
import index.alchemy.client.IColorItem;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.INetworkMessage;
import index.alchemy.world.IGenerator;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl;
import net.minecraftforge.oredict.OreDictionary;

public class AlchemyInitHook {
	
	public static class InitHookEvent extends Event {
		
		public final Object init;
		
		public InitHookEvent(Object obj) {
			init = obj;
		}
		
	}
	
	public static void push_event(Object obj) {
		MinecraftForge.EVENT_BUS.post(new InitHookEvent(obj));
	}
	
	public static void init_impl(Impl impl) {
		init(impl);
	}
	
	public static <I extends Item & IColorItem, B extends Block & IColorBlock,
				   R extends Item & IResourceLocation, C> void init(Object obj) {
		
		if (obj instanceof Impl)
			GameRegistry.register((Impl) obj);
		
		if (obj instanceof Block)
			init(new ItemBlock((Block) obj).setRegistryName(((Block) obj).getRegistryName()));
		
		if (obj instanceof ITileEntity)
			GameRegistry.registerTileEntity(((ITileEntity) obj).getTileEntityClass(), ((ITileEntity) obj).getTileEntityName());
		
		if (obj instanceof IOreDictionary)
			OreDictionary.registerOre(((IOreDictionary) obj).getNameInOreDictionary(), ((IOreDictionary) obj).getItemStackInOreDictionary());
		
		if (obj instanceof IBrewingRecipe)
			BrewingRecipeRegistry.addRecipe((IBrewingRecipe) obj);
		
		if (obj instanceof IGenerator)
			GameRegistry.registerWorldGenerator((IGenerator) obj, ((IGenerator) obj).getWeight());
		
		if (obj instanceof IPlayerTickable)
			AlchemyEventSystem.registerPlayerTickable((IPlayerTickable) obj);
		
		if (obj instanceof IEventHandle)
			AlchemyEventSystem.registerEventHandle((IEventHandle) obj);
		
		if (obj instanceof INetworkMessage)
			AlchemyNetworkHandler.registerMessage((INetworkMessage) obj);
		
		if (obj instanceof AlchemyCapability)
			CapabilityManager.INSTANCE.register((Class<C>) obj.getClass(), (IStorage<C>) obj, (Callable<C>) obj);
		
		if (Alway.isClient()) {
			
			if (obj instanceof Item) {
				
				if (obj instanceof IColorItem)
					AlchemyColorLoader.addItemColor((I) obj);
				
				if (obj instanceof IResourceLocation)
					ModelLoader.setCustomModelResourceLocation((Item) obj, 0, new ModelResourceLocation(
							((IResourceLocation) obj).getResourceLocation(), "inventory"));
				
			}
			
			if (obj instanceof Block) {
				
				if (obj instanceof IColorBlock)
					AlchemyColorLoader.addBlockColor((B) obj);
				
			}
			
			if (obj instanceof KeyBinding)
				ClientRegistry.registerKeyBinding((KeyBinding) obj);
			
		}
		
		push_event(obj);
		
	}
	
}