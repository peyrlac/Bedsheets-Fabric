package be.pierrelac.bedsheets.mixin;

import be.pierrelac.bedsheets.event.SleepHooks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to track when players start and stop sleeping.
 * Integrates with the sleep hooks for health regeneration and messiness.
 */
@Mixin(PlayerEntity.class)
public class PlayerSleepMixin {
    
    /**
     * Inject when a player tries to sleep to track sleep start.
     */
    @Inject(method = "trySleep", at = @At(value = "RETURN", ordinal = 0))
    private void bedsheets$onSleepStart(BlockPos pos, CallbackInfoReturnable<PlayerEntity.SleepFailureReason> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        
        // Only handle on server side and when sleep was successful
        if (self.getWorld().isClient() || cir.getReturnValue() != PlayerEntity.SleepFailureReason.NOT_POSSIBLE_NOW) {
            return;
        }
        
        if (self instanceof ServerPlayerEntity serverPlayer) {
            // Use improved method that automatically gets messiness from bed
            SleepHooks.onSleepStart(serverPlayer, pos);
        }
    }
    
    /**
     * Inject when a player wakes up to track sleep end.
     */
    @Inject(method = "wakeUp(ZZ)V", at = @At("HEAD"))
    private void bedsheets$onWakeUp(boolean bl, boolean bl2, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        
        // Only handle on server side
        if (self.getWorld().isClient()) {
            return;
        }
        
        if (self instanceof ServerPlayerEntity serverPlayer) {
            SleepHooks.onWake(serverPlayer);
        }
    }
}