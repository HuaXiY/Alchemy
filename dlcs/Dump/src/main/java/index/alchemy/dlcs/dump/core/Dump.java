package index.alchemy.dlcs.dump.core;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.core.AlchemyModLoader;
import index.project.version.annotation.Omega;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

import com.google.common.collect.Maps;

import static index.alchemy.dlcs.dump.core.Dump.*;

@Omega
@Init(state = ModState.AVAILABLE)
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "*")
public class Dump {
    
    public static final String
            DLC_ID = "dump",
            DLC_NAME = "Dump",
            DLC_VERSION = "0.0.1-dev";
    
    public static final File dump_dir = new File(AlchemyModLoader.mc_dir, "dump");
    
    public static void init() throws IOException {
        if (!dump_dir.isDirectory())
            dump_dir.mkdirs();
        dumpFile(Item.REGISTRY, "item");
        dumpFile(Block.REGISTRY, "block");
        dumpFile(Potion.REGISTRY, "potion");
        dumpFile(Biome.REGISTRY, "biome");
        dumpFile(Enchantment.REGISTRY, "enchantment");
        dumpFile(PotionType.REGISTRY, "potiontype");
        dumpFile(SoundEvent.REGISTRY, "soundevent");
    }
    
    public static Map<Impl, Integer> dumpId(RegistryNamespaced<ResourceLocation, Impl> namespaced) {
        return StreamSupport.stream(namespaced.spliterator(), false).collect(Maps::newLinkedHashMap,
                (map, impl) -> map.put(impl, namespaced.getIDForObject(impl)), Map::putAll);
    }
    
    public static void dumpFile(RegistryNamespaced namespaced, String name) throws IOException {
        File output = new File(dump_dir, name + ".dump");
        Map<Impl, Integer> map = dumpId(namespaced);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
        for (Entry<Impl, Integer> entry : map.entrySet()) {
            writer.write(entry.getValue() + " - " + entry.getKey().getRegistryName());
            writer.newLine();
        }
        writer.close();
    }
    
}
