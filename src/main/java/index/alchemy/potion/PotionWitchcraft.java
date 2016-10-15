package index.alchemy.potion;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.client.render.RenderHelper;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.entity.AlchemyEntityManager;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.potion.PotionWitchcraft.MessageWitchcraftUpdate;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static org.lwjgl.opengl.GL11.*;

public class PotionWitchcraft extends AlchemyPotion implements IEventHandle, INetworkMessage.Client<MessageWitchcraftUpdate> {
	
	public static final String NBT_KEY_RENDER = "wc_render";
	
	private EntityLivingBase last;
	
	@Override
	public void performEffect(EntityLivingBase base, int level) {
		if (base instanceof EntityLiving) {
			EntityLiving living = (EntityLiving) base;
			living.tasks.tickCount = 1;
			living.targetTasks.tickCount = 1;
			if (living instanceof EntityCreeper)
				((EntityCreeper) living).setCreeperState(-1);
		}
	}
	
	@Override
	public void applyAttributesModifiersToEntity(EntityLivingBase living, AbstractAttributeMap attributeMap, int level) {
		living.getEntityData().setInteger(NBT_KEY_RENDER, living.rand.nextInt(AlchemyEntityManager.FRIENDLY_LIVING_LIST.size() - 1) + 1);
		updateTracker(living);
		super.applyAttributesModifiersToEntity(living, attributeMap, level);
	}
	
	@Override
	public void removeAttributesModifiersFromEntity(EntityLivingBase living, AbstractAttributeMap attributeMap, int level) {
		living.getEntityData().setInteger(NBT_KEY_RENDER, 0);
		updateTracker(living);
		super.removeAttributesModifiersFromEntity(living, attributeMap, level);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderLiving_Pre(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (last == event.getEntity())
			return;
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayer player = minecraft.thePlayer;
		EntityLivingBase living = event.getEntity();
		int id = living.getEntityData().getInteger(NBT_KEY_RENDER);
		if (id != 0) {
			event.setCanceled(true);
			last = AlchemyEntityManager.getEntityById(AlchemyEntityManager.FRIENDLY_LIVING_LIST, id, minecraft.theWorld);
			float partialTick = minecraft.getRenderPartialTicks();
			double lx = living.lastTickPosX + (living.posX - living.lastTickPosX) * partialTick;
			double ly = living.lastTickPosY + (living.posY - living.lastTickPosY) * partialTick;
			double lz = living.lastTickPosZ + (living.posZ - living.lastTickPosZ) * partialTick;
			double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTick;
			double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTick;
			double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTick;
			float f = living.prevRotationYaw + (living.rotationYaw - living.prevRotationYaw) * partialTick;
			glPushMatrix();
			glTranslated(lx - px, ly - py, lz - pz);
			glRotatef(-f % 360, 0, 1, 0);
			RenderHelper.renderEntity(last, partialTick);
			glPopMatrix();
		}
	}
	
	public static class MessageWitchcraftUpdate implements IMessage {
		
		public int id, render_id;
		
		public MessageWitchcraftUpdate(int id, int render_id) {
			this.id = id;
			this.render_id = render_id;
		}
		
		public MessageWitchcraftUpdate() {}

		@Override
		public void fromBytes(ByteBuf buf) {
			id = buf.readInt();
			render_id = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(id);
			buf.writeInt(render_id);
		}
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageWitchcraftUpdate message, MessageContext ctx) {
		World world = Minecraft.getMinecraft().theWorld;
		if (world != null) {
			Entity entity = world.getEntityByID(message.id);
			if (entity != null) {
				entity.getEntityData().setInteger(NBT_KEY_RENDER, message.render_id);
				if (entity == Minecraft.getMinecraft().thePlayer)
					if (message.render_id != 0)
						AlchemyEventSystem.addInputHook(this);
					else 
						AlchemyEventSystem.removeInputHook(this);
			}
		}
		return null;
	}

	@Override
	public Class<MessageWitchcraftUpdate> getClientMessageClass() {
		return MessageWitchcraftUpdate.class;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (event.getEntityLiving().isPotionActive(this) && event.getEntityLiving() instanceof EntityLiving)
			((EntityLiving) event.getEntityLiving()).attackTarget = null;
	}
	
	@SubscribeEvent
	public void onPlayer_StartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof EntityLivingBase) {
			EntityLivingBase living = event.getEntityPlayer();
			if (living.isPotionActive(this))
				updatePlayer((EntityPlayerMP) event.getEntityPlayer(), living);
		}
	}
	
	public void updateTracker(final EntityLivingBase living) {
		int id = living.getEntityData().getInteger(NBT_KEY_RENDER);
		for (EntityPlayer player : ((WorldServer) living.worldObj).getEntityTracker().getTrackingPlayers(living))
			updatePlayer((EntityPlayerMP) player, living, id);
		if (living instanceof EntityPlayerMP)
			updatePlayer((EntityPlayerMP) living, living, id);
	}
	
	public void updatePlayer(EntityPlayerMP player, EntityLivingBase living) {
		updatePlayer(player, living, living.getEntityData().getInteger(NBT_KEY_RENDER));
	}
	
	
	public void updatePlayer(EntityPlayerMP player, EntityLivingBase living, int id) {
		AlchemyNetworkHandler.network_wrapper.sendTo(new MessageWitchcraftUpdate(living.getEntityId(), id), player);
	}
	
	public PotionWitchcraft() {
		super("witchcraft", true, 0XFFFFFF);
	}
	
}