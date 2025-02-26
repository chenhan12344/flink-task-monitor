package com.demo.state;

import com.demo.utils.ProtostuffRuntimeUtil;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;

import java.io.IOException;

/**
 * 基于PB序列化的ValueState
 * 对ValueState进行封装，以无感的方式使用带PB序列化的ValueState
 * 避免在业务代码中出现大量正反序列化的逻辑
 *
 * @author sky
 * @date 2023-10-27 16:09
 */
public class PrototsuffValueState<T> implements ValueState<T> {

    /**
     * 代理的ValueState，存储PB序列化后的数据
     */
    private final ValueState<byte[]> delegatedState;
    /**
     * 反序列化后的对象类型
     */
    private final Class<T> valueClass;

    public PrototsuffValueState(ValueState<byte[]> delegatedState, Class<T> valueClass) {
        this.delegatedState = delegatedState;
        this.valueClass = valueClass;
    }

    @Override
    public T value() throws IOException {
        return ProtostuffRuntimeUtil.deserialize(delegatedState.value(), valueClass);
    }

    public T valueOrDefault(T defaultValue) throws Exception {
        T value = value();
        return value != null ? value : defaultValue;
    }

    @Override
    public void update(T value) throws IOException {
        delegatedState.update(ProtostuffRuntimeUtil.serialize(value));
    }

    @Override
    public void clear() {
        delegatedState.clear();
    }

    /**
     * 状态描述符简化创建方法
     *
     * @param name 描述符名称
     * @return ValueState描述符
     */
    public static ValueStateDescriptor<byte[]> descriptor(String name) {
        return new ValueStateDescriptor<>(name, byte[].class);
    }

    /**
     * 状态描述符简化创建方法
     *
     * @param name 描述符名称
     * @param ttl  状态保存时间
     * @return ValueState描述符
     */
    public static ValueStateDescriptor<byte[]> descriptor(String name, Time ttl) {
        ValueStateDescriptor<byte[]> descriptor = new ValueStateDescriptor<>(name, byte[].class);
        descriptor.enableTimeToLive(StateTtlConfig.newBuilder(ttl).build());
        return descriptor;
    }

}
