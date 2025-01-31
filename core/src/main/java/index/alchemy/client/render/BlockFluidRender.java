package index.alchemy.client.render;

import index.project.version.annotation.Beta;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
@SideOnly(Side.CLIENT)
public class BlockFluidRender {
    
    private final BlockColors blockColors;
    
    public BlockFluidRender(BlockColors blockColorsIn) {
        blockColors = blockColorsIn;
    }
    
    public boolean renderFluid(IBlockAccess blockAccess, IBlockState blockStateIn, BlockPos blockPosIn, BufferBuilder worldRendererIn, boolean aboolean[]) {
        Minecraft minecraft = Minecraft.getMinecraft();
        BlockRendererDispatcher dispatcher = minecraft.getBlockRendererDispatcher();
        TextureAtlasSprite textureatlassprite = dispatcher.getBlockModelShapes().getTexture(blockStateIn);
        int i = blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn, 0);
        float f = (float) (i >> 16 & 255) / 255.0F;
        float f1 = (float) (i >> 8 & 255) / 255.0F;
        float f2 = (float) (i & 255) / 255.0F;
        
        if (!aboolean[0] && !aboolean[1] && !aboolean[2] && !aboolean[3] && !aboolean[4] && !aboolean[5]) {
            return false;
        }
        else {
            boolean flag3 = false;
            float f7 = 1.0F;
            float f8 = 1.0F;
            float f9 = 1.0F;
            float f10 = 1.0F;
            double d0 = blockPosIn.getX();
            double d1 = blockPosIn.getY();
            double d2 = blockPosIn.getZ();
            
            if (aboolean[0]) {
                flag3 = true;
                f7 -= 0.001F;
                f8 -= 0.001F;
                f9 -= 0.001F;
                f10 -= 0.001F;
                float f13 = textureatlassprite.getInterpolatedU(0.0D);
                float f14 = f13;
                float f15 = textureatlassprite.getInterpolatedU(16.0D);
                float f16 = f15;
                float f17 = textureatlassprite.getInterpolatedV(0.0D);
                float f18 = textureatlassprite.getInterpolatedV(16.0D);
                float f19 = f18;
                float f20 = f17;
                
                int k2 = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn);
                int l2 = k2 >> 16 & 65535;
                int i3 = k2 & 65535;
                float f24 = 1.0F * f;
                float f25 = 1.0F * f1;
                float f26 = 1.0F * f2;
                worldRendererIn.pos(d0 + 0.0D, d1 + (double) f7, d2 + 0.0D).color(f24, f25, f26, 1.0F).tex((double) f13, (double) f17).lightmap(l2, i3).endVertex();
                worldRendererIn.pos(d0 + 0.0D, d1 + (double) f8, d2 + 1.0D).color(f24, f25, f26, 1.0F).tex((double) f14, (double) f18).lightmap(l2, i3).endVertex();
                worldRendererIn.pos(d0 + 1.0D, d1 + (double) f9, d2 + 1.0D).color(f24, f25, f26, 1.0F).tex((double) f15, (double) f19).lightmap(l2, i3).endVertex();
                worldRendererIn.pos(d0 + 1.0D, d1 + (double) f10, d2 + 0.0D).color(f24, f25, f26, 1.0F).tex((double) f16, (double) f20).lightmap(l2, i3).endVertex();
                
                if (aboolean[0]) {
                    worldRendererIn.pos(d0 + 0.0D, d1 + (double) f7, d2 + 0.0D).color(f24, f25, f26, 1.0F).tex((double) f13, (double) f17).lightmap(l2, i3).endVertex();
                    worldRendererIn.pos(d0 + 1.0D, d1 + (double) f10, d2 + 0.0D).color(f24, f25, f26, 1.0F).tex((double) f16, (double) f20).lightmap(l2, i3).endVertex();
                    worldRendererIn.pos(d0 + 1.0D, d1 + (double) f9, d2 + 1.0D).color(f24, f25, f26, 1.0F).tex((double) f15, (double) f19).lightmap(l2, i3).endVertex();
                    worldRendererIn.pos(d0 + 0.0D, d1 + (double) f8, d2 + 1.0D).color(f24, f25, f26, 1.0F).tex((double) f14, (double) f18).lightmap(l2, i3).endVertex();
                }
            }
            
