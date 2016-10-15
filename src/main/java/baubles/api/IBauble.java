package baubles.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * 
 * This interface should be extended by items that can be worn in bauble slots
 * 
 * @author Azanor
 * 
 * About adding CapabilityBauble to EntityLivingBase
 * see: {@link index.alchemy.capability.CapabilityBauble#onAttachCapabilities_Entity(net.minecraftforge.event.AttachCapabilitiesEvent.Entity)}
 * 
 * @author Mickeyxiami
 */

public interface IBauble {
	
	/**
	 * This method return the type of bauble this is. 
	 * Type is used to determine the slots it can go into.
	 */
	public BaubleType getBaubleType(ItemStack itemstack);
	
	/**
	 * This method is called once per tick if the bauble is being worn by a living
	 */
	public default void onWornTick(ItemStack itemstack, EntityLivingBase living) { }
	
	/**
	 * This method is called when the bauble is equipped by a living
	 */
	public default void onEquipped(ItemStack itemstack, EntityLivingBase living) { }
	
	/**
	 * This method is called when the bauble is unequipped by a living
	 */
	public default void onUnequipped(ItemStack itemstack, EntityLivingBase living) { }

	/**
	 * can this bauble be placed in a bauble slot
	 */
	public default boolean canEquip(ItemStack itemstack, EntityLivingBase living) { return true; }
	
	/**
	 * Can this bauble be removed from a bauble slot
	 */
	public default boolean canUnequip(ItemStack itemstack, EntityLivingBase living) { return true; }
	
}