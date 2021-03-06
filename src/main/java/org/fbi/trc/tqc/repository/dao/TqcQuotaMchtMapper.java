package org.fbi.trc.tqc.repository.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.fbi.trc.tqc.repository.model.TqcQuotaMcht;
import org.fbi.trc.tqc.repository.model.TqcQuotaMchtExample;
import org.fbi.trc.tqc.repository.model.TqcQuotaMchtKey;

public interface TqcQuotaMchtMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int countByExample(TqcQuotaMchtExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int deleteByExample(TqcQuotaMchtExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int deleteByPrimaryKey(TqcQuotaMchtKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int insert(TqcQuotaMcht record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int insertSelective(TqcQuotaMcht record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    List<TqcQuotaMcht> selectByExample(TqcQuotaMchtExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    TqcQuotaMcht selectByPrimaryKey(TqcQuotaMchtKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int updateByExampleSelective(@Param("record") TqcQuotaMcht record, @Param("example") TqcQuotaMchtExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int updateByExample(@Param("record") TqcQuotaMcht record, @Param("example") TqcQuotaMchtExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int updateByPrimaryKeySelective(TqcQuotaMcht record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_QUOTA_MCHT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    int updateByPrimaryKey(TqcQuotaMcht record);
}