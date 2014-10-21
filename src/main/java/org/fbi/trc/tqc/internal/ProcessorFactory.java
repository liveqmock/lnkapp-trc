package org.fbi.trc.tqc.internal;

import org.fbi.trc.tqc.service.ProcessorManagerServiceImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * User: zhanrui
 * Date: 13-8-22
 * Time: 上午7:44
 */
public class ProcessorFactory implements ServiceFactory<ProcessorManagerServiceImpl> {
    @Override
    public ProcessorManagerServiceImpl getService(Bundle bundle, ServiceRegistration<ProcessorManagerServiceImpl> registration) {
        System.out.println(bundle.getSymbolicName() + "已获取服务。 ");
        return new ProcessorManagerServiceImpl();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration<ProcessorManagerServiceImpl> registration, ProcessorManagerServiceImpl service) {
        System.out.println(bundle.getSymbolicName() + "已释放服务。");
    }
}
