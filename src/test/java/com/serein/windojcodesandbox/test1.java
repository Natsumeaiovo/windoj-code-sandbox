package com.serein.windojcodesandbox;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.ToIntFunction;

/**
 * @author: serein
 * @date: 2024/9/14 10:06
 * @description:
 */
public class test1 {

    public static void main(String[] args) {
        int[][] points = {{0, 1}, {3, 6}, {-100, 200}};
        Arrays.sort(points, Comparator.comparingInt(point -> point[0]));

    }
}
