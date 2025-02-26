package com.demo.state;

import org.apache.flink.contrib.streaming.state.DefaultConfigurableOptionsFactory;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyOptions;

import java.util.Collection;

/**
 * 为RocksDB启用布隆过滤器
 * Flink1.15才引入了state.backend.rocksdb.use-bloom-filter参数开启布隆过滤器
 * 对于Flink1.13只能通过PredefinedOptions.SPINNING_DISK_OPTIMIZED_HIGH_MEM启用
 * 但是使用SPINNING_DISK_OPTIMIZED_HIGH_MEM会默认修改其他RocksDB参数
 * 所以这里单独使用一个工厂类继承DefaultConfigurableOptionsFactory，在创建TableFormatConfig时启用布隆过滤器
 *
 * @author sky
 * @see org.apache.flink.contrib.streaming.state.PredefinedOptions#SPINNING_DISK_OPTIMIZED_HIGH_MEM
 */
public class BloomFilterEnabledBlockBasedOptionsFactory extends DefaultConfigurableOptionsFactory {

    @Override
    public ColumnFamilyOptions createColumnOptions(ColumnFamilyOptions currentOptions, Collection<AutoCloseable> handlesToClose) {
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();

        BloomFilter bloomFilter = new BloomFilter();
        handlesToClose.add(bloomFilter);
        tableConfig.setFilter(bloomFilter);

        // 父类会调用currentOptions.tableFormatConfig()获取配置，在这里设置TableFormatConfig即可
        currentOptions.setTableFormatConfig(tableConfig);
        return super.createColumnOptions(currentOptions, handlesToClose);
    }
}
