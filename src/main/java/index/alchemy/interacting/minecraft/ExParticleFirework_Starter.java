package index.alchemy.interacting.minecraft;


import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Patch;
import index.alchemy.util.FireworkHelper;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

@Hook.Provider
@Patch("net.minecraft.client.particle.ParticleFirework$Starter")
public class ExParticleFirework_Starter extends ParticleFirework.Starter {
	
	@Patch.Exception
	@Hook("net.minecraft.entity.item.EntityFireworkRocket#func_70103_a")
	public static Hook.Result handleStatusUpdate(EntityFireworkRocket rocket, byte id) {
		if (id == 17 && rocket.worldObj.isRemote) {
            ItemStack item = rocket.getDataManager().get(EntityFireworkRocket.FIREWORK_ITEM).orNull();
            NBTTagCompound nbt = null;

            if (item != null && item.hasTagCompound()) {
            	nbt = item.getTagCompound().getCompoundTag("Fireworks");
            	if (item.getTagCompound().hasKey("display", NBT.TAG_COMPOUND))
            		nbt.setTag("display", item.getTagCompound().getTag("display"));
            }

            rocket.worldObj.makeFireworks(rocket.posX, rocket.posY, rocket.posZ, rocket.motionX, rocket.motionY, rocket.motionZ, nbt);
        }
		return Hook.Result.NULL;
	}
	
	public NBTTagCompound itemNbt;
	
	public ExParticleFirework_Starter(World world, double x, double y, double z, double mx, double my, double mz,
			ParticleManager manager, NBTTagCompound nbt) {
		super(world, x, y, z, mx, my, mz, manager, nbt);
		itemNbt = nbt;
	}

	@Override
	public void onUpdate() {
		if (fireworkAge == 0 && fireworkExplosions != null) {
            boolean far = isFarFromCamera();
            boolean large = false;
            
            if (fireworkExplosions.tagCount() >= 3)
            	large = true;
            else {
                for (int i = 0; i < fireworkExplosions.tagCount(); ++i) {
                    NBTTagCompound nbt = fireworkExplosions.getCompoundTagAt(i);
                    if (nbt.getByte("Type") == 1) {
                    	large = true;
                        break;
                    }
                }
            }

            SoundEvent sound = far ? large ?
            		SoundEvents.ENTITY_FIREWORK_LARGE_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_BLAST_FAR :
            		large ? SoundEvents.ENTITY_FIREWORK_LARGE_BLAST : SoundEvents.ENTITY_FIREWORK_BLAST;

            worldObj.playSound(posX, posY, posZ, sound, SoundCategory.AMBIENT,
            		20.0F, 0.95F + rand.nextFloat() * 0.1F, true);
        }

        if (fireworkAge % 2 == 0 && fireworkExplosions != null &&
        		fireworkAge / 2 < fireworkExplosions.tagCount()) {
            int k = fireworkAge / 2;
            NBTTagCompound nbt = fireworkExplosions.getCompoundTagAt(k);
            int type = nbt.getByte("Type");
            boolean trail = nbt.getBoolean("Trail");
            boolean flicker = nbt.getBoolean("Flicker");
            int[] colors = nbt.getIntArray("Colors");
            int[] fadeColors = nbt.getIntArray("FadeColors");

            if (colors.length == 0)
                colors = new int[] {ItemDye.DYE_COLORS[0]};
            if (itemNbt.hasKey("display", NBT.TAG_COMPOUND) && itemNbt.getCompoundTag("display").hasKey("Name", NBT.TAG_STRING)) {
            	String name = itemNbt.getCompoundTag("display").getString("Name");
            	FireworkHelper.createShaped(this, name, 0.5D, colors, fadeColors, trail, flicker);
            } else
            	switch (type) {
					case 1:
						createBall(0.5D, 4, colors, fadeColors, trail, flicker);
						break;
					case 2:
						createShaped(0.5D, new double[][] {
							{0.0D, 1.0D},
							{0.3455D, 0.309D},
							{0.9511D, 0.309D},
							{0.3795918367346939D, -0.12653061224489795D},
							{0.6122448979591837D, -0.8040816326530612D},
							{0.0D, -0.35918367346938773D}
						}, colors, fadeColors, trail, flicker, false);
						break;
					case 3:
						createShaped(0.5D, new double[][] {
							{0.0D, 0.2D},
							{0.2D, 0.2D},
							{0.2D, 0.6D},
							{0.6D, 0.6D},
							{0.6D, 0.2D},
							{0.2D, 0.2D},
							{0.2D, 0.0D},
							{0.4D, 0.0D},
							{0.4D, -0.6D},
							{0.2D, -0.6D},
							{0.2D, -0.4D},
							{0.0D, -0.4D}
						}, colors, fadeColors, trail, flicker, true);
						break;
					case 4:
						createBurst(colors, fadeColors, trail, flicker);
						break;
					default:
						createBall(0.25D, 2, colors, fadeColors, trail, flicker);
						break;
				}
            
            int rgb = colors[0];
            float r = (float)((rgb & 16711680) >> 16) / 255.0F;
            float g = (float)((rgb & 65280) >> 8) / 255.0F;
            float b = (float)((rgb & 255) >> 0) / 255.0F;
            ParticleFirework.Overlay overlay = new ParticleFirework.Overlay(worldObj, posX, posY, posZ);
            overlay.setRBGColorF(r, b, b);
            theEffectRenderer.addEffect(overlay);
        }

        ++fireworkAge;

        if (fireworkAge > particleMaxAge) {
            if (twinkle) {
                boolean far = isFarFromCamera();
                SoundEvent soundevent = far ? SoundEvents.ENTITY_FIREWORK_TWINKLE_FAR : SoundEvents.ENTITY_FIREWORK_TWINKLE;
                worldObj.playSound(posX, posY, posZ, soundevent, SoundCategory.AMBIENT,
                		20.0F, 0.9F + rand.nextFloat() * 0.15F, true);
            }

            setExpired();
        }
	}

}
