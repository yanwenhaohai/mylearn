package com.example.myself.util;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import redis.clients.jedis.Jedis;


public class SimpleCanalClientExample {
    public static final Integer _60SECONDS = 60;
    public static final String REDIS_IP_ADDRESS = "10.57.16.186";

    public static void main(String args[]) {
        // 创建链接canal服务器
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(REDIS_IP_ADDRESS,
                11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
//全部库的全部表            connector.subscribe(".*\\..*");
            //监听的表
            /*
            单库单表 test.user
            指定库全表 test\\..*
            多规则使用组合 test\\..*, test2.user1, test3.user2
             */
            connector.subscribe("my.t_user");
            connector.rollback();
            int totalEmptyCount = 10 * _60SECONDS;
            while (emptyCount < totalEmptyCount) {
                System.out.printf("我是canal, 每秒一次正在监听:" + UUID.randomUUID().toString());
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    emptyCount = 0;
                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            System.out.println("empty too many times, exit");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<Entry> entrys) throws Exception {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    redisDelete(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    redisInsert(rowData.getAfterColumnsList());
                } else if (eventType == EventType.UPDATE) {
                    redisUpdate(rowData.getAfterColumnsList());
                } else {
                    throw new Exception("数据库操作类型不明");
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

    private static void redisInsert(List<Column> columns) {
        JSONObject jsonObject = new JSONObject();
        for (Column column : columns) {
            System.out.printf(column.getName() + ":" + column.getValue() + "insert=" + column.getUpdated());
            jsonObject.put(column.getName(), column.getValue());
        }
        if (columns.size() > 0) {
            try (Jedis jedis = RedisUtil.getJedis()) {
                jedis.set(columns.get(0).getValue(), jsonObject.toJSONString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void redisUpdate(List<Column> columns) {
        JSONObject jsonObject = new JSONObject();
        for (Column column : columns) {
            System.out.printf(column.getName() + ":" + column.getValue() + "insert=" + column.getUpdated());
            jsonObject.put(column.getName(), column.getValue());
        }
        if (columns.size() > 0) {
            try (Jedis jedis = RedisUtil.getJedis()) {
                jedis.set(columns.get(0).getValue(), jsonObject.toJSONString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void redisDelete(List<Column> columns) {
        JSONObject jsonObject = new JSONObject();
        for (Column column : columns) {
            System.out.printf(column.getName() + ":" + column.getValue() + "insert=" + column.getUpdated());
            jsonObject.put(column.getName(), column.getValue());
        }
        if (columns.size() > 0) {
            try (Jedis jedis = RedisUtil.getJedis()) {
                jedis.del(columns.get(0).getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}