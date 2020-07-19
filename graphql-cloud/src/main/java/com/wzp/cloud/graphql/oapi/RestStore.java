package com.wzp.cloud.graphql.oapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RestStore {

    public Map<String, ObjectType> types = new HashMap<>();

    public Map<String, Action> actions = new HashMap<>();

    public RestStore(Collection<ObjectType> types, Collection<Action> actions) {

        for (ObjectType type : types) {
            this.types.put(type.getName(), type);
        }
        for (Action action : actions) {
            this.actions.put(action.getName(), action);
        }
    }

    public Map<String, ObjectType> getTypes() {
        return types;
    }

    public Map<String, Action> getActions() {
        return actions;
    }
}
