package index.alchemy.entity.control;

import index.alchemy.api.IFollower;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Message;
import index.alchemy.api.annotation.Patch;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.Always;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Listener
@Hook.Provider
public class SingleProjection {
	
	@Message(Side.SERVER)
	public static class MessageSingleProjection implements IMessage, IMessageHandler<MessageSingleProjection, IMessage> {
		
		public boolean state;
		public double x, y, z, mx, my, mz;
		public float rotationYaw, rotationPitch;
		
		public MessageSingleProjection() { }
		
		public MessageSingleProjection(EntityLivingBase follower) {
			state = true;
			x = follower.posX;
			y = follower.posY;
			z = follower.posZ;
			mx = follower.motionX;
			my = follower.motionY;
			mz = follower.motionZ;
			rotationYaw = follower.rotationYaw;
			rotationPitch = follower.rotationPitch;
		}
		
		@Override
		public IMessage onMessage(MessageSingleProjection message, MessageContext ctx) {
			AlchemyEventSystem.addDelayedRunnable(p -> {
				EntityPlayer player = ctx.getServerHandler().playerEntity;
				EntityLivingBase follower = IFollower.follower.get(player);
				if (follower != null) {
					((IFollower) follower).setProjectionState(message.state);
					if (message.state) {
						follower.setPositionAndRotation(message.x, message.y, message.z, message.rotationYaw, message.rotationPitch);
						follower.motionX = message.mx;
						follower.motionY = message.my;
						follower.motionZ = message.mz;
					} else
						follower.setDead();
				}
			}, 0);
			return null;
		}
		
		@Override
		public void fromBytes(ByteBuf buf) {
			state = buf.readBoolean();
			if (state) {
				x = buf.readDouble();
				y = buf.readDouble();
				z = buf.readDouble();
				mx = buf.readDouble();
				my = buf.readDouble();
				mz = buf.readDouble();
				rotationYaw = buf.readFloat();
				rotationPitch = buf.readFloat();
			}
		}
		
		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeBoolean(state);
			if (state) {
				buf.writeDouble(x);
				buf.writeDouble(y);
				buf.writeDouble(z);
				buf.writeDouble(mx);
				buf.writeDouble(my);
				buf.writeDouble(mz);
				buf.writeFloat(rotationYaw);
				buf.writeFloat(rotationPitch);
			}
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	private static boolean projectionState;
	
	public static boolean isProjectionState() {
		return projectionState;
	}
	
	@SideOnly(Side.CLIENT)
	private static EntityLivingBase follower;
	
	public static EntityLivingBase getFollower() {
		return follower;
	}
	
	@SideOnly(Side.CLIENT)
	public static void cutoverState() {
		if (projectionState)
			reduction();
		else
			projection();
	}
	
