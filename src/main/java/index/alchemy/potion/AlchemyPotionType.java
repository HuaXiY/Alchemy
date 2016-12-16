package index.alchemy.potion;

import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Proxy;
import index.project.version.annotation.Omega;
import net.minecraft.potion.PotionType;

@Omega
@Proxy("net.minecraft.potion.PotionType")
public class AlchemyPotionType extends PotionType implements IRegister { }
