package index.alchemy.core;

import index.alchemy.api.*;
import index.alchemy.api.annotation.Change;
import index.alchemy.client.color.AlchemyColorLoader;
import index.alchemy.client.render.HUDManager;
import index.alchemy.item.AlchemyItemBlock;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.SideHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

@Omega
public class AlchemyInitHook {
    
    @Omega
    public static class InitHookEvent extends Event {
        
        public final Object init;
        
        public InitHookEvent(Object obj) {
            init = obj;
        }
        
    }
    
    public static void push_event(Object obj) {
        MinecraftForge.EVENT_BUS.post(new InitHookEvent(obj));
    }
    
    @Change("1.9.4")
    public static <T extends IForgeRegistryEntry<T>> void init_impl(T impl) {
        
        Tool.checkNull(impl);
        AlchemyModLoader.checkState();
        
        GameRegistry.register(impl);
        
        if (impl instanceof Block)
            init(new AlchemyItemBlock((Block) impl).setRegistryName(impl.getRegistryName()));
        
    }
    
    @Change("1.9.4")
    @SuppressWarnings("unchecked")
    public static <T extends IForgeRegistryEntry<T>, I extends Item & IColorItem, B extends Block & IColorBlock,
            R extends Item & IResourceLocation, C> void init(Object obj) {
        
        Tool.checkNull(obj);
        AlchemyModLoader.checkState();
        
        if (obj instanceof Impl && obj instanceof IRegister && ((IRegister) obj).shouldRegisterToGame())
            init_impl((T) obj);
        
        if (obj instanceof ITileEntity)
            AlchemyEventSystem.registerTileEntity((ITileEntity) obj);
        
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
            AlchemyNetworkHandler.registerMessage((INetworkMessage<?>) obj);
        
        if (obj instanceof IGuiHandle)
            AlchemyEventSystem.registerGuiHandle((IGuiHandle) obj);
        
        if (obj instanceof ICapability)
            CapabilityManager.INSTANCE.register((Class<C>) obj.getClass(), (ICapability<C>) obj, (ICapability<C>) obj);
        
        if (obj instanceof IAlchemyBiome) {
            IAlchemyBiome biome = (IAlchemyBiome) obj;
            
            if (biome.canGenerateVillages())
                BiomeManager.addVillageBiome((Biome) biome, true);
        }
        
        if (SideHelper.isClient()) {
            
            if (obj instanceof Item) {
                
                if (obj instanceof IColorItem)
                    AlchemyColorLoader.addItemColor((I) obj);
                
                if (obj instanceof IItemMeshProvider && ((IItemMeshProvider) obj).getItemMesh() != null) {
                    ModelLoader.setCustomMeshDefinition((Item) obj, ((IItemMeshProvider) obj).getItemMesh());
                    ModelLoader.registerItemVariants((Item) obj, ((IItemMeshProvider) obj).getItemVariants());
                }
                else if (obj instanceof IResourceLocation)
                    ModelLoader.setCustomModelResourceLocation((Item) obj, 0, new ModelResourceLocation(
                            ((IResourceLocation) obj).getResourceLocation(), "inventory"));
                
            }
            
            if (obj instanceof Block) {
                
                if (obj instanceof IColorBlock)
                    AlchemyColorLoader.addBlockColor((B) obj);
                
            }
            
            if (obj instanceof IInputHandle)
                AlchemyEventSystem.registerInputHandle((IInputHandle) obj);
            
            if (obj instanceof ICoolDown)
                HUDManager.registerCoolDown((ICoolDown) obj);
            
        }
        
        Tool.setInstance(obj.getClass(), obj);
        
        push_event(obj);
        
    }
    
}