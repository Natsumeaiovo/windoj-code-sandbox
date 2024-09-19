package com.serein.windojcodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/10 14:41
 * @description: 无限占用内存空间
 */
public class MemoryError {

    public static void main(String[] args) throws InterruptedException {
        List<byte[]> bytes = new ArrayList<>();
        while (true) {
            bytes.add(new byte[10000]);
        }
    }
}
