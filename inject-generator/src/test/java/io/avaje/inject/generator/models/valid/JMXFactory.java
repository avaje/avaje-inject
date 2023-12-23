package io.avaje.inject.generator.models.valid;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class JMXFactory {

    @Bean
    MemoryMXBean memoryMXBean(){
        return ManagementFactory.getMemoryMXBean();
    }

    @Bean
    ClassLoadingMXBean classLoadingMXBean(){
        return ManagementFactory.getClassLoadingMXBean();
    }

}