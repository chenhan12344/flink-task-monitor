package com.demo.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @Description
 * @Date 2023/12/19 19:44
 */
public class ScheduleTaskSource implements SourceFunction<Integer> {
    private final Integer n;

    public ScheduleTaskSource(Integer n) {
        this.n = n;
    }

    @Override
    public void run(SourceContext<Integer> sourceContext) throws Exception {
        do {
            sourceContext.collect(n);
            Thread.sleep(n * 60 * 1000);
        } while (true);
    }

    @Override
    public void cancel() {

    }
}
