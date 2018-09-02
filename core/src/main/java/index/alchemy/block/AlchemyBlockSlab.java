package index.alchemy.block;

import java.util.Random;
import javax.annotation.Nullable;

import index.alchemy.api.IRegister;
import index.project.version.annotation.Beta;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Beta
public abstract class AlchemyBlockSlab extends BlockSlab implements IRegister {
    
    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.<Variant>create("variant", Variant.class);
    
    public AlchemyBlockSlab(Material material, String name) {
        super(material);
        int index = name.lastIndexOf(':');
        setTranslationKey(index == -1 ? name : name.substring(index + 1));
        setRegistryName(name);
        setCreativeTab(getCreativeTab());
        setLightOpacity(isDouble() ? 255 : 0);
        register();
    }
    
    public abstract Block getHalfBlock();
    
    public abstract Block getDoubleBlock();
    
    @Nullable
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(getHalfBlock());
    }
    
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(getHalfBlock());
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        if (!this.isDouble())
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        return iblockstate;
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP)
            i |= 8;
        return i;
    }
    
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState iblockstate = getStateFromMeta(meta);
        if (!isDouble())
            iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        return this.isDouble() ? iblockstate : (facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP));
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, new IProperty[]{VARIANT}) :
                new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
    }
    
    @Override
    public String getTranslationKey(int meta) {
        return getTranslationKey(meta);
    }
    
    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }
    
    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return AlchemyBlockSlab.Variant.DEFAULT;
    }
    
    public static enum Variant implements IStringSerializable {
        
        DEFAULT;
        
        @Override
        public String getName() {
            return "default";
        }
        
    }
    
}
