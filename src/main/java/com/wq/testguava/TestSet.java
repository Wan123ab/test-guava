package com.wq.testguava;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.HashSet;

/**
 * @author 万强
 * @date 2019/6/26 11:28
 * @desc
 */
public class TestSet {

    @Test
    public void test1() {
        HashSet<Integer> hashSet1 = Sets.newHashSet(1, 2, 3, 4, 5, 6);
        HashSet<Integer> hashSet2 = Sets.newHashSet(1, 3, 5, 7, 9, 11);

        //求并集
        Sets.SetView<Integer> union = Sets.union(hashSet1, hashSet2);
        System.out.println(union);//[1, 2, 3, 4, 5, 6, 7, 9, 11]

        //求差集
        Sets.SetView<Integer> difference = Sets.difference(hashSet1, hashSet2);
        System.out.println(difference);//[2, 4, 6]

        //求交集
        Sets.SetView<Integer> intersection = Sets.intersection(hashSet1, hashSet2);
        System.out.println(intersection);//[1, 3, 5]
    }

    @Test
    public void test2() {
        HashSet<Car> hashSet1 = Sets.newHashSet(
                new Car("1","丰田","黑色"),
                new Car("2","本田","红色"),
                new Car("3","雷克萨斯","银色"),
                new Car("4","尼桑","蓝色")
                );

        HashSet<Car> hashSet2 = Sets.newHashSet(
                new Car("1","丰田皇冠","黑色"),
                new Car("3","雷克萨斯SUV","银色"),
                new Car("5","大众","银色"),
                new Car("6","奥迪","蓝色")
        );

        //求并集
        Sets.SetView<Car> union = Sets.union(hashSet1, hashSet2);
        System.out.println(union);//[TestSet.Car(id=2, brand=本田, color=红色), TestSet.Car(id=3, brand=雷克萨斯, color=银色), TestSet.Car(id=4, brand=尼桑, color=蓝色), TestSet.Car(id=1, brand=丰田, color=黑色), TestSet.Car(id=5, brand=大众, color=银色), TestSet.Car(id=6, brand=奥迪, color=蓝色)]

        //求差集
        Sets.SetView<Car> difference = Sets.difference(hashSet1, hashSet2);
        System.out.println(difference);//[TestSet.Car(id=2, brand=本田, color=红色), TestSet.Car(id=4, brand=尼桑, color=蓝色)]

        //求交集
        Sets.SetView<Car> intersection = Sets.intersection(hashSet1, hashSet2);
        System.out.println(intersection);//[TestSet.Car(id=3, brand=雷克萨斯, color=银色), TestSet.Car(id=1, brand=丰田, color=黑色)]
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    //设置排除brand，color，只通过比较id确定是否为同一对象
    //将影响到test2的输出结果
    @EqualsAndHashCode(exclude = {"brand", "color"})
    class Car{

        private String id;

        private String brand;

        private String color;

    }

}
