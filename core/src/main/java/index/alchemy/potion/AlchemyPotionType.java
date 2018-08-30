package index.alchemy.potion;

import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Patch;
import index.project.version.annotation.Omega;
import net.minecraft.potion.PotionType;

@Omega
@Patch("net.minecraft.potion.PotionType")
public class AlchemyPotionType extends PotionType implements IRegister { }
