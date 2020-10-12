package com.wzp.util.streamcollectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamCollectorsTest {


    @Test
    public void flatMap() {
        List<Integer> a = Arrays.asList(1, 2, 3, 4);
        List<Integer> b = Arrays.asList(4, 5, 6);
        // <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper)
        List<List<Integer>> collect = Stream.of(a, b).collect(Collectors.toList());
        // [[1, 2, 3], [4, 5, 6]]
        System.out.println(collect);
        // 将多个集合中的元素合并成一个集合
        List<Integer> mergeList = Stream.of(a, b).flatMap(list -> list.stream()).distinct().collect(Collectors.toList());
        // [1, 2, 3, 4, 5, 6]
        System.out.println(mergeList);
        // 通过Builder模式来构建
        Stream<Object> stream = Stream.builder().add("hello").add("hi").add("byebye").build();
    }

    @Test
    public void limit() {
        List<String> list = Arrays.asList("a", "b", "c", "d", "e");
        list.stream().skip(2).limit(2).forEach(System.out::println); // c、d
    }

    @Test
    public void concat() {
        List<String> list = Arrays.asList("a", "b");
        List<String> list2 = Arrays.asList("c", "d");
        Stream<String> concatStream = Stream.concat(list.stream(), list2.stream());
        concatStream.forEach(System.out::println);
    }

    @Test
    public void match() {
        // 你给我站住
        List<String> list = Arrays.asList("you", "give", "me", "stop");
//         boolean anyMatch(Predicate<? super T> predicate);
        // parallelStream可以并行计算，速度比stream更快
        // 如果集合中有一个元素满足条件就返回true
        boolean result = list.parallelStream().anyMatch(item -> item.equals("stop"));
        System.out.println(result);
    }

    @Test
    public void findFirst() {
        Stream<String> stream = Stream.of("you", "give", "me", "stop");
        String value = stream.findFirst().get();
        System.out.println(value);
    }

    @Test
    public void findAny() {
        Stream<String> stream = Stream.of("you", "give", "me", "stop");
        String value2 = stream.findAny().get();
        System.out.println(value2);
    }

    @Test
    public void testToCollection() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        // [10, 20, 30]
        List<Integer> collect = list.stream().map(i -> i * 10).collect(Collectors.toList());
        System.out.println(collect);

        // [20, 10, 30]
        Set<Integer> collect1 = list.stream().map(i -> i * 10).collect(Collectors.toSet());
        System.out.println(collect1);

        // {key1=value:10, key2=value:20, key3=value:30}
        Map<String, String> collect2 = list.stream().map(i -> i * 10).collect(Collectors.toMap(key -> "key" + key / 10, value -> "value:" + value));
        System.out.println(collect2);

        // [1, 3, 4]
        TreeSet<Integer> collect3 = Stream.of(1, 2, 3).collect(Collectors.toCollection(TreeSet::new));
        System.out.println(collect3);
    }

    @Data
    @ToString
    @AllArgsConstructor
    @RequiredArgsConstructor
    public class User {
        private Long id;
        private String username;
    }

    @Test
    public void testToMap() {
        List<User> userList = Arrays.asList(
                new User(1L, "mengday"),
                new User(2L, "mengdee"),
                new User(3L, "mengdy")
        );

        // toMap 可用于将List转为Map，便于通过key快速查找到某个value
        Map<Long, User> userIdAndModelMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        User user = userIdAndModelMap.get(1L);
        // User(id=1, username=mengday)
        System.out.println(user);

        Map<Long, String> userIdAndUsernameMap = userList.stream().collect(Collectors.toMap(User::getId, User::getUsername));
        String username = userIdAndUsernameMap.get(1L);
        // mengday
        System.out.println(username);
    }


    @Test
    public void testJoining() {
        // a,b,c
        List<String> list2 = Arrays.asList("a", "b", "c");
        String result = list2.stream().collect(Collectors.joining(","));
        System.out.println(result);
        // Collectors.joining(",")的结果是：a,b,c 然后再将结果 x + "d"操作, 最终返回a,b,cd
        String str = Stream.of("a", "b", "c").collect(Collectors.collectingAndThen(Collectors.joining(","), x -> x + "d"));
        System.out.println(str);
    }

    @Test
    public void aggr() {
        // 求最值 3
        List<Integer> list = Arrays.asList(1, 2, 3);
        System.out.println(list.stream().max(Comparator.comparingInt(a -> a)).get());
        Integer maxValue = list.stream().collect(Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparingInt(a -> a)), Optional::get));
        System.out.println(maxValue);
        // 最小值 1
        Integer minValue = list.stream().collect(Collectors.collectingAndThen(Collectors.minBy(Comparator.comparingInt(a -> a)), Optional::get));
        System.out.println(minValue);
        // 求和 6
        Integer sumValue = list.stream().collect(Collectors.summingInt(item -> item));
        System.out.println(sumValue);
        // 平均值 2.0
        Double avg = list.stream().collect(Collectors.averagingDouble(x -> x));
        System.out.println(avg);

        // 映射：先对集合中的元素进行映射，然后再对映射的结果使用Collectors操作
        // A,B,C
        String collect = Stream.of("a", "b", "c").collect(Collectors.mapping(x -> x.toUpperCase(), Collectors.joining(",")));
        System.out.println(collect);
    }

    @Test
    public void testReducing(){
        // sum: 是每次累计计算的结果，b是Function的结果
        System.out.println(Stream.of(1, 3, 4).collect(Collectors.reducing(0, x -> x + 1, (sum, b) -> {
            System.out.println(sum + "-" + b);
            return sum + b;
        })));

        // 下面代码是对reducing函数功能实现的描述，用于理解reducing的功能
        int sum = 0;
        List<Integer> list3 = Arrays.asList(1, 3, 4);
        for (Integer item : list3) {
            int b = item + 1;
            System.out.println(sum + "-" + b);
            sum = sum + b;
        }
        System.out.println(sum);

        // 注意reducing可以用于更复杂的累计计算，加减乘除或者更复杂的操作
        // result = 2 * 4 * 5 = 40
        System.out.println(Stream.of(1, 3, 4).collect(Collectors.reducing(1, x -> x + 1, (result, b) -> {
            System.out.println(result + "-" + b);
            return result * b;
        })));
    }


}
