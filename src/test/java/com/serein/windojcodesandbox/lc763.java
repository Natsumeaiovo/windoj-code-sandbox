package com.serein.windojcodesandbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: serein
 * @date: 2024/9/19 10:26
 * @description:
 */
public class lc763 {
    public List<Integer> partitionLabels(String s) {
        List<Integer> res = new ArrayList<>();
        int n = s.length();
        if (n == 0) {
            return res;
        }
        Map<Character, Integer> map = new HashMap<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < n; i++) {
            map.put(chars[i], i);    // map 记录了所有字母，和其对应的最后出现位置索引
        }

        int index = 0;
        int maxRight;
        while (index < n) {
            maxRight = map.get(chars[index]);
            for (int i = index + 1; i < maxRight; i++) {
                maxRight = Math.max(maxRight, map.get(chars[i]));
            }
            res.add(maxRight - index + 1);
            index = maxRight + 1;
        }

        return res;
    }
}
