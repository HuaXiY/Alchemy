package index.alchemy.client.fx.update;

import index.alchemy.api.IFXUpdate;
import index.alchemy.api.ILocationProvider;
import index.alchemy.client.fx.AlchemyFX;
import index.project.version.annotation.Omega;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.util.math.MathHelper.*;

@Omega
public class FXTowUpdate implements IFXUpdate {
	
	protected ILocationProvider location;
	protected double acceleration;
	
	public FXTowUpdate(ILocationProvider location, double acceleration) {
		this.location = location;
		this.acceleration = acceleration;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean updateFX(AlchemyFX fx, long tick) {
		Vec3d src = location.getLocation();
		Vec3d tra = new Vec3d(fx.getPosX(), fx.getPosY(), fx.getPosZ());
		
		double dx = src.xCoord - tra.xCoord;
		double dy = src.yCoord - tra.yCoord;
		double dz = src.zCoord - tra.zCoord;
		double max = sqrt_double(dx * dx + dy * dy + dz * dz);
		
		fx.setMotionX(dx / max * acceleration);
		fx.setMotionY(dy / max * acceleration);
		fx.setMotionZ(dz / max * acceleration);
		return false;
	}

}
