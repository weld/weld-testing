package org.jboss.weld.junit.jupiter.extensionInjection;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

@ApplicationScoped
public class MapProducer {

    @Produces
    @Dependent
    public <T> Map<String, T> produceMap(InjectionPoint ip) {
        Map<String, T> map = new HashMap<>();
        map.put(ip.getType().getTypeName(), null);
        return map;
    }
}
