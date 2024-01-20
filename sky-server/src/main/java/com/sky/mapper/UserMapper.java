package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where user.openid = #{openid}")
    User getByOpenid(String openid);

    void insert(User user);
    @Select("select * from user where user.id = #{d}")
    User getById(Long userId);

    Integer countByMap(Map<String, Object> map);
}
