package com.backslashashley.incandescent.mixin.world.chunk;

import net.minecraft.world.chunk.ChunkNibbleStorage;
import net.minecraft.world.chunk.WorldChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes MC-80966
 * @author Angeline (@jellysquid)
 * @author Ashley
 */
@Mixin(WorldChunkSection.class)
public class MixinWorldChunkSection {
	@Shadow private int nonAirBlockCount;
	@Shadow private ChunkNibbleStorage blockLight;
	@Shadow private ChunkNibbleStorage skyLight;

	@Unique private int lightRefCount = -1;

	@Inject(method = "setSkyLight", at = @At("TAIL"))
	public void setSkyLight(CallbackInfo ci) {
		lightRefCount = -1;
	}

	@Inject(method = "setBlockLight", at = @At("TAIL"))
	public void setBlockLight(CallbackInfo ci) {
		lightRefCount = -1;
	}

	@Inject(method = "setBlockLightStorage", at = @At("TAIL"))
	public void setBlockLightStorage(CallbackInfo ci) {
		lightRefCount = -1;
	}

	@Inject(method = "setSkyLightStorage", at = @At("TAIL"))
	public void setSkyLightStorage(CallbackInfo ci) {
		lightRefCount = -1;
	}


	@Inject(method = "isEmpty", at=@At("HEAD"), cancellable = true)
	public void isEmpty(CallbackInfoReturnable cir) {
		if (nonAirBlockCount != 0) {
			cir.setReturnValue(false);
		}
		if (lightRefCount == -1) {
			if (compareLightArray(skyLight, (byte) 255) && compareLightArray(blockLight, (byte) 255)) {
				lightRefCount = 0;
			} else {
				lightRefCount = 1;
			}
		}
		if (lightRefCount == 0) {
			cir.setReturnValue(true);
		}
		else {
			cir.setReturnValue(false);
		}
	}

	@Unique
	private boolean compareLightArray(final ChunkNibbleStorage storage, final byte target) {
		if (storage == null) {
			return true;
		}

		for (final byte currentByte : storage.getData()) {
			if (currentByte != target) {
				return false;
			}
		}
		return true;
	}
}
