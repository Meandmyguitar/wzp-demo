package com.demo.infrastructure.db.repository;

import com.demo.domain.aggregate.cargo.Cargo;
import com.demo.domain.aggregate.cargo.CargoRepository;
import com.demo.domain.aggregate.cargo.valueobject.EnterpriseSegment;
import com.demo.infrastructure.db.converter.CargoConverter;
import com.demo.infrastructure.db.dataobject.CargoDO;
import com.demo.infrastructure.db.mapper.CargoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CargoRepositoryImpl implements CargoRepository {

    @Autowired
    private CargoMapper cargoMapper;

    @Override
    public Cargo find(String id) {
        CargoDO cargoDO = cargoMapper.select(id);
        Cargo cargo = CargoConverter.deserialize(cargoDO);
        return cargo;
    }

    @Override
    public void save(Cargo cargo) {
        CargoDO cargoDO = CargoConverter.serialize(cargo);
        CargoDO data = cargoMapper.select(cargoDO.getId());
        if (null == data) {
            cargoMapper.save(cargoDO);
        } else {
            cargoMapper.update(cargoDO);
        }
    }

    @Override
    public void remove(String id) {
        cargoMapper.remove(id);
    }

    @Override
    public int sizeByCustomer(String customerPhone) {
        return cargoMapper.countByCustomer(customerPhone);
    }

    @Override
    public int sizeByEnterpriseSegment(EnterpriseSegment enterpriseSegment) {
        // cargoMapper
        return 20;
    }

}
