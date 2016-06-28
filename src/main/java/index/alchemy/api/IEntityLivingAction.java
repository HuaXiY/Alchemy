package index.alchemy.api;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;

public interface IEntityLivingAction {
	
	public void apply(@Nullable EntityLivingBase src, EntityLivingBase living, float amplify);

}