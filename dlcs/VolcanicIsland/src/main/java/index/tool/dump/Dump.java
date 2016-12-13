package index.tool.dump;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl;

@Mod(modid = "dump", name = "dump", version = "1.0.0")
public class Dump {
	
	@EventHandler
	public void onFMLPostInitialization(FMLPostInitializationEvent event) throws IOException {
		File outputDir = new File("dump");
		if (!outputDir.isDirectory())
			outputDir.mkdirs();
		dumpFile(Item.REGISTRY, "item");
		dumpFile(Block.REGISTRY, "block");
		dumpFile(Potion.REGISTRY, "potion");
		dumpFile(Biome.REGISTRY, "biome");
		dumpFile(Enchantment.REGISTRY, "enchantment");
		dumpFile(PotionType.REGISTRY, "potiontype");
		dumpFile(SoundEvent.REGISTRY, "soundevent");
	}
	
	public static Map<Impl, Integer> dumpId(RegistryNamespaced<ResourceLocation, Impl> namespaced) {
		Map<Impl, Integer> map = new LinkedHashMap<Impl, Integer>();
		int index = 0;
		for (Impl k : namespaced)
			map.put(k, namespaced.getIDForObject(k));
		return map;
	}
	
	public static void dumpFile(RegistryNamespaced namespaced, String name) throws IOException {
		File output = new File("dump/" + name + ".dump");
		Map<Impl, Integer> map = dumpId(namespaced);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		for (Entry<Impl, Integer> entry : map.entrySet()) {
			writer.write(entry.getValue() + " - " + entry.getKey().getRegistryName());
			writer.newLine();
		}
		writer.close();
	}

}
