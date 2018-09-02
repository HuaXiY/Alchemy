package index.alchemy.network;

import index.project.version.annotation.Omega;

@Omega
public class Double6IntArrayPackage {
    
    public double x, y, z, ox, oy, oz;
    public int args[];
    
    public Double6IntArrayPackage(double x, double y, double z, double ox, double oy, double oz, int... args) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.args = args;
    }
    
}