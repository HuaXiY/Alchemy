package index.alchemy.dlcs.exnails.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import index.alchemy.api.annotation.Listener;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import toughasnails.api.HealthHelper;
import toughasnails.config.GameplayOption;
import toughasnails.config.SyncedConfigHandler;

@Listener
public class ExMaxHealthHandler {
	
	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
		IAttributeInstance maxHealthInstance = event.player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
		AttributeModifier modifier = maxHealthInstance.getModifier(HealthHelper.LIFEBLOOD_HEALTH_MODIFIER_ID);
		
		if (SyncedConfigHandler.getBooleanValue(GameplayOption.ENABLE_LOWERED_STARTING_HEALTH) && modifier != null) { 
			Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();
			multimap.put(SharedMonsterAttributes.MAX_HEALTH.getAttributeUnlocalizedName(), modifier);
			event.player.getAttributeMap().applyAttributeModifiers(multimap);
		}
	}

}
