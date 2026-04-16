package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLSensors {
    public static final DeferredRegister<SensorType<?>> SENSORS = DeferredRegister.create(Registries.SENSOR_TYPE, NoMansLand.MODID);

    private static <U extends Sensor<?>> Supplier<SensorType<U>> register(String key, Supplier<U> sensorSupplier) {
        return SENSORS.register(key, () -> new SensorType<>(sensorSupplier));
    }
}