	@SideOnly(Side.CLIENT)
	public static void reduction() {
		follower = null;
		projectionState = false;
		EntityLivingBase follower = IFollower.follower.get(Minecraft.getMinecraft().thePlayer);
		if (follower != null && follower instanceof IFollower)
			((IFollower) follower).setProjectionState(false);
		Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().thePlayer);
		AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageSingleProjection());
	}
	
	@SideOnly(Side.CLIENT)
	public static void projection() {
		EntityLivingBase follower = IFollower.follower.get(Minecraft.getMinecraft().thePlayer);
		if (follower != null && follower instanceof IFollower)
			projectionFollower(follower);
	}
	
	@SideOnly(Side.CLIENT)
	public static void projectionFollower(EntityLivingBase follower) {
		if (follower instanceof IFollower) {
			if (Always.isClient()) {
				SingleProjection.follower = follower;
				projectionState = true;
				projectionCamera(follower);
				AlchemyNetworkHandler.network_wrapper.sendToServer(new MessageSingleProjection(follower));
			}
			((IFollower) follower).setProjectionState(true);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void projectionCamera(Entity entity) {
		Minecraft.getMinecraft().setRenderViewEntity(entity);
	}
	
	@SideOnly(Side.CLIENT)
	@Hook("net.minecraft.client.Minecraft#func_184117_aA")
	public static Hook.Result processKeyBinds(Minecraft mc) {
		if (projectionState) {
			for (; mc.gameSettings.keyBindTogglePerspective.isPressed(); mc.renderGlobal.setDisplayListEntitiesDirty()) {
	            ++mc.gameSettings.thirdPersonView;

	            if (mc.gameSettings.thirdPersonView > 2)
	            	mc.gameSettings.thirdPersonView = 0;

	            if (mc.gameSettings.thirdPersonView == 0)
	            	mc.entityRenderer.loadEntityShader(mc.getRenderViewEntity());
	            else if (mc.gameSettings.thirdPersonView == 1)
	            	mc.entityRenderer.loadEntityShader(null);
	        }

	        while (mc.gameSettings.keyBindSmoothCamera.isPressed())
	        	mc.gameSettings.smoothCamera = !mc.gameSettings.smoothCamera;
	        
	        boolean flag = mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

	        if (flag) {
	            while (mc.gameSettings.keyBindChat.isPressed())
	            	mc.displayGuiScreen(new GuiChat());

	            if (mc.currentScreen == null && mc.gameSettings.keyBindCommand.isPressed())
	            	mc.displayGuiScreen(new GuiChat("/"));
	        }
			return Hook.Result.NULL;
		}
		return Hook.Result.VOID;
	}
	
	@SideOnly(Side.CLIENT)
	@Hook("net.minecraft.util.MovementInputFromOptions#func_78898_a")
	public static Hook.Result updatePlayerMoveState(MovementInputFromOptions input) {
		if (projectionState && Minecraft.getMinecraft().thePlayer.movementInput == input) {
			input.moveForward = 0;
			input.moveStrafe = 0;
			input.backKeyDown = false;
			input.forwardKeyDown = false;
			input.leftKeyDown = false;
			input.rightKeyDown = false;
			input.jump = false;
			input.sneak = false;
			return Hook.Result.NULL;
		}
		return Hook.Result.VOID;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingAttack(LivingAttackEvent event) {
		EntityLivingBase follower = IFollower.follower.get(event.getEntityLiving());
		if (((IFollower) follower).getProjectionState())
			follower.setDead();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onClientTick(ClientTickEvent event) {
		if (projectionState) {
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			if (player != null) {
				if (IFollower.follower.get(player) != follower)
				reduction();
				boolean flag3;
				AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
				double d0 = player.posX - player.lastReportedPosX;
				double d1 = axisalignedbb.minY - player.lastReportedPosY;
				double d2 = player.posZ - player.lastReportedPosZ;
				double d3 = player.rotationYaw - player.lastReportedYaw;
				double d4 = player.rotationPitch - player.lastReportedPitch;
				++player.positionUpdateTicks;
				boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4 || player.positionUpdateTicks >= 20;
				boolean bl = flag3 = d3 != 0.0 || d4 != 0.0;
				if (player.isRiding()) {
					player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.motionX, -999.0,
							player.motionZ, player.rotationYaw, player.rotationPitch, player.onGround));
					flag2 = false;
				} else if (flag2 && flag3)
					player.connection.sendPacket(new CPacketPlayer.PositionRotation(player.posX, axisalignedbb.minY,
							player.posZ, player.rotationYaw, player.rotationPitch, player.onGround));
				else if (flag2)
					player.connection.sendPacket(
							new CPacketPlayer.Position(player.posX, axisalignedbb.minY, player.posZ, player.onGround));
				else if (flag3)
					player.connection.sendPacket(
							new CPacketPlayer.Rotation(player.rotationYaw, player.rotationPitch, player.onGround));
				else if (player.prevOnGround != player.onGround)
					player.connection.sendPacket(new CPacketPlayer(player.onGround));
				if (flag2) {
					player.lastReportedPosX = player.posX;
					player.lastReportedPosY = axisalignedbb.minY;
					player.lastReportedPosZ = player.posZ;
					player.positionUpdateTicks = 0;
				}
				if (flag3) {
					player.lastReportedYaw = player.rotationYaw;
					player.lastReportedPitch = player.rotationPitch;
				}
				player.prevOnGround = player.onGround;
				player.autoJumpEnabled = player.mc.gameSettings.autoJump;
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onRenderHand(RenderHandEvent event) {
		if (projectionState)
			AlchemyEventSystem.markEventCanceled(event);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onRenderSpecificHand(RenderSpecificHandEvent event) { 
		if (projectionState)
			AlchemyEventSystem.markEventCanceled(event);
	}
	
	@SideOnly(Side.CLIENT)
	@Patch("net.minecraft.client.renderer.entity.RenderPlayer")
	public static class ExRenderPlayer extends RenderPlayer {

		@Patch.Exception
		public ExRenderPlayer(RenderManager renderManager) { super(renderManager); }
		
		@Override
		public void doRender(@Patch.Generic("Lnet/minecraft/entity/EntityLivingBase;") AbstractClientPlayer entity,
				double x, double y, double z, float entityYaw, float partialTicks) {
			if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
					new net.minecraftforge.client.event.RenderPlayerEvent.Pre(entity, this, partialTicks, x, y, z)))
				return;
			if (!entity.isUser() || renderManager.renderViewEntity == entity || Minecraft.getMinecraft().thePlayer == entity) {
				double ny = y;
				
				if (entity.isSneaking() && !(entity instanceof EntityPlayerSP))
					ny = y - 0.125D;
				
				ModelPlayer modelplayer = getMainModel();
				modelplayer.bipedHead.isHidden = false;
	            modelplayer.bipedHeadwear.isHidden = false;
				GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
				super.doRender(entity, x, ny, z, entityYaw, partialTicks);
				GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
			}
			net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
					new net.minecraftforge.client.event.RenderPlayerEvent.Post(entity, this, partialTicks, x, y, z));
		}
		
	}
	
}
