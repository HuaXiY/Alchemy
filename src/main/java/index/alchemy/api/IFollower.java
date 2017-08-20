package index.alchemy.api;

import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Hook.Provider
@Field.Provider
public interface IFollower {
	
	@SideOnly(Side.CLIENT)
	@Hook("net.minecraft.network.play.server.SPacketEntity#func_149065_a")
	public static Hook.Result getEntity(SPacketEntity packet, World world) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null) {
			IFollower follower = (IFollower) IFollower.follower.get(player);
			if (follower != null && follower.getProjectionState() && world.getEntityByID(packet.entityId) == follower)
				return Hook.Result.NULL;
		}
		return Hook.Result.VOID;
	}
	
	String NBT_KEY_OWNER_UUID = "owner_uuid";
	
	IFieldAccess<EntityLivingBase, EntityLivingBase> follower = null;
	
	void setOwner(EntityLivingBase living);
	
	EntityLivingBase getOwner();
	
	void setProjectionState(boolean projectionState);
	
	boolean getProjectionState();

}
