package index.alchemy.client.render;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import index.alchemy.client.RenderHelper;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Lists;

public class Rectangle<T extends Rectangle.IScaledResolutionProvider> {
    
    public static interface IScaledResolutionProvider {
        
        ScaledResolution scaledResolution();
        
    }
    
    public static enum ScaleType {
        
        X, Y, XY_MAX, XY_MIN, SELF
        
    }
    
    public static class Group<T extends Rectangle.IScaledResolutionProvider> extends Rectangle<T> {
        
        public Group(float offsetX, float offsetY, ScaleType scaleType) {
            super(render -> null, 0, 0, 0, 0, 0, 1, 1, offsetX, offsetY, scaleType);
        }
        
        public float scaleX(T render) { return locals.isEmpty() ? 1.0F : (float) (render.scaledResolution().getScaledWidth_double() / locals.get(0).texLocalXSize); }
        
        public float scaleY(T render) { return locals.isEmpty() ? 1.0F : (float) (render.scaledResolution().getScaledHeight_double() / locals.get(0).texLocalYSize); }
        
        @Override
        public void doRender(T render, float parentOffsetX, float parentOffsetY) {
            doRenderSub(render, realOffsetX(render, realScaleX(render)) + parentOffsetX, realOffsetY(render, realScaleY(render)) + parentOffsetY);
        }
        
    }
    
    public Function<T, ResourceLocation> texLocation;
    public float x1, x2, y1, y2, texSize, texLocalXSize, texLocalYSize;
    public float offsetX, offsetY;
    public ScaleType scaleType;
    
    public boolean hasParent;
    
    public boolean hasParent() {
        return hasParent;
    }
    
    public void setHasParent() {
        hasParent = true;
    }
    
    public List<Rectangle<T>> locals = Lists.newArrayList();
    
    @SafeVarargs
    public final Rectangle<T> add(Rectangle<T>... rectangles) {
        Stream.of(rectangles)
                .filter(this::onAdd)
                .peek(Rectangle::setHasParent)
                .forEach(locals::add);
        return this;
    }
    
    public boolean onAdd(Rectangle<T> rectangle) {
        return true;
    }
    
    public Rectangle(Function<T, ResourceLocation> texLocation, float x1, float x2, float y1, float y2, float texSize, float texLocalXSize,
                     float texLocalYSize, float offsetX, float offsetY, ScaleType scaleType) {
        this.texLocation = texLocation;
        this.x1 = x1;
        this.x2 = x2 + 1;
        this.y1 = y1;
        this.y2 = y2 + 1;
        this.texSize = texSize;
        this.texLocalXSize = texLocalXSize;
        this.texLocalYSize = texLocalYSize;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scaleType = scaleType;
    }
    
    public Rectangle(ResourceLocation texLocation, float x1, float x2, float y1, float y2, float texSize, float texLocalXSize, float texLocalYSize,
                     float offsetX, float offsetY, ScaleType scaleType) {
        this(render -> texLocation, x1, x2, y1, y2, texSize, texLocalXSize, texLocalYSize, offsetX, offsetY, scaleType);
    }
    
    public float x1(T render) { return x1; }
    
    public float x2(T render) { return x2; }
    
    public float y1(T render) { return y1; }
    
    public float y2(T render) { return y2; }
    
    public float realX1(T render) { return x1(render); }
    
    public float realX2(T render) { return x2(render); }
    
    public float realY1(T render) { return y1(render); }
    
    public float realY2(T render) { return y2(render); }
    
    public float offsetX(T render) { return offsetX; }
    
    public float offsetY(T render) { return offsetY; }
    
    public float realOffsetX(T render, float realScaleX) {
        float result = offsetX(render) * realScaleX;
        return (float) (result >= 0 || hasParent ? result : result + render.scaledResolution().getScaledWidth_double() + 1);
    }
    
    public float realOffsetY(T render, float realScaleY) {
        float result = offsetY(render) * realScaleY;
        return (float) (result >= 0 || hasParent ? result : result + render.scaledResolution().getScaledHeight_double() + 1);
    }
    
    public float scaleX(T render) { return (float) (render.scaledResolution().getScaledWidth_double() / (texLocalXSize)); }
    
    public float scaleY(T render) { return (float) (render.scaledResolution().getScaledHeight_double() / (texLocalYSize)); }
    
    public float realScaleX(T render) {
        switch (scaleType) {
            case X:
                return scaleX(render);
            case Y:
                return scaleY(render);
            case XY_MAX:
                return Math.max(scaleX(render), scaleY(render));
            case XY_MIN:
                return Math.min(scaleX(render), scaleY(render));
            default:
                return scaleX(render);
        }
    }
    
    public float realScaleY(T render) {
        switch (scaleType) {
            case X:
                return scaleX(render);
            case Y:
                return scaleY(render);
            case XY_MAX:
                return Math.max(scaleX(render), scaleY(render));
            case XY_MIN:
                return Math.min(scaleX(render), scaleY(render));
            default:
                return scaleY(render);
        }
    }
    
    public ResourceLocation texLocation(T render) {
        return texLocation.apply(render);
    }
    
    public void bind(T render) {
        HUDManager.bind(texLocation(render));
    }
    
    public boolean shouldRender() {
        return true;
    }
    
    public void render(T render) {
        render(render, 0, 0);
    }
    
    public void render(T render, float parentOffsetX, float parentOffsetY) {
        if (!shouldRender())
            return;
        doRender(render, parentOffsetX, parentOffsetY);
    }
    
    public void doRender(T render, float parentOffsetX, float parentOffsetY) {
        bind(render);
        float realScaleX = realScaleX(render), realScaleY = realScaleY(render);
        drawModalRectWithCustomSizedTexture(
                parentOffsetX = realOffsetX(render, realScaleX) + parentOffsetX,
                parentOffsetY = realOffsetY(render, realScaleY) + parentOffsetY,
                realX1(render) * realScaleX, realY1(render) * realScaleY,
                (realX2(render) - realX1(render)) * realScaleX, (realY2(render) - realY1(render)) * realScaleY,
                texSize * realScaleX, texSize * realScaleY);
        doRenderSub(render, parentOffsetX, parentOffsetY);
    }
    
    public void doRenderSub(T render, float parentOffsetX, float parentOffsetY) {
        if (!locals.isEmpty())
            locals.forEach(rectangle -> rectangle.render(render, parentOffsetX, parentOffsetY));
    }
    
    protected static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height,
                                                              float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        bufferBuilder.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        bufferBuilder.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        bufferBuilder.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }
    
    protected static void drawRect(float left, float top, float width, float height, int color) {
        float right = left + width, bottom = top + height;
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderHelper.disableTexture2D();
        RenderHelper.color(r, g, b, a);
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferBuilder.pos(left, bottom, 0.0D).endVertex();
        bufferBuilder.pos(right, bottom, 0.0D).endVertex();
        bufferBuilder.pos(right, top, 0.0D).endVertex();
        bufferBuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        RenderHelper.enableTexture2D();
        RenderHelper.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
}