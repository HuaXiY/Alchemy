package index.alchemy.core.debug;

import java.util.List;

import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Listener;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;

import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.base.Joiner;

import static org.lwjgl.input.Keyboard.KEY_C;
import static org.lwjgl.input.Keyboard.isKeyDown;

@Omega
@Listener
@Hook.Provider
@SideOnly(Side.CLIENT)
public class RTooltip {
    
    @Config(category = "runtime", comment = "Enable item tooltip debug.")
    private static boolean enable_tooltip_debug = false;
    
    @Hook("net.minecraft.potion.PotionEffect#toString")
    public static Hook.Result toString(PotionEffect effect) {
        String result;
        if (effect.getAmplifier() > 0)
            result = effect.getPotion().getRegistryName() + " x " + (effect.getAmplifier() + 1) + ", Duration: " + effect.getDuration();
        else
            result = effect.getPotion().getRegistryName() + ", Duration: " + effect.getDuration();
        if (!effect.doesShowParticles())
            result += ", Particles: false";
        return new Hook.Result(result);
    }
    
    @SubscribeEvent(priority = EventPriority.BOTTOM)
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (enable_tooltip_debug) {
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();
            if (item instanceof ItemFood)
                event.getToolTip().add("Food: " + ((ItemFood) item).getHealAmount(stack) + ", " +
                        ((ItemFood) item).getSaturationModifier(stack));
            if (item instanceof ItemPotion) {
                List<PotionEffect> effects = PotionUtils.getEffectsFromStack(stack);
                if (!effects.isEmpty())
                    event.getToolTip().add("Effect: " +
                            Joiner.on("\n    ").appendTo(new StringBuilder(effects.size() > 1 ? "\n    " : ""), effects).toString());
            }
            for (int id : OreDictionary.getOreIDs(stack))
                event.getToolTip().add(OreDictionary.getOreName(id));
            event.getToolTip().add(item.getClass().getName());
            addNBTToTooltip(stack.serializeNBT(), event.getToolTip(), 1);
            if (GuiScreen.isCtrlKeyDown() && isKeyDown(KEY_C))
                GuiScreen.setClipboardString(GuiScreen.isShiftKeyDown() ? Joiner.on('\n').join(event.getToolTip()) :
                        item.getRegistryName().toString());
        }
    }
    
    public static void addNBTToTooltip(NBTTagCompound nbt, List<String> tooltip, int depth) {
        if (nbt == null)
            return;
        String blank = Tool.makeString(' ', (depth - 1) * 3);
        for (String key : nbt.getKeySet()) {
            NBTBase base = nbt.getTag(key);
            if (base.getId() == NBT.TAG_COMPOUND) {
                tooltip.add(blank + "|-> " + key + " : (" + base.getClass().getSimpleName().replace("NBTTag", "") + ")");
                addNBTToTooltip((NBTTagCompound) base, tooltip, depth + 1);
            }
            else
                tooltip.add(blank + "|-> " + key + " = (" + base.getClass().getSimpleName().replace("NBTTag", "") + ") " + base.toString());
        }
    }
    
}
