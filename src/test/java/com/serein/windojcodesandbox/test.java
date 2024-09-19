package com.serein.windojcodesandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/12 9:32
 * @description:
 */
public class test {

    public static int[][] reconstructQueue(int[][] people) {
//        // 先根据身高降序排列，高的在前，people[i] = [h, k]
        Arrays.sort(people, (person1, person2) -> {
            // 如果身高相同，那么按照 k 值排序，k 小的在前
            if (person1[0] == person2[0]) {
                return person1[1] - person2[1];
            }
            return person2[0] - person1[0];
        });

        List<int[]> queue = new ArrayList<>();

        for (int[] person : people) {
            // void add(int index, E element);
            queue.add(person[1], person);
        }
        return queue.toArray(new int[people.length][]);
    }

    public static void main(String[] args) {

    }
}
