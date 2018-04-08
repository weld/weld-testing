package org.jboss.weld.junit5.auto;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator.Builder;
import org.jboss.weld.junit5.WeldJunitEnricher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import static org.jboss.weld.junit5.WeldJunit5Extension.getExplicitInjectionInfoFromStore;



public class AutoWeldJUnitEnricher implements WeldJunitEnricher {

  @Override
  public void enrich(ExtensionContext context, Weld weld, Builder weldInitiatorBuilder, Object testInstance) {

    Class<?> testClass = testInstance.getClass();

    weld.addAlternativeStereotype(OverrideBean.class);

    ClassScanning.scanForRequiredBeanClass(testClass, weld, getExplicitInjectionInfoFromStore(context));

    weld.addBeanClass(testClass);
    weld.addExtension(new TestExtension(testClass, testInstance));

    AnnotationSupport.findRepeatableAnnotations(testClass, ActivateScopes.class)
        .forEach(ann -> weldInitiatorBuilder.activate(ann.value()));
  }

}
