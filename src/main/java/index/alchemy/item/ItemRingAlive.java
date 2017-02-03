package index.alchemy.item;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.ICoolDown;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IFXUpdate;
import index.alchemy.api.IFieldAccess;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.ILocationProvider;
import index.alchemy.api.INetworkMessage;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.annotation.FX;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.api.annotation.SideOnlyLambda;
import index.alchemy.client.fx.AlchemyFX;
import index.alchemy.client.fx.FXWisp;
import index.alchemy.client.fx.update.FXARGBUpdate;
import index.alchemy.client.fx.update.FXAgeUpdate;
import index.alchemy.client.fx.update.FXMotionUpdate;
import index.alchemy.client.fx.update.FXPosClearUpdate;
import index.alchemy.client.fx.update.FXPosSourceUpdate;
import index.alchemy.client.fx.update.FXPosUpdate;
import index.alchemy.client.fx.update.FXScaleUpdate;
import index.alchemy.client.fx.update.FXTowUpdate;
import index.alchemy.client.fx.update.FXTriggerUpdate;
import index.alchemy.client.fx.update.FXUpdateHelper;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.entity.AlchemyDamageSourceLoader;
import index.alchemy.entity.ai.EntityAIFindEntityNearestHelper;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.item.ItemRingAlive.MessageAlivePower;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Always;
import index.alchemy.util.Counter;
import index.alchemy.util.NBTHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static java.lang.Math.*;

