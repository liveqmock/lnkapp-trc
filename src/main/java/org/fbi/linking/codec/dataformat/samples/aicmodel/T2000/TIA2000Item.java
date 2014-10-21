package org.fbi.linking.codec.dataformat.samples.aicmodel.T2000;


import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManyFixedLengthTextMessage;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-10
 * Time: ÏÂÎç5:44
 */
@OneToManyFixedLengthTextMessage
public class TIA2000Item {
    @DataField(seq = 1,length = 2)
    private String itemNo;

    @DataField(seq = 2, length = 4)
    private String itemName;


    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
