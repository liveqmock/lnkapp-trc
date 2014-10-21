package org.fbi.trc.tqc.enums;

/**
 * 枚举
 */
public interface EnumApp {
    public String getCode();
    public String getTitle();
    public int ordinal();

    @Override
    public String toString();
}
