package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.client.fx.AlchemyFX;
import index.project.version.annotation.Omega;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
public class FXAgeUpdate implements IFXUpdate {

    protected int age;

    public FXAgeUpdate(int age) {
        this.age = age;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean updateFX(AlchemyFX fx, long tick) {
        fx.setMaxAge(age);
        return true;
    }

}
