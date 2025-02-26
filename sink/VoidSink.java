package com.demo.sink;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * 虚空Sink，丢弃所有接收到的数据
 *
 * @author sky
 */
public class VoidSink<T> implements SinkFunction<T> {

    @Override
    public void invoke(T value, Context context) throws Exception {

    }
}
