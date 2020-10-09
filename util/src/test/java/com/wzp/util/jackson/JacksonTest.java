package com.wzp.util.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class JacksonTest {

    /**
     * 简单数据绑定
     */
    @Test
    public void test1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 绑定简单类型  和 Map类型
        Integer age = objectMapper.readValue("1", int.class);
        Map map = objectMapper.readValue("{\"name\":  \"YourBatman\"}", Map.class);
        System.out.println(age);
        System.out.println(map);
    }

    /**
     * === 泛型擦除问题 ===
     *  <pre>
     *      1. Java 在编译时会在字节码里指令集之外的地方保留部分泛型信息
     *      2. 泛型接口、类、方法定义上的所有泛型、成员变量声明处的泛型都会被保留类型信息，其它地方的泛型信息都会被擦除
     *  </pre>
     */
    @Test
    public void test5() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("----------读集合类型----------");
        List<Long> list = objectMapper.readValue("[1,2,3]", List.class);

        Long id = list.get(0);
        System.out.println(id);
    }

    /**
     * 方案一：利用成员变量保留泛型
     */
    @Test
    public void test6() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("----------读集合类型----------");
        Data data = objectMapper.readValue("{\"ids\" : [1,2,3]}", Data.class);

        Long id = data.getIds().get(0);
        System.out.println(id);
    }

    /**
     * 方案二：使用官方推荐的TypeReference<T>，保留泛型类型
     */
    @Test
    public void test7() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("----------读集合类型----------");
        List<Long> ids = objectMapper.readValue("[1,2,3]", new TypeReference<List<Long>>() {
        });

        Long id = ids.get(0);
        System.out.println(id);
    }

}