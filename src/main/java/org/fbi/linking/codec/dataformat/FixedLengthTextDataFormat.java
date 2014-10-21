package org.fbi.linking.codec.dataformat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-7
 */
public class FixedLengthTextDataFormat extends DataBindAbstractDataFormat {
    private static final transient Logger logger = LoggerFactory.getLogger(FixedLengthTextDataFormat.class);

    public FixedLengthTextDataFormat() {
    }

    public FixedLengthTextDataFormat(String... packages) {
        super(packages);
    }

    @SuppressWarnings("unchecked")
    public Object toMessage(Object req) throws Exception {
        FixedLengthTextDataBindFactory factory = (FixedLengthTextDataBindFactory)getFactory();
        Map<String, Object> modelsMap = (Map<String, Object>)req;

        return factory.unbind(modelsMap);
    }

    public Object fromMessage(Object req, String requestBeanName) throws Exception {
        String packageName = getPackages()[0];
        return ((Map)fromMessage(req)).get(packageName + "." + requestBeanName);
    }

    public Object fromMessage(Object req) throws Exception {
        FixedLengthTextDataBindFactory factory = (FixedLengthTextDataBindFactory)getFactory();
        Map<String, Object> modelMap = factory.factory();

/*
        String separator = factory.getSeparator();
        String[] tokens = ((String)req).split(separator, -1);
        List<String> result = Arrays.asList(tokens);
        if (result.size() == 0 || result.isEmpty()) {
            throw new IllegalArgumentException("Request String is empty!");
        }

*/
        factory.bind((byte[])req, modelMap);
        factory.link(modelMap);
        return modelMap;
    }

    @Override
    protected DataBindAbstractFactory createModelFactory() throws Exception {
        return new FixedLengthTextDataBindFactory(getPackages());
    }
}

