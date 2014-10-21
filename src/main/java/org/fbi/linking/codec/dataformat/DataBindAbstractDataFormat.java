package org.fbi.linking.codec.dataformat;

/**
 * User: zhanrui
 * Date: 13-9-7
 */
public abstract class DataBindAbstractDataFormat implements DataFormat {
    private String[] packages;
    private DataBindAbstractFactory modelFactory;
    private String charset = "GBK";

    public DataBindAbstractDataFormat() {
    }

    public DataBindAbstractDataFormat(String... packages) {
        this.packages = packages;
    }


    public DataBindAbstractFactory getFactory() throws Exception {
        if (modelFactory == null) {
            modelFactory = createModelFactory();
            modelFactory.setCharset(charset);
        }
        return modelFactory;
    }
    
    protected abstract DataBindAbstractFactory createModelFactory() throws Exception;

    //=================================
    public String[] getPackages() {
        return packages;
    }

    public void setPackages(String... packages) {
        this.packages = packages;
    }
    public void setModelFactory(DataBindAbstractFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