@Beta
@FX.UpdateProvider
public class ItemRingAlive extends AlchemyItemRing implements IInputHandle, IEventHandle, ICoolDown,
		INetworkMessage.Server<MessageAlivePower> {
	
	public static final int USE_CD = 20 * 15, RADIUS = 16, EFFECT_RANDE = 8;
	public static final float MAX_POWER = 40, DECREASE_COEFFICIENT = 0.01F;
	public static final String NBT_KEY_CD = "cd_ring_alive", NBT_KEY_ALIVE_POWER = "alive_power",
			FX_KEY_FOLLOW = "ring_alive_follow", FX_KEY_RIST = "ring_alive_rise";
	
	public static final ItemRingAlive type = null;
	
	public static final IFieldAccess.Float<ItemStack> alive_power = new IFieldAccess.Float<ItemStack>() {

		@Override
		public float get(ItemStack item) {
			return item.getTagCompound() == null ? 0 : item.getTagCompound().getFloat(NBT_KEY_ALIVE_POWER);
		}

		@Override
		public void set(ItemStack item, float power) {
			NBTHelper.getOrSetNBT(item).setFloat(NBT_KEY_ALIVE_POWER, power);
		}
		
	};
	
	@FX.UpdateMethod(FX_KEY_FOLLOW)
	public static List<IFXUpdate> getFXUpdateFollow(int[] args) {
		List<IFXUpdate> result = Lists.newLinkedList();
		int i = 1,
			entity_id = Tool.getSafe(args, i++, -1);
		if (entity_id != -1) {
			Entity entity = Always.findEntityFormClientWorld(entity_id);
			if (entity != null) {
				ILocationProvider location = Always.generateLocationProvider(entity, entity.height / 2);
				result.add(new FXAgeUpdate(20 * 6));
				result.add(new FXARGBUpdate(0xFF6ACCA9));
				result.add(new FXTowUpdate(location, .25));
				result.add(new FXTriggerUpdate((@SideOnlyLambda(Side.CLIENT) Consumer<AlchemyFX>) AlchemyFX::setExpired,
						(@SideOnlyLambda(Side.CLIENT) Predicate<AlchemyFX>) fx -> fx.getDistanceSq(location.getLocation()) < .1));
			}
		}
		return result;
	}
	
	@FX.UpdateMethod(FX_KEY_RIST)
	public static List<IFXUpdate> getFXUpdateRist(int[] args) {
		List<IFXUpdate> result = Lists.newLinkedList();
		int i = 1,
			entity_id = Tool.getSafe(args, i++, -1),
			scale = 140 + random.nextInt(60);
		if (entity_id != -1) {
			Entity entity = Always.findEntityFormClientWorld(entity_id);
			if (entity != null) {
				final int max_age = 20 * 4;
				result.add(new FXAgeUpdate(max_age));
				result.add(new FXARGBUpdate(0xFF6ACCA9));
				result.add(new FXScaleUpdate(new StdCycle().setLenght(max_age).setMin(scale / 1000F).setMax(scale / 100F)));
				result.add(new FXPosClearUpdate());
				result.add(new FXPosUpdate(0, 0, 1.2));
				result.add(new FXPosSourceUpdate(entity));
				result.add(new FXMotionUpdate(
						new StdCycle().setLoop(true).setRotation(true).setLenght(max_age / 3).setMin(-.2F).setMax(.2F),
						new StdCycle().setMax(.025F),
						new StdCycle().setLoop(true).setRotation(true).setLenght(max_age / 3).setNow(max_age / 6).setMin(-.2F).setMax(.2F)));
			}
		}
		return result;
	}
	
	public void onAlivePowerEffectiveLiving(EntityLivingBase living, float amount) {
		if (living.isEntityUndead())
			living.attackEntityFrom(AlchemyDamageSourceLoader.alive_power, amount * 2);
		else
			living.heal(amount);
	}
	
	public void useAlivePower(EntityLivingBase target) {
		ItemStack item = getFormLiving(target);
		if (item != null) {
			float power = alive_power.get(item) / 2;
			for (EntityLivingBase living : target.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
					AABBHelper.getAABBFromEntity(target, EFFECT_RANDE))) {
				onAlivePowerEffectiveLiving(living, power);
				List<Double6IntArrayPackage> d6iaps = Lists.newLinkedList();
				int update[] = FXUpdateHelper.getIntArrayByArgs(FX_KEY_RIST, living.getEntityId());
				d6iaps.add(new Double6IntArrayPackage(
						living.posX + living.width / 2,
						living.posY,
						living.posZ + living.width / 2, 0, 0, 0, update));
				IPhaseRunnable runnable = p -> AlchemyNetworkHandler.spawnParticle(FXWisp.Info.type,
						AABBHelper.getAABBFromEntity(living, AlchemyNetworkHandler.getParticleRange()), living.worldObj, d6iaps);
				runnable.run(AlchemyEventSystem.getPhase());
				AlchemyEventSystem.addCounterRunnable(runnable, new Counter(20), 5);
			}
			alive_power.set(item, power);
		}
	}
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (Always.isServer() && living.ticksExisted % 5 == 0 && living.getHealth() < living.getMaxHealth() && alive_power.get(item) >= 1) {
			living.heal(1);
			alive_power.set(item, alive_power.get(item) - 1);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event) {
		if (Always.isServer()) {
			EntityLivingBase living = event.getEntityLiving();
			if (!living.isEntityUndead()) {
				EntityLivingBase target = EntityAIFindEntityNearestHelper.findNearest(living,
						EntityLivingBase.class, AABBHelper.getAABBFromEntity(living, RADIUS), this::isEquipmented);
				float power = living.getMaxHealth() / 5;
				if (target != null && power > 0) {
					ItemStack alive = getFormLiving(target);
					alive_power.set(alive, min(alive_power.get(alive) + power, MAX_POWER));
					List<Double6IntArrayPackage> d6iaps = Lists.newLinkedList();
					int update[] = FXUpdateHelper.getIntArrayByArgs(FX_KEY_FOLLOW, target.getEntityId());
					for (int i = 0; i < 4; i++)
						d6iaps.add(new Double6IntArrayPackage(
								living.posX + living.rand.nextGaussian(),
								living.posY + living.rand.nextFloat() * .5,
								living.posZ + living.rand.nextGaussian(), 0, 0, 0, update));
					AlchemyNetworkHandler.spawnParticle(FXWisp.Info.type,
							AABBHelper.getAABBFromEntity(living, AlchemyNetworkHandler.getParticleRange()), living.worldObj, d6iaps);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHurt(LivingHurtEvent event) {
		if (event.getSource() instanceof EntityDamageSource) {
			EntityLivingBase living = event.getEntityLiving();
			EntityDamageSource source = (EntityDamageSource) event.getSource();
			if (source.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) source.getEntity()).isEntityUndead()) {
				ItemStack alive = getFormLiving(living);
				if (alive != null)
					event.setAmount(event.getAmount() * (1 - alive_power.get(alive) * DECREASE_COEFFICIENT));
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		return new KeyBinding[] {
				key_binding_1,
				key_binding_2
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent({ KEY_RING_1, KEY_RING_2 })
	public void onKeyTimeLeapPressed(KeyBinding binding) {
		if (shouldHandleInput(binding))
			if (isCDOver()) {
				AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageAlivePower());
				restartCD();
			} else
				HUDManager.setSnake(this);
	}
	
	public static class MessageAlivePower implements IMessage, IMessageHandler<MessageAlivePower, IMessage> {
		
		@Override
		public void fromBytes(ByteBuf buf) { }

		@Override
		public void toBytes(ByteBuf buf) { }
		
		@Override
		public IMessage onMessage(MessageAlivePower message, MessageContext ctx) {
			AlchemyEventSystem.addDelayedRunnable(p -> type.useAlivePower(ctx.getServerHandler().playerEntity), 0);
			return null;
		}
		
	}
	
	@Override
	public Class<MessageAlivePower> getServerMessageClass() {
		return MessageAlivePower.class;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(String.format("%.2f", alive_power.get(stack)) + " / " + String.format("%.2f", MAX_POWER));
	}
	
	@Override
	public int getMaxCD() {
		return USE_CD;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getResidualCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		return isEquipmented(player) ? 
				max(0, getMaxCD() - (player.ticksExisted - player.getEntityData().getInteger(NBT_KEY_CD))) : -1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isCDOver() {
		return getResidualCD() == 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setResidualCD(int cd) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted - (getMaxCD() - cd));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void restartCD() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		player.getEntityData().setInteger(NBT_KEY_CD, player.ticksExisted);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderID() {
		return 6;
	}
	
	public ItemRingAlive() {
		super("ring_alive", 0x6ACCA9);
	}

}