            if (aboolean[1]) {
                float f35 = textureatlassprite.getMinU();
                float f36 = textureatlassprite.getMaxU();
                float f37 = textureatlassprite.getMinV();
                float f38 = textureatlassprite.getMaxV();
                int l1 = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down());
                int i2 = l1 >> 16 & 65535;
                int j2 = l1 & 65535;
                worldRendererIn.pos(d0, d1, d2 + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) f35, (double) f38).lightmap(i2, j2).endVertex();
                worldRendererIn.pos(d0, d1, d2).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) f35, (double) f37).lightmap(i2, j2).endVertex();
                worldRendererIn.pos(d0 + 1.0D, d1, d2).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) f36, (double) f37).lightmap(i2, j2).endVertex();
                worldRendererIn.pos(d0 + 1.0D, d1, d2 + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) f36, (double) f38).lightmap(i2, j2).endVertex();
                flag3 = true;
            }
            
            for (int i1 = 0; i1 < 4; ++i1) {
                int j1 = 0;
                int k1 = 0;
                
                if (i1 == 0) {
                    --k1;
                }
                
                if (i1 == 1) {
                    ++k1;
                }
                
                if (i1 == 2) {
                    --j1;
                }
                
                if (i1 == 3) {
                    ++j1;
                }
                
                BlockPos blockpos = blockPosIn.add(j1, 0, k1);
                TextureAtlasSprite textureatlassprite1 = textureatlassprite;

                /*if (!flag)
                {
                    Block block = blockAccess.getBlockState(blockpos).getBlock();

                    if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS)
                    {
                        textureatlassprite1 = atlasSpriteWaterOverlay;
                    }
                }*/
                
                if (aboolean[i1 + 2]) {
                    float f39;
                    float f40;
                    double d3;
                    double d4;
                    double d5;
                    double d6;
                    
                    if (i1 == 0) {
                        f39 = f7;
                        f40 = f10;
                        d3 = d0;
                        d5 = d0 + 1.0D;
                        d4 = d2 + 0.0010000000474974513D;
                        d6 = d2 + 0.0010000000474974513D;
                    }
                    else if (i1 == 1) {
                        f39 = f9;
                        f40 = f8;
                        d3 = d0 + 1.0D;
                        d5 = d0;
                        d4 = d2 + 1.0D - 0.0010000000474974513D;
                        d6 = d2 + 1.0D - 0.0010000000474974513D;
                    }
                    else if (i1 == 2) {
                        f39 = f8;
                        f40 = f7;
                        d3 = d0 + 0.0010000000474974513D;
                        d5 = d0 + 0.0010000000474974513D;
                        d4 = d2 + 1.0D;
                        d6 = d2;
                    }
                    else {
                        f39 = f10;
                        f40 = f9;
                        d3 = d0 + 1.0D - 0.0010000000474974513D;
                        d5 = d0 + 1.0D - 0.0010000000474974513D;
                        d4 = d2;
                        d6 = d2 + 1.0D;
                    }
                    
                    flag3 = true;
                    float f41 = textureatlassprite1.getInterpolatedU(0.0D);
                    float f27 = textureatlassprite1.getInterpolatedU(8.0D);
                    float f28 = textureatlassprite1.getInterpolatedV((double) ((1.0F - f39) * 16.0F * 0.5F));
                    float f29 = textureatlassprite1.getInterpolatedV((double) ((1.0F - f40) * 16.0F * 0.5F));
                    float f30 = textureatlassprite1.getInterpolatedV(8.0D);
                    int j = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos);
                    int k = j >> 16 & 65535;
                    int l = j & 65535;
                    float f31 = i1 < 2 ? 0.8F : 0.6F;
                    float f32 = 1.0F * f31 * f;
                    float f33 = 1.0F * f31 * f1;
                    float f34 = 1.0F * f31 * f2;
                    worldRendererIn.pos(d3, d1 + (double) f39, d4).color(f32, f33, f34, 1.0F).tex((double) f41, (double) f28).lightmap(k, l).endVertex();
                    worldRendererIn.pos(d5, d1 + (double) f40, d6).color(f32, f33, f34, 1.0F).tex((double) f27, (double) f29).lightmap(k, l).endVertex();
                    worldRendererIn.pos(d5, d1 + 0.0D, d6).color(f32, f33, f34, 1.0F).tex((double) f27, (double) f30).lightmap(k, l).endVertex();
                    worldRendererIn.pos(d3, d1 + 0.0D, d4).color(f32, f33, f34, 1.0F).tex((double) f41, (double) f30).lightmap(k, l).endVertex();
                    
                    worldRendererIn.pos(d3, d1 + 0.0D, d4).color(f32, f33, f34, 1.0F).tex((double) f41, (double) f30).lightmap(k, l).endVertex();
                    worldRendererIn.pos(d5, d1 + 0.0D, d6).color(f32, f33, f34, 1.0F).tex((double) f27, (double) f30).lightmap(k, l).endVertex();
                    worldRendererIn.pos(d5, d1 + (double) f40, d6).color(f32, f33, f34, 1.0F).tex((double) f27, (double) f29).lightmap(k, l).endVertex();
                    worldRendererIn.pos(d3, d1 + (double) f39, d4).color(f32, f33, f34, 1.0F).tex((double) f41, (double) f28).lightmap(k, l).endVertex();
                }
            }
            
            return flag3;
        }
    }
    
}