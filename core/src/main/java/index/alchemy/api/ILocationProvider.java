package index.alchemy.api;

import net.minecraft.util.math.Vec3d;

@FunctionalInterface
public interface ILocationProvider {
    
    Vec3d getLocation();
    
}