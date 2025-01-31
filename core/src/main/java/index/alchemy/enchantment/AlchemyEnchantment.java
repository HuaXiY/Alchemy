package index.alchemy.enchantment;

import java.util.Random;

import index.alchemy.api.IRegister;
import index.project.version.annotation.Omega;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

@Omega
public class AlchemyEnchantment extends Enchantment implements IRegister {
    
    protected static final Random random = new Random();
    
    public static final EntityEquipmentSlot[]
            SLOT_ARMOR = new EntityEquipmentSlot[]{
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET},
            SLOT_HANDS = new EntityEquipmentSlot[]{
                    EntityEquipmentSlot.MAINHAND,
                    EntityEquipmentSlot.OFFHAND};
    
    protected int max_level;
    protected boolean treasure;
    
    @Override
    public int getMinLevel() {
        return 1;
    }
    
    @Override
    public int getMaxLevel() {
        return max_level;
    }
    
    @Override
    public boolean isTreasureEnchantment() {
        return treasure;
    }
    
    public Enchantment setTreasureEnchantment() {
        treasure = true;
        return this;
    }
    
    public AlchemyEnchantment(String name, Rarity rarity, EnumEnchantmentType type, int max_level, EntityEquipmentSlot... slots) {
        super(rarity, type, slots);
        this.max_level = max_level;
        //setTreasureEnchantment();
        setName(name);
        setRegistryName(name);
        register();
    }
    
}
