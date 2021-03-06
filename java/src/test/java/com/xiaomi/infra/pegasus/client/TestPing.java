// Copyright (c) 2017, Xiaomi, Inc.  All rights reserved.
// This source code is licensed under the Apache License Version 2.0, which
// can be found in the LICENSE file in the root directory of this source tree.

package com.xiaomi.infra.pegasus.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by mi on 16-3-22.
 */
public class TestPing {
    @Test
    public void testPing() throws PException {
        PegasusClientInterface client = PegasusClientFactory.getSingletonClient();
        String tableName = "temp";

        byte[] hashKey = "hello".getBytes();
        byte[] sortKey = "0".getBytes();
        byte[] value = "world".getBytes();
        byte[] sortKey1 = "1".getBytes();
        byte[] value1 = "pegasus".getBytes();

        try {
            System.out.println("set value ...");
            client.set(tableName, hashKey, sortKey, value, 0);
            System.out.println("set value ok");

            System.out.println("set value1 ...");
            client.set(tableName, hashKey, sortKey1, value1, 0);
            System.out.println("set value1 ok");

            System.out.println("multi set ...");
            List<Pair<byte[], byte[]>> setValues = new ArrayList<Pair<byte[], byte[]>>();
            for (int i = 2; i < 9; ++i) {
                byte[] k = Integer.toString(i).getBytes();
                byte[] v = ("value" + i).getBytes();
                setValues.add(new ImmutablePair<byte[], byte[]>(k, v));
            }
            client.multiSet(tableName, hashKey, setValues);
            System.out.println("multi set ...");

            System.out.println("get value ...");
            byte[] result = client.get(tableName, hashKey, sortKey);
            Assert.assertTrue(Arrays.equals(value, result));
            System.out.println("get value ok");

            System.out.println("get ttl ...");
            int ttl = client.ttl(tableName, hashKey, sortKey);
            Assert.assertEquals(-1, ttl);
            System.out.println("get ttl ok");

            System.out.println("multi get ...");
            List<byte[]> sortKeys = new ArrayList<byte[]>();
            sortKeys.add("unexist-sort-key".getBytes());
            sortKeys.add(sortKey1);
            sortKeys.add(sortKey1);
            sortKeys.add(sortKey);
            List<Pair<byte[], byte[]>> values = new ArrayList<Pair<byte[], byte[]>>();
            boolean getAll = client.multiGet(tableName, hashKey, sortKeys, values);
            Assert.assertTrue(getAll);
            Assert.assertEquals(2, values.size());
            Assert.assertEquals(sortKey, values.get(0).getKey());
            Assert.assertArrayEquals(sortKey, values.get(0).getKey());
            Assert.assertArrayEquals(value, values.get(0).getValue());
            Assert.assertEquals(sortKey1, values.get(1).getKey());
            Assert.assertArrayEquals(sortKey1, values.get(1).getKey());
            Assert.assertArrayEquals(value1, values.get(1).getValue());
            System.out.println("multi get ok");

            System.out.println("multi get partial ...");
            sortKeys.clear();
            values.clear();
            sortKeys.add(sortKey);
            sortKeys.add(sortKey1);
            for (Pair<byte[], byte[]> p : setValues) {
                sortKeys.add(p.getKey());
            }
            getAll = client.multiGet(tableName, hashKey, sortKeys, 5, 1000000, values);
            Assert.assertFalse(getAll);
            Assert.assertEquals(5, values.size());
            Assert.assertEquals(sortKey, values.get(0).getKey());
            Assert.assertArrayEquals(sortKey, values.get(0).getKey());
            Assert.assertArrayEquals(value, values.get(0).getValue());
            Assert.assertEquals(sortKey1, values.get(1).getKey());
            Assert.assertArrayEquals(sortKey1, values.get(1).getKey());
            Assert.assertArrayEquals(value1, values.get(1).getValue());
            for (int i = 2; i < 5; ++i) {
                Assert.assertEquals(setValues.get(i - 2).getKey(), values.get(i).getKey());
                Assert.assertArrayEquals(setValues.get(i - 2).getKey(), values.get(i).getKey());
                Assert.assertArrayEquals(setValues.get(i - 2).getValue(), values.get(i).getValue());
            }
            System.out.println("multi get partial ok");

            System.out.println("del value ...");
            client.del(tableName, hashKey,sortKey);
            System.out.println("del value ok");

            System.out.println("get deleted value ...");
            result = client.get(tableName, hashKey, sortKey);
            Assert.assertEquals(result, null);
            System.out.println("get deleted value ok");
        }
        catch (PException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

        PegasusClientFactory.closeSingletonClient();
    }
}
