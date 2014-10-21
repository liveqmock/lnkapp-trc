package org.fbi.trc.tqc.internal;

import org.fbi.linking.processor.ProcessorManagerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

public class AppActivator implements BundleActivator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static BundleContext context;

    public static BundleContext getBundleContext() {
        return context;
    }

    public void start(BundleContext context) {
        AppActivator.context = context;

        ProcessorFactory factory = new ProcessorFactory();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("APPID", "TRCTQC");
        context.registerService(ProcessorManagerService.class.getName(), factory, properties);

        logger.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - Starting the TRC(TXN RISK CONTROL) app bundle...." );
    }

    public void stop(BundleContext context) throws Exception {
        AppActivator.context = null;
        logger.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - Stopping the TRC(TXN RISK CONTROL) app bundle...");
    }

}
