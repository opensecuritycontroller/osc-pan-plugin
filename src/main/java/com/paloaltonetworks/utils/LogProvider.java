package com.paloaltonetworks.utils;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * This component Takes the properly initialized {@link ILoggerFactory}
 * service from OSGi uses it to create {@link Logger} objects for other
 * classes via a static {@code getLogger} method.
 *
 * @see org.osc.core.broker.util.log
 * @see org.osc.core.server.Server
 */
@Component
public class LogProvider {
    private static ILoggerFactory loggerFactory;

    @Reference
    public void setLoggerFactoryInst(ILoggerFactory instance) {
        setLoggerFactory(instance);
    }

    public static Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Attempt to get logger for null class!!");
        }

        return new LoggerProxy(clazz.getName());
    }

    public static ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    private static void setLoggerFactory(ILoggerFactory instance) {
        loggerFactory = instance;
    }
}
