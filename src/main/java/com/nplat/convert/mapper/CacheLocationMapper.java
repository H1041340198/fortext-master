package com.nplat.convert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nplat.convert.entity.entity.CacheLocation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface CacheLocationMapper extends BaseMapper<CacheLocation> {

    //@Select("select * from cache_location where cache_key = #{key}")
    //CacheLocation getCacheLocationByKeyOnly(@Param("key")String key);


    @Select("select * from cache_location where cache_key = #{key} and expires > #{currentTimeStamp}")
    CacheLocation getCacheLocationByKey(@Param("key") String key, @Param("currentTimeStamp") Long currentTimeStamp);


    @Select("select * from cache_location where id = #{userId} and expires > #{currentTimeStamp}")
    CacheLocation getCacheLocationByUserId(@Param("userId") Long userId, @Param("currentTimeStamp") Long currentTimeStamp);


    @Insert("insert into cache_location(id, cache_key, cache_value,expires) value(#{id},#{cacheKey},#{cacheValue},#{expires})")
    void addCacheLocation(CacheLocation cacheLocation);


}
