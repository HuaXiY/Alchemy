package index.alchemy.dlcs.skin.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import index.alchemy.api.annotation.Listener;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
import javafx.scene.image.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
@Listener
@SideOnly(Side.CLIENT)
public class GuiWardrobe extends GuiScreen {
	
	protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("skin:textures/gui_widgets.png");
	
	public class ButtonWardrobe extends GuiButton {
		
		public ButtonWardrobe(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
			super(buttonId, x, y, widthIn, heightIn, buttonText);
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			FontRenderer fontrenderer = mc.fontRendererObj;
			mc.getTextureManager().bindTexture(GuiWardrobe.BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
			int i = getHoverState(hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
					GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(xPosition, yPosition, 0, 46 + i * 20, width / 2, height);
			drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 46 + i * 20, width / 2, height);
			mouseDragged(mc, mouseX, mouseY);
			int color = 14737632;
			if (packedFGColour != 0)
				color = packedFGColour;
			else if (!enabled)
				color = 10526880;
			else if (hovered)
				color = 16777120;
			drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, color);
		}
		
		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			boolean result = super.mousePressed(mc, mouseX, mouseY);
			if (result) {
				if (id == 0) {
					if (Tool.getSafe(list.skins, list.selectedId) != null)
						list.update(list.skins.get(list.selectedId), Minecraft.getMinecraft().thePlayer, true);
				} else if (id == 1) {
					if (++typeId >= SkinCapability.SKIN_TYPES.length)
						typeId = 0;
					list.type = displayString = SkinCapability.SKIN_TYPES[typeId];
				}
			}
			return result;
		}
		
	}
	
	public class GuiSkinList extends GuiListExtended {
		
		public class Entry implements IGuiListEntry {
			
			protected String name;
			protected File file;
			protected byte[] data;
			
			public Entry(String name) {
				this.name = name;
			}
			
			public Entry(File file, byte[] data) {
				name = Tool.get(file.getName(), "(.*)\\.");
				this.file = file;
				this.data = data;
			}
			
			public String getName() {
				return name;
			}
			
			public Entry setName(String name) {
				this.name = name;
				return this;
			}

			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				String str = fontRendererObj.listFormattedStringToWidth(name, listWidth).get(0);
				fontRendererObj.drawStringWithShadow(str, x + listWidth / 2 - fontRendererObj.getStringWidth(str) / 2, y + 1, 0xEEEEEE);
				x += listWidth / 2;
				listWidth = fontRendererObj.getStringWidth(str);
				x -= listWidth / 2 + 1;
				if (isSelected) {
					drawRect(x, y, x + 1, y + slotHeight, 0xFF443533);
					drawRect(x + listWidth, y, x + listWidth + 1, y + slotHeight, 0xFF443533);
					drawRect(x, y, x + listWidth, y + 1, 0xFF443533);
					drawRect(x, y + slotHeight - 1, x + listWidth, y + slotHeight, 0xFF443533);
				}
			}

			@Override
			public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
				update(this, player, false);
				return true;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) { }
			
		}
		
		public final List<GuiSkinList.Entry> skins = Lists.newArrayList();
		protected int selectedId = -1, amount, max = 12;
		protected String type = "default";

		public GuiSkinList(Minecraft mc, int width, int height, int top, int max, int slotHeight) {
			super(mc, width, height, top, top + max * slotHeight - 2, slotHeight);
			left = width;
			right = left + getListWidth();
		}
		
		public void update(GuiSkinList.Entry entry, EntityPlayer player, boolean send) {
			SkinCore.UpdateSkinClient updater = null;
			if (entry.name.equals("default"))
				updater = new SkinCore.UpdateSkinClient(player.getEntityId(), "", new byte[0]);
			else if (entry.file != null)
				updater = new SkinCore.UpdateSkinClient(player.getEntityId(), type, entry.data);
			if (updater != null) {
				updater.onMessage(updater, null);
				if (send)
					AlchemyNetworkHandler.network_wrapper.sendToServer(updater.toUpdateServerMessage());
			}
				
		}
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return skins.get(index);
		}

		@Override
		protected int getSize() {
			return skins.size();
		}
		
		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedId = slotIndex;
			getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, 0, 0, 0);
		}
		
		@Override
		protected boolean isSelected(int slotIndex) {
			return selectedId == slotIndex;
		}
		
		public IGuiListEntry getSelectedEntry() {
			return skins.get(selectedId);
		}
		
		@Override
		public int getListWidth() {
			return 60;
		}
		
		@Override
		public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
			mouseX = mouseXIn;
			mouseY = mouseYIn;
			for (int i = amount, len = Math.min(skins.size(), amount + max); i < len; i++)
				getListEntry(i).drawEntry(i, width, top + slotHeight * (i - amount), getListWidth(), slotHeight,
						mouseXIn, mouseYIn, i == selectedId);
		}
		
		@Override
		public void handleMouseInput() {
			if (isMouseYWithinSlotBounds(mouseY)) {
				if (Mouse.getEventButton() == 0) {
					int k = mouseY - top;
					if (k >= 0) {
						int l = k / slotHeight + amount;
						if (l < getSize() && l >= 0) {
							elementClicked(l, false, mouseX, mouseY);
							selectedElement = l;
						}
					}
				}
				int wheel = Mouse.getEventDWheel();
				wheel = wheel > 0 ? -1 : wheel == 0 ? 0 : 1;
				amount += wheel;
				amount = Math.min(amount, getSize() - max);
				amount = Math.max(amount, 0);
			}
		}
		
	}
	
	public static final ResourceLocation BACKGROUND_TEXTURES = new ResourceLocation("skin:textures/gui_wardrobe.png");
	
	protected static EntityPlayer player;
	
	@SubscribeEvent
	public static void onRenderLiving_Specials_Pre(RenderLivingEvent.Specials.Pre event) {
		if (event.getEntity() == player)
			AlchemyEventSystem.markEventCanceled(event);
	}
	
	public static void init() {
		if (player == null)
			player = new EntityOtherPlayerMP(Minecraft.getMinecraft().theWorld,
					new GameProfile(UUID.randomUUID(), "preview")) {
				
				{ setEntityId(Integer.MAX_VALUE - 10); }
				
				@Override
				public ResourceLocation getLocationSkin() {
					ResourceLocation skin = getCapability(SkinCore.skin_info, null).skin_mapping.get(Type.SKIN);
					return skin != null ? skin : super.getLocationSkin();
				}
				
				@Override
				public String getSkinType() {
					String type = getCapability(SkinCore.skin_info, null).skin_type;
					return type != null ? type : super.getSkinType();
				}
				
				@Override
				public boolean getAlwaysRenderNameTagForRender() {
					return false;
				}
				
				@Override
				public boolean hasCustomName() {
					return false;
				}
				
				@Override
				public boolean isSpectator() {
					return false;
				}
				
				@Override
				public boolean isPlayerInfoSet() {
					return false;
				}
				
				@Override
				public boolean hasSkin() {
					return true;
				}
				
				@Override
				public boolean hasPlayerInfo() {
					return false;
				}
				
				@Override
				public boolean isWearing(EnumPlayerModelParts part) {
					return true;
				}
				
			};
		}
	
	{ init(); }
	
	protected GuiSkinList list;
	protected int typeId;
	
	@Override
	public void initGui() {
		list = new GuiSkinList(mc, (width - 174) / 2 + 112, 0, (height - 161) / 2 + 6, 12, 11);
		list.skins.add(list.new Entry("default"));
		list.skins.addAll(getAllEntry());
		buttonList.clear();
		buttonList.add(new ButtonWardrobe(0, (width - 174) / 2 + 143, (height - 161) / 2 + 140, 30, 15,
				I18n.translateToLocal("skin.text.use")));
		buttonList.add(new ButtonWardrobe(1, (width - 174) / 2 + 113, (height - 161) / 2 + 140, 30, 15,
				SkinCapability.SKIN_TYPES[typeId]));
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURES);
		drawTexturedModalRect((width - 174) / 2, (height - 161) / 2, 0, 0, 174, 161);
		int guiWidth = (width - 174) / 2 + 58, guiHeight = (height - 161) / 2 + 140;
		GuiInventory.drawEntityOnScreen(guiWidth, guiHeight, 60, guiWidth - mouseX, guiHeight - mouseY, player);
		list.drawScreen(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	public List<GuiSkinList.Entry> getAllEntry() {
		List<GuiSkinList.Entry> result = Lists.newArrayList();
		File skin = new File(AlchemyEngine.getMinecraftDir(), "skin");
		if (skin.isDirectory())
			Arrays.stream(skin.listFiles()).forEach(f -> {
				try {
					if (f.getName().endsWith(".png")) {
						Image image = new Image(new FileInputStream(f));
						if (SkinHelper.isSkin(image)) {
							if (SkinHelper.isX32(image))
								image = SkinHelper.x32Tox64(image);
							result.add(list.new Entry(f, SkinHelper.toInputSteam(image)));
						}
					}
				} catch(Exception e) { }
			});
		return result;
	}
	
}
