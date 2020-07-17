package com.demo.infrastructure.db.mapper;

import com.demo.infrastructure.db.dataobject.CargoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CargoMapper {

    CargoDO select(@Param("id") String id);

    List<CargoDO> selectAll();

    List<CargoDO> selectByCustomer(@Param("phone") String phone);

    void save(CargoDO cargoDO);
    
    void update(CargoDO cargoDO);

    void remove(@Param("id") String id);

    int countByCustomer(@Param("phone") String phone);


}
