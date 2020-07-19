package com.wzp.cloud.support.domain.aggregate.location;

public interface LocationRepository {
    Location find(String code);
}
