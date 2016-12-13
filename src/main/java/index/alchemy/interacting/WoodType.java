package index.alchemy.interacting;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import index.alchemy.api.annotation.Init;
import index.alchemy.util.Tool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import static index.alchemy.util.Tool.$;

@Init(state = ModState.INITIALIZED)
public class WoodType {
	
	public static final List<WoodType> types = Lists.newArrayList();
	public static final Map<String, BiFunction<ItemStack, String, String>> func_mapping = Maps.newHashMap();
	static {
		func_mapping.put("forestry", safe((i, s) -> $($($(i.getItem(), "getBlock"), "getWoodType", i.getMetadata()),
				s.equals("planks") ? "getPlankTexture" : "getBarkTexture")));
	}
	
	private static <A, B, C> BiFunction<A, B, C> safe(BiFunction<A, B, C> func) {
		return (a, b) -> {
			try {
				return func.apply(a, b);
			} catch(Exception e) {
				return null;
			}
		};
	}
	
	public final ItemStack log, plank;
	public final IBlockState logState, plankState;
	
	public WoodType(ItemStack log, ItemStack plank, IBlockState logState, IBlockState plankState) {
		this.log = log;
		this.plank = plank;
		this.logState = logState;
		this.plankState = plankState;
	}
	
	public static final BiFunction<String, String, String> conversion = (s, r) -> {
		WoodType type = types.stream().filter(w -> {
			ResourceLocation location = w.log.getItem().getRegistryName();
			return r.equals(location.getResourceDomain() + "_T_" + location.getResourcePath() + "_T_" + w.log.getMetadata());
		}).findFirst().orElse(null);
		if (type != null) {
			String textures[] = getTexture(type);
			return s.replace("blocks/log_oak", textures[0])
					.replace("blocks/planks_oak", textures[1]);
		}
		return s;
	};
	
	public static void init() {
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
			ItemStack output = recipe.getRecipeOutput();
			List<ItemStack> inputs = Lists.newLinkedList();
			if (output != null && output.stackSize == 4 && isOre(output, "plankWood")) {
				for (Field field : recipe.getClass().getDeclaredFields())
					if (field.getType() == ItemStack[].class)
						try {
							Object array = Tool.setAccessible(field).get(recipe);
							if (array != null && Array.getLength(array) == 1) {
								ItemStack item = (ItemStack) Array.get(array, 0);
								if (item != null && item.stackSize == 1 && isOre(item, "logWood")) {
									inputs.add(item);
									break;
								}
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							continue;
						}
				if (inputs.isEmpty())
					for (Field field : recipe.getClass().getDeclaredFields())
						if (Tool.isInstance(List.class, field.getType()))
							try {
								List list = (List) Tool.setAccessible(field).get(recipe);
								if (list != null && list.size() == 1) {
									if (list.get(0).getClass() == ItemStack.class) {
										ItemStack item = (ItemStack) list.get(0);
										if (item != null && item.stackSize == 1 && isOre(item, "logWood")) {
											inputs.add(item);
											break;
										}
									} else if (Tool.isInstance(List.class, list.get(0).getClass())) {
										List list1 = (List) list.get(0);
										for (Object obj : list1)
											if (obj.getClass() == ItemStack.class) {
												ItemStack item = (ItemStack) obj;
												if (item != null && item.stackSize == 1 && isOre(item, "logWood"))
													inputs.add(item);
											}
									}
								}
							} catch (IllegalArgumentException | IllegalAccessException e) {
								continue;
							}
			}
			if (!inputs.isEmpty() && output != null)
				for (ItemStack input : inputs) {
					Block log = Block.getBlockFromItem(input.getItem()),
							plank = Block.getBlockFromItem(output.getItem());
					if (log != null && plank != null) {
						IBlockState logState = log.getStateFromMeta(input.getMetadata()),
								plankState = plank.getStateFromMeta(output.getMetadata());
						if (logState != null && plankState != null)
							types.add(new WoodType(input, output, logState, plankState));
					}
				}
		}
	}
	
	public static boolean isOre(ItemStack item, String ore) {
		for (int id : OreDictionary.getOreIDs(item))
			if (ore.equals(OreDictionary.getOreName(id)))
				return true;
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public static String[] getTexture(WoodType type) {
		String textures[] = new String[2];
		BiFunction<ItemStack, String, String> func = func_mapping.get(type.log.getItem().getRegistryName().getResourceDomain());
		if (func != null)
			textures[0] = ObjectUtils.firstNonNull(func.apply(type.log, "log"), "blocks/log_oak");
		else
			textures[0] = getTexture(type.logState, "blocks/log_oak");
		func = func_mapping.get(type.log.getItem().getRegistryName().getResourceDomain());
		if (func != null)
			textures[1] = ObjectUtils.firstNonNull(func.apply(type.log, "planks"), "blocks/planks_oak");
		else
			textures[1] = getTexture(type.plankState, "blocks/planks_oak");
		return textures;
	}
	
	@SideOnly(Side.CLIENT)
	public static String getTexture(IBlockState state, String defaultTexture) {
		try {
			ICustomModelLoader customLoader = $("Lnet.minecraftforge.client.model.ModelLoader$VanillaLoader", "INSTANCE");
			ModelLoader loader = $(customLoader, "getLoader");
			BlockStateMapper mapper = $($(loader, "blockModelShapes"), "getBlockStateMapper");
			Map<IBlockState, ModelResourceLocation> map = mapper.getVariants(state.getBlock());
			ModelResourceLocation stateLocation = map.get(state);
			ModelBlockDefinition definition = $(loader, "getModelBlockDefinition", stateLocation);
			ResourceLocation modelLocation = definition.getVariant(stateLocation.getVariant()).getVariantList().get(0).getModelLocation();
			ModelBlock modelBlock = $(loader, "loadModel",
					new ResourceLocation(modelLocation.getResourceDomain(), "models/" + modelLocation.getResourcePath()));
			String texture = getTexture(modelBlock);
			return ObjectUtils.firstNonNull(texture, defaultTexture);
		} catch (Exception e) {
			e.printStackTrace();
			return defaultTexture;
		}
	}
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public static String getTexture(ModelBlock modelBlock) {
		if (modelBlock == null)
			return null;
		if (modelBlock.textures == null)
			return getTexture(modelBlock.parent);
		String texture = modelBlock.textures.get("all");
		if (texture != null)
			return texture;
		texture = modelBlock.textures.get("side");
		if (texture != null)
			return texture;
		texture = modelBlock.textures.get("particle");
		if (texture != null)
			return texture;
		for (Entry<String, String> entry : modelBlock.textures.entrySet())
			if (!entry.getKey().equals("end") && !entry.getValue().contains("_top") && !entry.getValue().contains("top_"))
				return entry.getValue();
		return getTexture(modelBlock.parent);
	}

}
