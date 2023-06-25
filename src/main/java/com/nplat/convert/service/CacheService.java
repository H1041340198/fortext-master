package com.nplat.convert.service;


import com.nplat.convert.entity.entity.CacheLocation;
import com.nplat.convert.mapper.CacheLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CacheService {

    @Autowired
    private CacheLocationMapper cacheLocationMapper;

    /**
     *
     * @param key
     * @param value
     * @param expires  过期时间,单位是秒
     */
    public void set(Long userId, String key, String value, Long expires){
        //CacheLocation cacheLocation = cacheLocationMapper.getCacheLocationByKeyOnly(key);
        CacheLocation cacheLocation = cacheLocationMapper.selectById(userId);
        if(Objects.isNull(cacheLocation)) {
            cacheLocation = new CacheLocation();
            cacheLocation.setId(userId);
            cacheLocation.setCacheKey(key);
            cacheLocation.setCacheValue(value);
            cacheLocation.setExpires(System.currentTimeMillis() + expires );
            cacheLocationMapper.addCacheLocation(cacheLocation);
        } else {
            cacheLocation.setCacheValue(value);
            cacheLocation.setExpires(System.currentTimeMillis() + expires);
            updateCacheLocationById(cacheLocation);
        }

    }


    public CacheLocation getCacheLocationByUserId(Long userId){
        CacheLocation cacheLocation =cacheLocationMapper.selectById(userId);
        if(Objects.isNull(cacheLocation) || cacheLocation.getExpires() <=  System.currentTimeMillis()){
            return null;
        }else {
            return cacheLocation;
        }
    }


    public CacheLocation getCacheLocation(String key){
        return cacheLocationMapper.getCacheLocationByKey(key, System.currentTimeMillis());
    }

    public void updateCacheLocationById( CacheLocation cacheLocation){
        cacheLocationMapper.updateById(cacheLocation);
    }


    public void createNewCache(String key, String value, Long expires){
        CacheLocation cacheLocation = new CacheLocation();
        cacheLocation.setCacheKey(key);
        cacheLocation.setCacheValue(value);
        cacheLocation.setExpires( expires );
        cacheLocationMapper.insert(cacheLocation);

    }


}
