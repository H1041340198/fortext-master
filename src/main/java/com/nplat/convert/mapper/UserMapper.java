package com.nplat.convert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nplat.convert.entity.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface UserMapper extends BaseMapper<User> {


    @Select("select * from user where email = #{email}")
    User getInfoByEmail(@Param("email") String email);



    @Select("select * from user where email = #{email} and password = #{password}")
    User getInfoByEmailAndPassword(@Param("email") String email, @Param("password") String password);



}