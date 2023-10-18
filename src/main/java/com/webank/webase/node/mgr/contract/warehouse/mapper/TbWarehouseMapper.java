package com.webank.webase.node.mgr.contract.warehouse.mapper;

import com.webank.webase.node.mgr.contract.warehouse.entity.TbWarehouse;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

/**
 * @author marsli
 */
public interface TbWarehouseMapper {

    @Select({ "select id,warehouse_name,warehouse_name_en,type,create_time,modify_time,warehouse_icon,description,description_en,warehouse_detail,warehouse_detail_en from tb_warehouse" })
    List<TbWarehouse> findAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_warehouse
     *
     * @mbg.generated
     */
    @Delete({ "delete from tb_warehouse where id = #{id,jdbcType=INTEGER}" })
    int deleteByPrimaryKey(@Param("id") Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_warehouse
     *
     * @mbg.generated
     */
    @InsertProvider(type = TbWarehouseSqlProvider.class, method = "insertSelective")
    int insertSelective(TbWarehouse record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_warehouse
     *
     * @mbg.generated
     */
    @Select({ "select id, warehouse_name, warehouse_name_en, type, create_time, modify_time, warehouse_icon, description, description_en, warehouse_detail, warehouse_detail_en from tb_warehouse where id = #{id,jdbcType=INTEGER}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "warehouse_name", property = "warehouseName", jdbcType = JdbcType.VARCHAR), @Result(column = "warehouse_name_en", property = "warehouseNameEn", jdbcType = JdbcType.VARCHAR), @Result(column = "type", property = "type", jdbcType = JdbcType.INTEGER), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "warehouse_icon", property = "warehouseIcon", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "description", property = "description", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "description_en", property = "descriptionEn", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "warehouse_detail", property = "warehouseDetail", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "warehouse_detail_en", property = "warehouseDetailEn", jdbcType = JdbcType.LONGVARCHAR) })
    TbWarehouse selectByPrimaryKey(@Param("id") Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_warehouse
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbWarehouseSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(TbWarehouse record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_warehouse
     *
     * @mbg.generated
     */
    @Options(useGeneratedKeys = false, keyProperty = "id", keyColumn = "id")
    @Insert({"insert into tb_warehouse (id,warehouse_name, warehouse_name_en,type, create_time,modify_time, warehouse_icon,description, description_en,warehouse_detail, warehouse_detail_en) values(#{detail.id,jdbcType=INTEGER}, #{detail.warehouseName,jdbcType=VARCHAR}, #{detail.warehouseNameEn,jdbcType=VARCHAR}, #{detail.type,jdbcType=INTEGER}, #{detail.createTime,jdbcType=TIMESTAMP}, #{detail.modifyTime,jdbcType=TIMESTAMP}, #{detail.warehouseIcon,jdbcType=LONGVARCHAR}, #{detail.description,jdbcType=LONGVARCHAR}, #{detail.descriptionEn,jdbcType=LONGVARCHAR}, #{detail.warehouseDetail,jdbcType=LONGVARCHAR}, #{detail.warehouseDetailEn,jdbcType=LONGVARCHAR})" })
    int batchInsert(@Param("detail") TbWarehouse detail);
}
