package com.nplat.convert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nplat.convert.entity.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface FileEntityMapper extends BaseMapper<FileEntity> {


    @Select("select * from file_entity where status = 1 and serializable_no = #{serializableNo}")
    List<FileEntity> getFileEntityBySerializableNo(String serializableNo);


    @Select("select id from file_entity where status = 1 and serializable_no = #{serializableNo}")
    List<Long> getFileEntityIdBySerializableNo(String serializableNo);



    @Select("<script>  " +
            "select count(*) from file_entity where status =1 " +
            "<if  test=' serializableNo !=null '  >" +
            "and  serializable_no = #{serializableNo} " +
            "</if>" +
            "  </script>")
    Integer getUserCount( @Param("serializableNo") String serializableNo);



    @Select("<script>  " +
            "select * from file_entity where status=1 " +
            "<if  test=' serializableNo !=null '  >" +
            "and  serializable_no = #{serializableNo} " +
            "</if>" +
            " order by ${orderColumn} ${order} limit ${start},${offset}" +
            "  </script>")
    List<FileEntity> getUserInfoList( @Param("serializableNo") String serializableNo,
                               @Param("orderColumn") String orderColumn,
                               @Param("order") String order,
                               @Param("start") Integer start,
                               @Param("offset") Integer offset);


}
