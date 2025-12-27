package dev.rashing.press_f_to_stack.mixin.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class ClientPlayerEntityMixin {
    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void onPacketSend(ClientPlayNetworkHandler handler, Packet packet) {
        if (packet instanceof PlayerActionC2SPacket actionPacket && actionPacket.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            MinecraftClient client = (MinecraftClient) (Object) this;
            ClientPlayerEntity player = client.player;
            if (player != null) {
                ItemStack stackInMain = player.getStackInHand(Hand.MAIN_HAND);
                ItemStack stackInOff = player.getStackInHand(Hand.OFF_HAND);
                if (stackInMain.getItem() == stackInOff.getItem() && !stackInMain.isEmpty()) {
                    short hotbarSlot = (short) (36 + player.getInventory().getSelectedSlot());
                    // Pickup item from hotbar
                    handler.sendPacket(new ClickSlotC2SPacket(
                            player.currentScreenHandler.syncId,
                            player.currentScreenHandler.getRevision(),
                            hotbarSlot,
                            (byte) 0,
                            SlotActionType.PICKUP,
                            Int2ObjectMaps.emptyMap(),
                            ItemStackHash.EMPTY
                    ));
                    // Put in offhand
                    handler.sendPacket(new ClickSlotC2SPacket(
                            player.currentScreenHandler.syncId,
                            player.currentScreenHandler.getRevision(),
                            (short) 45,
                            (byte) 0,
                            SlotActionType.PICKUP,
                            Int2ObjectMaps.emptyMap(),
                            ItemStackHash.EMPTY
                    ));
                    // Put item back in hotbar if it wasn't stacked with offhand
                    handler.sendPacket(new ClickSlotC2SPacket(
                            player.currentScreenHandler.syncId,
                            player.currentScreenHandler.getRevision(),
                            hotbarSlot,
                            (byte) 0,
                            SlotActionType.PICKUP,
                            Int2ObjectMaps.emptyMap(),
                            ItemStackHash.EMPTY
                    ));
                    return;
                }
            }
        }
        handler.sendPacket(packet);
    }
}
