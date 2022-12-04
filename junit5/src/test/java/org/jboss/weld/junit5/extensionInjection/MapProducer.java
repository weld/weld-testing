package org.jboss.weld.junit5.extensionInjection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MapProducer {

    @Produces
    @Dependent
    public <T> Map<String,T> produceMap(InjectionPoint ip) {
        Map<String, T> map = new HashMap<>();
        map.put(ip.getType().getTypeName(), null);
        return map;
    }
}
