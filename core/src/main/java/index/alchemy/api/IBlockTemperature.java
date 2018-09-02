package index.alchemy.api;

import java.util.Map;
import java.util.function.Function;

import index.alchemy.util.Tool;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.collect.Maps;

public interface IBlockTemperature {
    
    class TemperatureMap implements Function<IBlockState, Float> {
        
        public Map<IBlockState, Float> mapping = Maps.newHashMap();
        public Function<IBlockState, Float> handle = i -> 0F;
        
        @Override
        public Float apply(IBlockState key) {
            return Tool.isNullOr(mapping.get(key), () -> handle.apply(key));
        }
        
    }
    
    void setBlockTemperature(IBlockState state, float temperature);
    
    void setBlockTemperature(Function<IBlockState, Float> handle);
    
    float getBlockTemperature(EntityPlayer player, IBlockState state);
    
}
