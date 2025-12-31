package me.toastymop.combatlog.mixin;

import me.toastymop.combatlog.CombatConfig;
import me.toastymop.combatlog.util.IEntityDataSaver;
import me.toastymop.combatlog.util.TagData;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"), cancellable = true)
    private void blockItemUseInCombat(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        ServerPlayerEntity player = ((ServerPlayNetworkHandler)(Object)this).player;

        Hand hand = packet.getHand();
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isEmpty()) return;

        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (id == null) return;

        String idString = id.toString();

        if (CombatConfig.Config.blockedItems.contains(idString) && TagData.getCombat((IEntityDataSaver) player)) {
            if (CombatConfig.Config.combatNotice) {
                player.sendMessage(Text.literal(CombatConfig.Config.blockedItemMessage)
                        .fillStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            }

            player.swingHand(hand);

            // Prevent any rapid-click bypass (client respects this immediately)
            player.getItemCooldownManager().set(stack.getItem(), 10);

            ci.cancel();
            player.closeHandledScreen();
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.5f);
        }
    }
}