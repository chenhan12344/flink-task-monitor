package com.demo.model.flinkdashboard;

import com.dyuproject.protostuff.Tag;
import lombok.Data;

@Data
public class GarbageCollectDto {

    @Tag(1)
    private String name; //PS_MarkSweep PS_Scavenge

    @Tag(2)
    private long count;

    @Tag(3)
    private long time;
}
