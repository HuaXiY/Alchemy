package index.alchemy.entity.control;

import java.util.stream.Stream;

import index.alchemy.api.IFollower;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Patch;
import index.alchemy.api.annotation.Remote;
import index.alchemy.api.annotation.SuppressFBWarnings;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.util.Always;
import index.project.version.annotation.Alpha;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Alpha
@Listener
@Hook.Provider
@Remote.Provider
public class SingleProjection {
	
	@Remote(Side.SERVER)
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static void syncSingleProjection(Stream<EntityPlayer> players) {
		EntityPlayer player = players.findAny().get();
		EntityLivingBase follower = IFollower.follower.get(player);
		if (follower != null) {
			((IFollower) follower).setProjectionState(false);
			follower.setDead();
		}
	}
	
	@Remote(Side.SERVER)
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static void syncSingleProjection(Stream<EntityPlayer> players, double x, double y, double z,
			float rotationYaw, float rotationPitch, double mx, double my, double mz) {
		EntityPlayer player = players.findAny().get();
		EntityLivingBase follower = IFollower.follower.get(player);
		if (follower != null) {
			((IFollower) follower).setProjectionState(true);
			follower.setPositionAndRotation(x, y, z, rotationYaw, rotationPitch);
			follower.motionX = mx;
			follower.motionY = my;
			follower.motionZ = mz;
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static boolean projectionState;
	
	@SideOnly(Side.CLIENT)
	private static void setProjectionState(boolean projectionState) {
		SingleProjection.projectionState = projectionState;
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isProjectionState() { return projectionState; }
	
	@SideOnly(Side.CLIENT)
	private static EntityLivingBase follower;
	
	@SideOnly(Side.CLIENT)
	public static EntityLivingBase getFollower() { return follower; }
	
	@SideOnly(Side.CLIENT)
	public static void cutoverState() {
		if (isProjectionState())
			reduction();
		else
			projection();
	}
	
	@SideOnly(Side.CLIENT)
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static void reduction() {
		follower = null;
		setProjectionState(false);
		EntityPlayer player = Always.lookupPlayer();
		EntityLivingBase follower = IFollower.follower.get(player);
		if (follower != null && follower instanceof IFollower)
			((IFollower) follower).setProjectionState(false);
		Minecraft.getMinecraft().setRenderViewEntity(player);
		syncSingleProjection(Stream.of(player));
	}
	
	@SideOnly(Side.CLIENT)
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static void projection() {
		EntityLivingBase follower = IFollower.follower.get(Always.lookupPlayer());
		if (follower != null && follower instanceof IFollower)
			projectionFollower(follower);
	}
	
	@SideOnly(Side.CLIENT)
	public static void projectionFollower(EntityLivingBase follower) {
		if (follower instanceof IFollower) {
			if (Always.isClient()) {
				SingleProjection.follower = follower;
				setProjectionState(true);
				projectionCamera(follower);
				syncSingleProjection(Stream.of(Always.lookupPlayer()), follower.posX, follower.posY, follower.posZ,
						follower.rotationYaw, follower.rotationPitch, follower.motionX, follower.motionY, follower.motionZ);
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
		if (isProjectionState()) {
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
		if (isProjectionState() && Minecraft.getMinecraft().player.movementInput == input) {
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
	
	@SubscribeEvent(priority = EventPriority.BOTTOM)
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static void onLivingAttack(LivingAttackEvent event) {
		EntityLivingBase follower = IFollower.follower.get(event.getEntityLiving());
		if (follower != null && ((IFollower) follower).getProjectionState())
			follower.setDead();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static void onClientTick(ClientTickEvent event) {
		if (isProjectionState()) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player != null) {
				if (IFollower.follower.get(player) != follower)
				reduction();
				AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
				double d0 = player.posX - player.lastReportedPosX;
				double d1 = axisalignedbb.minY - player.lastReportedPosY;
				double d2 = player.posZ - player.lastReportedPosZ;
				double d3 = player.rotationYaw - player.lastReportedYaw;
				double d4 = player.rotationPitch - player.lastReportedPitch;
				++player.positionUpdateTicks;
				boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4 || player.positionUpdateTicks >= 20;
				boolean flag3 = d3 != 0.0 || d4 != 0.0;
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
		if (isProjectionState())
			AlchemyEventSystem.markEventCanceled(event);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onRenderSpecificHand(RenderSpecificHandEvent event) { 
		if (isProjectionState())
			AlchemyEventSystem.markEventCanceled(event);
	}
	
	@SideOnly(Side.CLIENT)
	@Patch("net.minecraft.client.renderer.entity.RenderPlayer")
	public static class Patch$RenderPlayer extends RenderPlayer {

		@Patch.Exception
		public Patch$RenderPlayer(RenderManager renderManager) { super(renderManager); }
		
		@Override
		public void doRender(@Patch.Generic("Lnet/minecraft/entity/EntityLivingBase;") AbstractClientPlayer entity,
				double x, double y, double z, float entityYaw, float partialTicks) {
			if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
					new net.minecraftforge.client.event.RenderPlayerEvent.Pre(entity, this, partialTicks, x, y, z)))
				return;
			if (!entity.isUser() || renderManager.renderViewEntity == entity || Minecraft.getMinecraft().player == entity) {
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
