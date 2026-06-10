package com.xy.easymagic.mixin;

import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.capability.EasyMagicItemHandler;
import com.xy.easymagic.client.EnchantedItemRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityEnchantmentTableRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnchantmentTableRenderer.class)
public abstract class MixinTESREnchantmentTable {

    @Inject(method = "render", at = @At("RETURN"))
    private void easymagic$renderCapabilityItems(
            TileEntityEnchantmentTable te, double x, double y, double z,
            float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        float time = (float) te.tickCount + partialTicks;

        ItemStack itemToEnchant = ItemStack.EMPTY;
        ItemStack lapisStack = ItemStack.EMPTY;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.player.openContainer instanceof IEasyMagicContainer) {
            IEasyMagicContainer emc = (IEasyMagicContainer) mc.player.openContainer;
            if (te.getPos().equals(emc.easymagic$getPosition())) {
                itemToEnchant = emc.easymagic$getTableItem();
                lapisStack = emc.easymagic$getLapisItem();
            }
        }

        if (itemToEnchant.isEmpty() && lapisStack.isEmpty()) {
            IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (cap instanceof EasyMagicItemHandler) {
                itemToEnchant = ((EasyMagicItemHandler) cap).getStackInSlot(0);
                lapisStack = ((EasyMagicItemHandler) cap).getStackInSlot(1);
            }
        }

        if (!itemToEnchant.isEmpty()) {
            EnchantedItemRenderer.renderHoveringItem(itemToEnchant, x, y, z, time,
                    partialTicks, te.bookSpread, te.bookSpreadPrev);
        }

        if (!lapisStack.isEmpty()) {
            int count = Math.min(lapisStack.getCount(), 4);
            EnchantedItemRenderer.renderOrbitingLapis(lapisStack, x, y, z, time, count);
        }
    }
}
