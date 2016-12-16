package index.alchemy.dlcs.skin.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Message;
import index.alchemy.client.MemoryTexture;
import index.alchemy.interacting.WoodType;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.Always;
import index.alchemy.util.Tool;
import index.project.version.annotation.Alpha;
import io.netty.buffer.ByteBuf;

import static index.alchemy.core.AlchemyConstants.*;
import static index.alchemy.dlcs.skin.core.SkinCore.*;

@Alpha
@Init(state = ModState.INITIALIZED)
@DLC(id = DLC_ID, mcVersion = MC_VERSION, name = DLC_NAME, version = DLC_VERSION)
public class SkinCore {
	
	public static final String
			DLC_ID = "skin",
			DLC_NAME = "Skin",
			DLC_VERSION = "0.0.1-dev";
	
	@Message(Side.SERVER)
	public static class UpdateSkinServer implements IMessage, IMessageHandler<UpdateSkinServer, IMessage> {
		
		public String type;
		public byte[] data;
		
		public UpdateSkinServer() { }
		
		public UpdateSkinServer(String type, byte data[]) {
			this.type = type;
			this.data = data;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			type = ByteBufUtils.readUTF8String(buf);
			data = new byte[buf.readInt()];
			buf.readBytes(data);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			ByteBufUtils.writeUTF8String(buf, type);
			buf.writeInt(data.length);
			buf.writeBytes(data);
		}

		@Override
		public IMessage onMessage(UpdateSkinServer message, MessageContext ctx) {
			SkinInfo info = ctx.getServerHandler().playerEntity.getCapability(SkinCore.skin_info, null);
			info.skin_type = message.type;
			info.skin_data = message.data;
			updatePlayerSkin(ctx.getServerHandler().playerEntity);
			return null;
		}
		
	}
	
	@Message(Side.CLIENT)
	public static class UpdateSkinClient implements IMessage, IMessageHandler<UpdateSkinClient, IMessage> {
		
		public int id;
		public String type;
		public byte[] data;
		
		public UpdateSkinClient() { }
		
		public UpdateSkinClient(int id, String type, byte data[]) {
			this.id = id;
			this.type = Tool.isEmptyOr(type, "");
			this.data = Tool.isNullOr(data, new byte[0]);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			id = buf.readInt();
			type = ByteBufUtils.readUTF8String(buf);
			data = new byte[buf.readInt()];
			buf.readBytes(data);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(id);
			ByteBufUtils.writeUTF8String(buf, type);
			buf.writeInt(data.length);
			buf.writeBytes(data);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(UpdateSkinClient message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				
				@Override
				public void run() {
					World world = Minecraft.getMinecraft().theWorld;
					if (world != null) {
						Entity entity = world.getEntityByID(message.id);
						if (entity == null && message.id == Integer.MAX_VALUE - 10)
							entity = GuiWardrobe.player;
						if (entity != null) {
							SkinInfo info = entity.getCapability(SkinCore.skin_info, null);
							if (info != null) {
								ResourceLocation skin = new ResourceLocation("skin:" + entity.getName());
								Minecraft.getMinecraft().getTextureManager().deleteTexture(skin);
								if (message.data.length > 0) {
									Minecraft.getMinecraft().getTextureManager().loadTexture(skin, new MemoryTexture(message.data));
									info.skin_mapping.put(Type.SKIN, skin);
									info.skin_type = message.type;
								} else {
									info.skin_mapping.put(Type.SKIN, null);
									info.skin_type = null;
								}
							}
						}
					}
				}
				
			});
			return null;
		}
		
		public UpdateSkinServer toUpdateServerMessage() {
			return new UpdateSkinServer(type, data);
		}
		
	}
	
	@CapabilityInject(SkinCapability.class)
	public static final Capability<SkinInfo> skin_info = null;
	
	public static CommandBase update_skin = new CommandUpdateSkin();
	
	public static void init() {
		for (WoodType type : WoodType.types) {
			BlockWardrobe wardrobe = new BlockWardrobe(type);
			GameRegistry.addRecipe(new ItemStack(wardrobe), "BAB", "B B", "BAB", 'B', type.log, 'A', type.plank);
			if (Always.isClient())
				Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(wardrobe), 0,
						new ModelResourceLocation(wardrobe.getRegistryName(), "facing=south,part=foot,rely=null"));
		}
		if (Always.isClient())
			registerModelLoader();
	}
	
	@SideOnly(Side.CLIENT)
	private static void registerModelLoader() {
		ModelLoaderRegistry.registerLoader(new WardrobeModelLoader());
	}
	
	@EventHandler
	@SideOnly(Side.CLIENT)
	public void onFMLLoadComplete(FMLLoadCompleteEvent event) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.loadTexture(GuiWardrobe.BACKGROUND_TEXTURES, new SimpleTexture(GuiWardrobe.BACKGROUND_TEXTURES));
		textureManager.loadTexture(GuiWardrobe.BUTTON_TEXTURES, new SimpleTexture(GuiWardrobe.BUTTON_TEXTURES));
	}
	
	public static void updatePlayerSkin(EntityPlayer player) {
		SkinInfo info = player.getCapability(skin_info, null);
		for (EntityPlayer other : ((WorldServer) player.worldObj).getEntityTracker().getTrackingPlayers(player))
			AlchemyNetworkHandler.network_wrapper.sendTo(new UpdateSkinClient(player.getEntityId(), info.skin_type, info.skin_data),
					(EntityPlayerMP) other);
	}
	
	@SideOnly(Side.CLIENT)
	public static void updateSkin(String type, byte data[], boolean sendToServer) {
		UpdateSkinClient message = new UpdateSkinClient(Minecraft.getMinecraft().thePlayer.getEntityId(), type, data);
		message.onMessage(message, null);
		if (sendToServer)
			AlchemyNetworkHandler.network_wrapper.sendToServer(message.toUpdateServerMessage());
	}

}
