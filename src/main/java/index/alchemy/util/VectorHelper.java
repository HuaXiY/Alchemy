package index.alchemy.util;

import net.minecraft.util.math.Vec3d;

import static java.lang.Math.*;
import static index.alchemy.util.MathHelper.*;

public class VectorHelper {
	
	public static Vec3d offset(Vec3d a, Vec3d b) {
		double dx = a.xCoord - b.xCoord;
		double dy = a.yCoord - b.yCoord;
		double dz = a.zCoord - b.zCoord;
		double max = dx * dx + dy * dy + dz * dz;
		return new Vec3d(mod(dx) * pow(dx, 2) / max, mod(dy) * pow(dy, 2) / max, mod(dz) * pow(dz, 2) / max);
	}

}