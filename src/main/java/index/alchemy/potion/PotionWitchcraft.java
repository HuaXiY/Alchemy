package index.alchemy.potion;


import org.lwjgl.opengl.GL11;

import index.alchemy.api.IEventHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.entity.AlchemyEntityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.EntityDataManager.DataEntry;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionWitchcraft extends AlchemyPotion implements IEventHandle {
	
	public static final String NBT_KEY_RENDER = "wc_render";
	
	public static DataParameter<Integer> FLAGS = EntityDataManager.<Integer>createKey(EntityLivingBase.class, DataSerializers.VARINT);;
	
	private EntityLivingBase last;
	
	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@Override
	public void performEffect(EntityLivingBase base, int level) {
		int id = base.getDataManager().get(FLAGS);
		if (id == 0 || id >= AlchemyEntityManager.FRIENDLY_LIVING_LIST.size()) {
			id = base.rand.nextInt(AlchemyEntityManager.FRIENDLY_LIVING_LIST.size());
			base.getDataManager().set(FLAGS, id);
		}
		if (base instanceof EntityLiving) {
			EntityLiving living = (EntityLiving) base;
			living.tasks.tickCount = 1;
			living.targetTasks.tickCount = 1;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderLiving_Pre(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (last == event.getEntity())
			return;
		EntityLivingBase living = event.getEntity();
		Minecraft minecraft = Minecraft.getMinecraft();
		int id = living.getDataManager().get(FLAGS);
		if (id != 0) {
			event.setCanceled(true);
			last = AlchemyEntityManager.getEntityById(AlchemyEntityManager.FRIENDLY_LIVING_LIST, id, minecraft.theWorld);
			GL11.glRotatef(living.rotationPitch, 0, 1, 0);
			minecraft.getRenderManager().doRenderEntity(last,
					0, 0, 0, 0, minecraft.getRenderPartialTicks(), false);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		for (DataEntry<?> entry : event.getEntity().getDataManager().getAll())
			System.out.println(entry.getKey().getId() + " - " + entry.getValue());
		System.out.println(FLAGS.getId() + " - " + event.getEntity().getDataManager().get(FLAGS));
		//if (event.getEntity() instanceof EntityLivingBase)
		//	event.getEntity().getDataManager().register(FLAGS, 0);
	}
	
	public void removeAttributesModifiersFromEntity(EntityLivingBase living, AbstractAttributeMap attributeMap, int level) {
		living.getDataManager().set(FLAGS, 0);
	}

	public PotionWitchcraft() {
		super("witchcraft", true, 0XFFFFFF);
	}

}