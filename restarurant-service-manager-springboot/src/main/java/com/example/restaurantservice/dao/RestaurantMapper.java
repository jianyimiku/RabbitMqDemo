package com.example.restaurantservice.dao;


import com.example.restaurantservice.pojo.Restaurant;

public interface RestaurantMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Restaurant record);

    int insertSelective(Restaurant record);

    Restaurant selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Restaurant record);

    int updateByPrimaryKey(Restaurant record);
}