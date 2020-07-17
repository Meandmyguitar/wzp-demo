package com.demo.domain.aggregate.location;

public interface LocationRepository {
    Location find(String code);
}
