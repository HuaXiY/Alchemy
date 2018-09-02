package index.alchemy.item;

import javax.annotation.Nullable;

import index.alchemy.api.IColorItem;
import index.alchemy.api.IItemMeshProvider;
import index.alchemy.api.IRegister;
import index.alchemy.api.IResourceLocation;
import index.alchemy.block.AlchemyBlockSlab;
import index.alchemy.util.SideHelper;
import index.project.version.annotation.Beta;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
public class AlchemyItemBlock extends ItemBlock implements IColorItem, IResourceLocation, IItemMeshProvider, IRegister {
    
    @SideOnly(Side.CLIENT)
    protected IItemColor color;
    
    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor getItemColor() {
        return new IItemColor() {
            
            @Override
            public int colorMultiplier(ItemStack item, int index) {
                return color == null ? -1 : color.colorMultiplier(item, index);
            }
            
        };
    }
    
    @Override
    public ResourceLocation getResourceLocation() {
        return block instanceof IResourceLocation ? ((IResourceLocation) block).getResourceLocation() : getRegistryName();
    }
    
    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public ItemMeshDefinition getItemMesh() {
        return block instanceof IItemMeshProvider ? ((IItemMeshProvider) block).getItemMesh() : null;
    }
    
    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation[] getItemVariants() {
        return block instanceof IItemMeshProvider ? ((IItemMeshProvider) block).getItemVariants() : null;
    }
    
    public String getUnlocalizedName(ItemStack stack) {
        return block instanceof BlockSlab ? (((BlockSlab) block).getTranslationKey(stack.getMetadata())) : super.getTranslationKey(stack);
    }
    
    @Override
    public int getMetadata(int meta) {
        return meta;
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (block instanceof AlchemyBlockSlab && !((BlockSlab) block).isDouble()) {
            if (stack.getCount() != 0 && player.canPlayerEdit(pos.offset(facing), facing, stack)) {
                BlockSlab singleSlab = (BlockSlab) block;
                Comparable<?> comparable = singleSlab.getTypeForItem(stack);
                IBlockState iblockstate = world.getBlockState(pos);
                if (iblockstate.getBlock() == singleSlab) {
                    IProperty<?> iproperty = singleSlab.getVariantProperty();
                    Comparable<?> comparable1 = iblockstate.getValue(iproperty);
                    BlockSlab.EnumBlockHalf blockslab$enumblockhalf = (BlockSlab.EnumBlockHalf) iblockstate.getValue(BlockSlab.HALF);
                    if ((facing == EnumFacing.UP && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM ||
                            facing == EnumFacing.DOWN && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP) &&
                            comparable1 == comparable) {
                        IBlockState iblockstate1 = makeSlabState(iproperty, comparable1);
                        AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(world, pos);
                        if (axisalignedbb != Block.NULL_AABB && world.checkNoEntityCollision(axisalignedbb.offset(pos)) &&
                                world.setBlockState(pos, iblockstate1, 11)) {
                            SoundType soundtype = block.getSoundType(iblockstate1, world, pos, player);
                            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            stack.setCount(stack.getCount() - 1);
                        }
                        return EnumActionResult.SUCCESS;
                    }
                }
                return tryPlace(player, stack, world, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS :
                        super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
            }
            else
                return EnumActionResult.FAIL;
        }
        else
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        if (block instanceof AlchemyBlockSlab && !((BlockSlab) block).isDouble()) {
            BlockSlab singleSlab = (BlockSlab) block;
            BlockPos blockpos = pos;
            IProperty<?> iproperty = singleSlab.getVariantProperty();
            Comparable<?> comparable = singleSlab.getTypeForItem(stack);
            IBlockState iblockstate = worldIn.getBlockState(pos);
            if (iblockstate.getBlock() == singleSlab) {
                boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
                if ((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag) && comparable == iblockstate.getValue(iproperty))
                    return true;
            }
            pos = pos.offset(side);
            IBlockState iblockstate1 = worldIn.getBlockState(pos);
            return iblockstate1.getBlock() == singleSlab && comparable == iblockstate1.getValue(iproperty) ?
                    true : super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
        }
        else
            return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
    }
    
    private boolean tryPlace(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos, Object itemSlabType) {
        if (block instanceof AlchemyBlockSlab && !((BlockSlab) block).isDouble()) {
            BlockSlab singleSlab = (BlockSlab) block;
            IBlockState iblockstate = worldIn.getBlockState(pos);
            if (iblockstate.getBlock() == singleSlab) {
                Comparable<?> comparable = iblockstate.getValue(singleSlab.getVariantProperty());
                if (comparable == itemSlabType) {
                    IBlockState iblockstate1 = makeSlabState(singleSlab.getVariantProperty(), comparable);
                    AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
                    if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) &&
                            worldIn.setBlockState(pos, iblockstate1, 11)) {
                        SoundType soundtype = singleSlab.getSoundType(iblockstate1, worldIn, pos, player);
                        worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                        stack.setCount(stack.getCount() - 1);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> IBlockState makeSlabState(IProperty<T> property, Comparable<?> comparable) {
        return ((AlchemyBlockSlab) block).getDoubleBlock().getDefaultState().withProperty(property, (T) comparable);
    }
    
    public AlchemyItemBlock(Block block) {
        super(block);
        if (SideHelper.runOnClient()) {
            setCreativeTab(block.getCreativeTab());
            if (block instanceof IColorItem)
                color = ((IColorItem) block).getItemColor();
        }
    }
    
}
