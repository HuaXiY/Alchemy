package index.alchemy.item;

import java.util.List;

import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public abstract class ItemScroll extends AlchemyItem {
	
	public static final String SCROOL_TYPE = "scroll_type", SCROOL_USE = "scroll_use";
	
	public static enum Type {
		POWERFUL,
		BENIGN,
		RUTHLESS,
		GROUPS;
		
		private static int currentId = 0;
		
		private static final int next() {
			return (int) Math.pow(2, currentId++);
		}
		
		public final int id;
		
		private Type() {
			this.id = next();
		}
		
		public boolean contain(Type type){
			return (id & type.id) > 0;
		}
		
		public static int join(Type... ta) {
			int i = 0;
			for (Type t : ta) 
				i |= t.id;
			return i;
		}
	}
	
	public boolean sync, instant, infinite, next;
	public int max;
	
	public ItemScroll(String name, int time, int size, boolean sync, int max) {
		super(name);
		setNoRepair();
		setMaxDamage(time);
		setMaxStackSize(size);
		this.sync = sync;
		this.max = max < 1 ? (infinite = max == -1) ? max : 1 : max;
		this.next = max == 1;
		this.instant = time == 0;
	}
	
	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean flag) {
		
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (player.capabilities.isCreativeMode) {
			if (!sync || Always.isServer()) 
				useScroll(item, world, player, getType(item));
		} else if(instant) {
			onUsingTick(item, player, 1);
		} else 
			player.setActiveHand(hand);
		return new ActionResult(EnumActionResult.SUCCESS, item);
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack item) {
		return EnumAction.BOW;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack item) {
		return item.getMaxDamage();
	}
	
	@Override
	public void onUsingTick(ItemStack item, EntityLivingBase living, int time) {
		item.setItemDamage(time);
		if (time <= 1) {
			onPlayerStoppedUsing(item, living.worldObj, living, time);
			if (nextIsMaxUse(item)) 
				item.stackSize--;
		}
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack item, World world, EntityLivingBase living, int time) {
		if (!canUseItemStack(living, item))
			return;
		item.setItemDamage(0);
		if (time <= 1 && (!sync || Always.isServer())) 
			useScroll(item, world, (EntityPlayer) living, getType(item));
	}
	
	public abstract void useScroll(ItemStack item, World world, EntityPlayer player, int type);
	
	public boolean nextIsMaxUse(ItemStack item) {
		if (next) 
			return true;
		if (infinite) 
			return false;
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) {
			item.setTagCompound(nbt = new NBTTagCompound());
			if (1 >= max) {
				nbt.setInteger(SCROOL_USE, 0);
				return true;
			} else {
				nbt.setInteger(SCROOL_USE, 1);
				return false;
			}
		} else if (nbt.getInteger(SCROOL_USE) + 1 >= max) {
			nbt.setInteger(SCROOL_USE, 0);
			return true;
		} else {
			nbt.setInteger(SCROOL_USE, nbt.getInteger(SCROOL_USE) + 1);
			return false;
		}
	}
	
	public static void addType(ItemStack item, Type... ta) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) 
			item.setTagCompound(nbt = new NBTTagCompound());
		nbt.setInteger(SCROOL_TYPE, nbt.getInteger(SCROOL_TYPE) | Type.join(ta));
	}
	
	public static int getType(ItemStack item) {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) 
			return 0;
		else 
			return nbt.getInteger(SCROOL_TYPE);
	}
	
}
