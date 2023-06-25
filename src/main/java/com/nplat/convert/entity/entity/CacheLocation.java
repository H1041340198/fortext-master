package com.nplat.convert.entity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("cache_location")
public class CacheLocation {
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    String cacheKey;
    String cacheValue;
    Long expires;



}
