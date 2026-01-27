package com.farcr.nomansland.client.particle;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class MoonlightRayParticleOptions implements ParticleOptions
{
	@Override
	public ParticleType<?> getType()
	{
		return NMLParticleTypes.MOONLIGHT_RAY.get();
	}
}
