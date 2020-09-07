package com.wq.testguava;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.junit.Test;

import java.util.List;

/**
 * @author 万强
 * @date 2019/6/25 19:39
 * @desc
 */
public class Test1 {

    @Test
    public void test1() {
        Iterable<String> split = Splitter.on(",").split("1,2,3,4,5");
        split.forEach(System.out::println);

    }

    @Test
    public void test2() {
        List<String> list = Lists.newArrayList("张三", "李四", "王五");
        System.out.println(list);//[张三, 李四, 王五]
    }

    /**
     * 不可变List，只读
     */
    @Test
    public void test3() {
        ImmutableList<String> immutableList = ImmutableList.of("one", "two", "three", "six", "seven", "eight");
        System.out.println(immutableList);
    }

    /**
     * Table类型，用来取代Map<FirstName, Map<LastName, Person>>
     * 相当于有两个key的map
     */
    @Test
    public void test4() {
        Table<String, Integer, String> aTable = HashBasedTable.create();

        for (char a = 'A'; a <= 'C'; ++a) {
            for (Integer b = 1; b <= 3; ++b) {
                aTable.put(Character.toString(a), b, String.format("%c%d", a, b));
            }
        }


        System.out.println(aTable.get("B", 2));//B2

        System.out.println(aTable.contains("D", 1));//false
        System.out.println(aTable.containsColumn(3));//true
        System.out.println(aTable.containsRow("C"));//true


        System.out.println(aTable.remove("B", 3));//B3

        System.out.println(aTable.get("C", 2));//C2

        /**
         * column相关操作
         */
        System.out.println(aTable.columnKeySet());//[1, 2, 3]
        System.out.println(aTable.columnMap());//{1={A=A1, B=B1, C=C1}, 2={A=A2, B=B2, C=C2}, 3={A=A3, B=B3, C=C3}}
        System.out.println(aTable.column(2));//{A=A2, B=B2, C=C2}

        /**
         * row相关操作
         */
        /**
         * row(r)返回一个非null的Map<C, V>。修改这个视图Map也会导致原表格的修改。
         */
        System.out.println(aTable.row("B"));//{1=B1, 2=B2, 3=B3}
        /**
         * rowMap()返回一个Map<R, Map<C, V>>的视图
         */
        System.out.println(aTable.rowMap());//{A={1=A1, 2=A2, 3=A3}, B={1=B1, 2=B2, 3=B3}, C={1=C1, 2=C2, 3=C3}}
        System.out.println(aTable.rowKeySet());//[A, B, C]
    }

    /**
     * ClassToInstanceMap<B> 实现了Map<Class<? extends B>, B>，或者说，这是一个从B的子类到B对象的映射，
     * 这可能使得ClassToInstanceMap的泛型轻度混乱，但是只要记住B总是Map的上层绑定类型，通常来说B只是一个对象
     */
    @Test
    public void ClassToInstanceMapTest() {
        ClassToInstanceMap<String> classToInstanceMapString = MutableClassToInstanceMap.create();
        ClassToInstanceMap<Person> classToInstanceMap = MutableClassToInstanceMap.create();
        Person person = new Person("Tony", 20);
        classToInstanceMapString.put(String.class, "哈哈");
        System.out.println("string:" + classToInstanceMapString.getInstance(String.class));

        classToInstanceMap.putInstance(Person.class, person);
        Person person1 = classToInstanceMap.getInstance(Person.class);
        System.out.println("person1 name :" + person1.name + " age:" + person1.age);
    }

    class Person {
        public String name;
        public int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    /**
     * Multimap:key可以重复的map，相当于Map<String,List>
     */
    @Test
    public void testMultimap() {
        Multimap<String, String> multimap = ArrayListMultimap.create();
        multimap.put("Alibaba", "张三");
        multimap.put("Alibaba", "李四");
        multimap.put("Alibaba", "王五");
        multimap.put("Alibaba", "赵六");
        multimap.put("Alibaba", "周七");
        multimap.put("Alibaba", "丁八");
        System.out.println(multimap.get("Alibaba"));//[张三, 李四, 王五, 赵六, 周七, 丁八]
    }

    /**
     * Multiset：把重复的元素放入集合，并且可以统计重复元素的个数
     */
    @Test
    public void testMultiset() {
        Multiset<Integer> multiSet = HashMultiset.create();
        multiSet.add(10);
        multiSet.add(30);
        multiSet.add(30);
        multiSet.add(40);

        System.out.println(multiSet.count(30)); // 2
        System.out.println(multiSet.size());    //4
    }

    /**
     * BiMap:保证key和value都不会重复，若value重复将报错
     * 提供inverse()方法，可以通过key得到value，也可以通过value得到key
     * 注意：
     * inverse方法会返回一个反转的BiMap，但是注意这个反转的map不是新的map对象，
     * 它实现了一种视图关联，这样你对于反转后的map的所有操作都会影响原先的map对象
     */
    @Test
    public void testBiMap() {
        //双向map
        BiMap<Integer, String> biMap = HashBiMap.create();
        biMap.put(1, "hello");
        biMap.put(2, "my");
        int value = biMap.inverse().get("my");
        System.out.println(value);//2

        BiMap<String, Integer> inverseMap = biMap.inverse();
        inverseMap.put("world",3);
        inverseMap.put("honey",4);

        System.out.println(biMap);//{1=hello, 2=my, 3=world, 4=honey}
        System.out.println(inverseMap);//{hello=1, my=2, world=3, honey=4}
    }

    @Test
    public void test(){
        Table<String, Integer, String> table = HashBasedTable.create();
        table.put("湖北", 18,"Allen");
        table.put("湖北", 20,"Tom");
        table.put("湖北", 22,"John");
        table.put("湖南", 16,"Jerry");
        table.put("湖南", 19,"Mark");
        System.out.println(table.rowMap());
        table.rowMap().remove("湖北");
        System.out.println(table.rowMap());
    }


}
