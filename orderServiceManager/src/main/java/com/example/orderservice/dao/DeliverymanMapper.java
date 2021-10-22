package com.example.orderservice.dao;

import com.example.orderservice.pojo.Deliveryman;
import java.util.List;

public interface DeliverymanMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Deliveryman record);

    Deliveryman selectByPrimaryKey(Integer id);

    List<Deliveryman> selectAll();

    int updateByPrimaryKey(Deliveryman record);
}