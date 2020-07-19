package com.wzp.cloud.graphql.oapi;

import io.swagger.v3.oas.models.media.Schema;

public class ExtendSchema extends Schema {

    private Schema items = null;

    public ExtendSchema items(Schema items) {
        this.items = items;
        return this;
    }

    public Schema getItems() {
        return items;
    }

    public void setItems(Schema items) {
        this.items = items;
    }

}
