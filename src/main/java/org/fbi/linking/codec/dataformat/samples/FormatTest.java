package org.fbi.linking.codec.dataformat.samples;


import org.fbi.linking.codec.dataformat.FixedLengthTextDataFormat;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.codec.dataformat.samples.aicmodel.T2000.TIA2000;
import org.fbi.linking.codec.dataformat.samples.staringmodel.T1000.TIA1000;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-8
 * Time: 上午10:53
 */
public class FormatTest {

    public static void main(String... argv) throws Exception {
        //分隔符报文测试
        //解包
        String tiaStr = "1000|20130910|111|aaa|3|99999,a|88888,b|77777,c";
        TIA1000 tia1000 = new TIA1000();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia1000.getClass().getPackage().getName());
        tia1000 = (TIA1000) dataFormat.fromMessage(tiaStr, "TIA1000");
        System.out.println(tia1000.getId());
        System.out.println(tia1000.getName());

        //打包
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(tia1000.getClass().getName(), tia1000);
        modelObjectsMap.put(tia1000.getHeader().getClass().getName(), tia1000.getHeader());
        String result = (String) dataFormat.toMessage(modelObjectsMap);
        System.out.println(result);


        //固定长度报文测试
        //解包
        tiaStr = "1 22  2 4   汗6   77";
        TIA2000 tia2000 = new TIA2000();
        FixedLengthTextDataFormat dataFormat1 = new FixedLengthTextDataFormat(tia2000.getClass().getPackage().getName());
        tia2000 = (TIA2000) dataFormat1.fromMessage(tiaStr.getBytes("GBK"), "TIA2000");
        System.out.println("===T2000===" + tia2000.getId());
        System.out.println("===T2000===" + tia2000.getName());

        //打包
        modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(tia2000.getClass().getName(), tia2000);
        result = (String) dataFormat1.toMessage(modelObjectsMap);
        System.out.println("===T2000===" + result);
    }
}
