package index.alchemy.potion;

import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Proxy;
import net.minecraft.potion.PotionType;

@Proxy("net.minecraft.potion.PotionType")
public class AlchemyPotionType extends PotionType implements IRegister { }
