package org.fbi.trc.tqc.common.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: zhanrui
 * Date: 2010-11-1
 * Time: 14:47:13
 */
public class Ibator {
    public static void main(String[] argv) throws Exception {
        Ibator ibator = new Ibator();
        ibator.run("resources/generatorConfig.xml");
    }

    public void run(String configfile) throws Exception {
        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        String path = this.getClass().getResource("").getPath();
        File configFile = new File(path + configfile);
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = null;
        config = cp.parseConfiguration(configFile);

        DefaultShellCallback callback = new DefaultShellCallback(overwrite);

        MyBatisGenerator myBatisGenerator = null;
        myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        for (String warning : warnings) {
            System.out.println(warning);
        }

    }
}
