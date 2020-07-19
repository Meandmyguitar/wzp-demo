package com.wzp.cloud.support.domain.aggregate.cargo;


import com.wzp.cloud.support.domain.aggregate.cargo.valueobject.EnterpriseSegment;

public interface CargoRepository {

    Cargo find(String id);
    
    int sizeByCustomer(String customerPhone);
    
    int sizeByEnterpriseSegment(EnterpriseSegment enterpriseSegment);

    void save(Cargo cargo);

    void remove(String id);

}
