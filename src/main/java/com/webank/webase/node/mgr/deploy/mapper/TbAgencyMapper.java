package com.webank.webase.node.mgr.deploy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

import com.webank.webase.node.mgr.deploy.entity.TbAgency;

public interface TbAgencyMapper {

    @Delete({
            "delete from tb_agency where chain_id = #{chainId,jdbcType=INTEGER}"
    })
    int deleteByChainId(@Param("chainId") Integer chainId);

    @Select({
            "select id,agency_name,agency_desc,chain_id,chain_name,create_time,modify_time from tb_agency where chain_id = #{chainId,jdbcType=INTEGER}"
    })
    List<TbAgency> selectByChainId(@Param("chainId") int chainId);

    @Select({
            "select id,agency_name,agency_desc,chain_id,chain_name,create_time,modify_time from tb_agency where chain_id = #{chainId,jdbcType=INTEGER} and agency_name = #{agencyName,jdbcType=VARCHAR}"
    })
    TbAgency getByChainIdAndAgencyName(@Param("chainId") int chainId, @Param("agencyName") String agencyName);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_agency
     *
     * @mbg.generated
     */
    @Delete({
        "delete from tb_agency where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(@Param("id") Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_agency
     *
     * @mbg.generated
     */
    @InsertProvider(type=TbAgencySqlProvider.class, method="insertSelective")
    @SelectKey(statement="SELECT currval(id)", keyProperty="id", before=false, resultType=Integer.class)
    int insertSelective(TbAgency record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_agency
     *
     * @mbg.generated
     */
    @Select({
        "select id, agency_name, agency_desc, chain_id, chain_name, create_time, modify_time from tb_agency where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="agency_name", property="agencyName", jdbcType=JdbcType.VARCHAR),
        @Result(column="agency_desc", property="agencyDesc", jdbcType=JdbcType.VARCHAR),
        @Result(column="chain_id", property="chainId", jdbcType=JdbcType.INTEGER),
        @Result(column="chain_name", property="chainName", jdbcType=JdbcType.VARCHAR),
        @Result(column="create_time", property="createTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modify_time", property="modifyTime", jdbcType=JdbcType.TIMESTAMP)
    })
    TbAgency selectByPrimaryKey(@Param("id") Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_agency
     *
     * @mbg.generated
     */
    @UpdateProvider(type=TbAgencySqlProvider.class, method="updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(TbAgency record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_agency
     *
     * @mbg.generated
     */
    //TODO 无调用 psql不支持<script> 直接注释
    //@Options(useGeneratedKeys = true,keyProperty="id",keyColumn = "id")
    //@Insert({
    //"<script>",
    //    "insert into tb_agency (agency_name, ",
    //    "agency_desc, chain_id, ",
    //    "chain_name, create_time, ",
    //    "modify_time)",
    //    "values<foreach collection=\"list\" item=\"detail\" index=\"index\" separator=\",\">(#{detail.agencyName,jdbcType=VARCHAR}, ",
    //    "#{detail.agencyDesc,jdbcType=VARCHAR}, #{detail.chainId,jdbcType=INTEGER}, ",
    //    "#{detail.chainName,jdbcType=VARCHAR}, #{detail.createTime,jdbcType=TIMESTAMP}, ",
    //    "#{detail.modifyTime,jdbcType=TIMESTAMP})</foreach></script>",
    //})
    //int batchInsert(java.util.List<TbAgency> list);

}