package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import index.project.version.annotation.Omega;

import net.minecraft.entity.Entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
public class FXPosSourceUpdate implements IFXUpdate {

    protected Entity entity;

    public FXPosSourceUpdate(Entity entity) {
        this.entity = entity;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean updateFX(AlchemyFX fx, long tick) {
        fx.setPosSource(entity);
        return true;
    }

}
