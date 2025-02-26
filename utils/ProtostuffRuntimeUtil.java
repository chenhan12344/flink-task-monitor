package com.demo.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ProtostuffRuntimeUtil {


    private static final int LINKED_BUFFER_QUEUE_SIZE = 512;
    private static final int LINKED_BUFFER_SIZE = 512;
    private static final Map<String, Schema<?>> schemas = new ConcurrentHashMap<>();
    private static final BlockingQueue<LinkedBuffer> linkedBufferPoolQueue = new ArrayBlockingQueue<>(LINKED_BUFFER_QUEUE_SIZE);

    public ProtostuffRuntimeUtil() {
    }

    public static <T> byte[] serialize(T data) {
        Class<?> clazz = data.getClass();
        Schema<T> schema = getSchema(clazz);
        byte[] protostuff = null;
        LinkedBuffer linkedBuffer = null;
        try {
            linkedBuffer = linkedBufferPoolQueue.take();
            protostuff = ProtostuffIOUtil.toByteArray(data, schema, linkedBuffer);
        } catch (InterruptedException var13) {
            // logger.error("serialize error, Class=" + clazz, var13);
        } finally {
            if (linkedBuffer != null) {
                linkedBuffer.clear();

                try {
                    linkedBufferPoolQueue.put(linkedBuffer);
                } catch (InterruptedException var12) {
                    log.error("put linkedBuffer error, Class=" + clazz, var12);
                }
            }

        }

        return protostuff;
    }

    public static <T> List<byte[]> serialize(List<T> datas) {
        if (datas != null && !datas.isEmpty()) {
            List<byte[]> results = new ArrayList<>(datas.size());
            Class<?> clazz = datas.get(0).getClass();
            Schema<T> schema = getSchema(clazz);
            LinkedBuffer linkedBuffer = null;

            try {
                linkedBuffer = linkedBufferPoolQueue.take();

                for (T data : datas) {
                    results.add(ProtostuffIOUtil.toByteArray(data, schema, linkedBuffer));
                    linkedBuffer.clear();
                }
            } catch (InterruptedException var14) {
                log.error("serialize error, Class=" + clazz, var14);
                throw new RuntimeException(var14);
            } finally {
                if (linkedBuffer != null) {
                    linkedBuffer.clear();

                    try {
                        linkedBufferPoolQueue.put(linkedBuffer);
                    } catch (InterruptedException var13) {
                        log.error("put linkedBuffer error, Class=" + clazz, var13);
                    }
                }

            }

            return results;
        } else {
            return null;
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        if (clazz == null || data == null) {
            return null;
        }

        Schema<T> schema = getSchema(clazz);
        T t = null;
        try {
            t = clazz.newInstance();
            ProtostuffIOUtil.mergeFrom(data, t, schema);
        } catch (Exception e) {
            log.error("deSerialize error, Class=" + clazz, e);
        }
        return t;
    }

    public static <T> List<T> deserialize(List<byte[]> list, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        for (byte[] bytes : list) {
            resultList.add(deserialize(bytes, clazz));
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<?> clazz) {
        String name = clazz.getName();
        Schema<?> schema = schemas.get(name);
        if (schema == null) {
            synchronized (name) {
                schema = schemas.get(name);
                if (schema == null) {
                    schema = RuntimeSchema.getSchema(clazz);
                }

                schemas.put(name, schema);
            }
        }

        return (Schema<T>) schema;
    }

    static {
        for (int i = 0; i < 512; ++i) {
            try {
                linkedBufferPoolQueue.put(LinkedBuffer.allocate(LINKED_BUFFER_SIZE));
            } catch (InterruptedException var2) {
                log.error("init linkedBufferQueue error");
            }
        }

    }

}