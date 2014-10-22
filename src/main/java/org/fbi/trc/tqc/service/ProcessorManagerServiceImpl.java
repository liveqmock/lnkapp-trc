package org.fbi.trc.tqc.service;


import org.fbi.linking.processor.Processor;
import org.fbi.linking.processor.ProcessorManagerService;

/**
 * User: zhanrui
 * Date: 13-8-22
 * Time: 上午7:49
 */
public class ProcessorManagerServiceImpl implements ProcessorManagerService {
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    public Processor getProcessor(String txnCode) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String[] names = this.getClass().getPackage().getName().split("\\.");
        String className = names[0] + "." + names[1] + "." + names[2] + "." + names[3] + ".processor.T" + txnCode + "Processor";
        System.out.println("==ClassName:" + className);
        Class clazz = Class.forName(className);
        Processor processor = (Processor) clazz.newInstance();

        //TODO
        return processor;
    }
}
