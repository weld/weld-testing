package org.jboss.weld.junit5.extensionInjection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
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
