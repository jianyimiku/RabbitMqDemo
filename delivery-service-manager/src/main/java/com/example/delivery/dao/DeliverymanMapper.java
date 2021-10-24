package com.example.delivery.dao;

import com.example.delivery.constant.DeliveryManStatus;
import com.example.delivery.pojo.Deliveryman;

import java.util.List;

public interface DeliverymanMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Deliveryman record);

    int insertSelective(Deliveryman record);

    Deliveryman selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Deliveryman record);

    int updateByPrimaryKey(Deliveryman record);

    List<Deliveryman> selectDeliveryManByStatus(DeliveryManStatus deliveryManStatus);
}