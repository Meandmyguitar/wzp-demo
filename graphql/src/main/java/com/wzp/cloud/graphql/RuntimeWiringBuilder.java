package com.wzp.cloud.graphql;

import graphql.schema.idl.TypeRuntimeWiring;
import org.dataloader.DataLoader;

import java.util.function.Supplier;

public interface RuntimeWiringBuilder {

    void type(TypeRuntimeWiring.Builder builder);

    void registerDataLoader(String name, Supplier<DataLoader<?, ?>> dataLoader);
}
