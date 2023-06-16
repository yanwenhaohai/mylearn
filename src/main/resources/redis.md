# Redis
## 1. 配置文件:
daemonize 是否是守护进程的形式运行
protected-mod 是否开启保护模式, 即是否允许外界访问
requirepass	设置自己的密码
dir	数据文件存储路径
logfile	日志文件存储路径

## 2. 10大数据类型
    String
        二进制安全的, 1个redis中字符串的value最多可以是512M
    List
        可以添加元素到列表的头部或者尾部, 双端列表, 最多可以包含2^32-1个元素 
    Hash
        是一个String类型的field和value, 每个hash可以存储2^32-1键值对, hash特别适合用于存储对象
    Set
        无序无重复, 集合对象的编码可以是intset或者hashtable, 最大成员数是2^32-1,  是通过hash表实现的, 添加删除查找的时间复杂度都是O(1)
    Sorted set
        不同的每个元素都会关联一个double类型的scope(分数),通过scope排序,  zset的成员是唯一的, 但scope可以重复, zset最大成员数是2^32-1, 是通过hash表实现的, 添加删除查找的时间复杂度都是O(1)
    Geospatial(GEO)
        主要用于存储地理位置信息, 并对存储的信息进行操作
    HyperLogLog
        用于做基数统计的算法, 在输入元素的数量或者体积非常非常大时, 计算基数所需的空间总是固定且是很小的, 但是因为HyperLogLog只会根据输入元素来计算基数, 而不会存储元素本身, 所以HyperLogLog不能像集合一样返回输入的各个元素, 可用于访客量, 在线用户等统计
    Bitmap
        由0和1状态表现的二进制位的bit数组, 可用于签到统计
    Bitfield
        可以一次性操作多个比特位域, 会执行一系列操作并返回一个响应数组, 这个数组中的元素对应参数列表中相应操作的执行结果, 通过bitfield命令可以一次性对多个比特位域进行操作
    Stream
        Redis5.0以后新增加的数据结构, 主要用于消息队列, 缺点是消息无法持久化, 如果网络断开, Redis宕机等, 消息就会被丢弃, 简单来说发布订阅可以分发消息, 但是无法记录历史消息, 类似于消息队列

## 3. 常用命令
    keys * 查看当前库所有key
    exists key 判断某个key是否存在
    type key 查看指定的key的类型
    del key 删除指定的key数据
    unlink key 非阻塞删除, 仅仅将keys从keyspace元数据中删除, 真正的删除会在后续异步操作中进行
    ttl key 查看还有多少秒过期, -1表示永不过期, -2表示已过期
    expire key 秒钟 为给定的key设置过期时间
    move key dbindex [0-15] 将当前数据库的key移动到给定的数据库db
    select dbindex 切换数据库[0-15], 默认为0
    dbsize 查看当前数据库key的数量
    flushdb 清空当前库
    fushall 通杀全部库

## 4. RDB
    6.0.16以下:  900s/1次; 300s/10次; 60s/10000次
    6.2以及7以上: 3600s/1次; 300s/100次; 60s/10000次

## 5. RDB和AOF
    1. RDB 是快照模式, 在6.0.16及以下版本中, 默认是每900s/1次, 300s/10次, 60s/10000次, 在6.2以及7以上, 默认是3600s/1次, 300s/100次, 60s/10000次, 生成的备份文件名为dump+端口号.rdb,可以在dbfilename属性中自定义文件名,  以压缩二进制方式写入, 对性能损耗较低, , 可以执行redis-check-rdb进行rdb文件进行修复, 手动保存命令为save(生产禁用, 阻塞模式进行保存)和bgsave
    2. AOF, append only file, 是以追加的形式进行文件备份, 在7以前, 是生成一个文件, 7以后备份文件生成了3个, 以base, incy,manifest组成, 在配置文件中设置appendonly为yes可以开启aof模式, 优先级比RDB高, 在备份文件大小达到设置的rewrite值之后, 会后台线程进行rewrite,  将incr的增量文件写入到main文件中, 每一次rewrite, 版本号都会+1,  可以使用redis-check-aof —fix命令对aof文件进行修复, 默认有3种持久化方式: 永不持久化, 1秒一次持久化, 每一次操作都进行持久化, 默认使用1秒一次持久化方式, 相比较而言, AOF以原始命令方式写入, 性能损耗更高一些, 先写入缓冲区, 写完之后再持久化到aof文件, 可在appendfilename中自定义aof文件名, 手动保存命令为rewriteaof
      RDB和AOF可以一起使用,  AOF模式的优先级更高, 比如备份文件恢复, 会优先使用AOF文件进行恢复, aof-use-rdb-preamble yes 表示在AOF文件开头包含RDB持久化的数据, AOF重写时, 可以通过加载RDB来还原数据, 从而加快还原的速度
   
## 6. 主从复制原理和工作流程:
    1. Slave刚启动的时候, 会向master发起申请
    2. 首次连接, 是全量进行数据同步
    3. 默认是每10秒进行一次心跳检测
    4. 全量完成之后, 正常运行是增量进行复制
    5. 如果slave挂了又重新启动, 会重新连接master, 根据offset偏移量进行增量复制

## 7. 哨兵模式选举过程:
    1. 主观下线, 默认是30秒, sentinel down-after-milliseconds
    2. 客观下线, quorum是客观下线的依据, 法定人数
    3. 选举出领导者哨兵leader, Raft算法
    4. 由哨兵leader开始推动故障切换流程, 推举出一个master

## 8. master选举过程:
    1. 哨兵指定, 权限高的优先, replica-priority, 数值越小, 优先级越高
    2. 权限一致, 看replication offset, replication offset大的入选
    3. replication offset一致, 选举Run ID最小的为master, 字典顺序ASCII码
    4. 哨兵leader执行slaveof no one 从slave变为master
    5. 哨兵leader向其它slave发送命令,  执行slaveof命令让其他节点变更所属的master
    6. 老master节点重新上线以后, leader会让老master降级为slave

## 9. 哨兵使用建议:
    1. 数量为多个, 奇数, 本身应该集群
    2. 各节点配置应该一致
    3. 如果哨兵节点部署在Docker等容器里, 要注意端口的正确映射
    4. 哨兵集群+主从复制, 并不能保证数据零丢失, master挂了之后到选出新的master的时间中, 无法写入新数据

## 10. 一致性哈希算法:
    1. 构建哈希环：将32位哈希空间划分为一个环，每个节点和数据都在环上有一个对应的哈希值。
    2. 添加节点：将节点的哈希值添加到哈希环上。
    3. 数据映射：对于每个数据项，计算其哈希值，并将其映射到离该哈希值最近的节点（顺时针方向）上。这样就确定了数据项在环上的落点。
    4. 数据存储：将数据项存储在与其映射的节点上。
    5. 节点故障处理：当节点故障时，其对应的哈希值将从哈希环上移除。数据项会被重新映射到下一个最近的节点上，以保持数据的可用性。
    6. 缺点: 如果节点过少, 可能存在数据倾斜问题

2^14=16384
hash_slot=CRC16(key) mod 16384

redis集群:
槽位 16384 , 建议节点数小于等于1000

## 11. 槽位为什么设置为16384:
    1. 如果槽位位65536, 发送心跳信息的消息头达8k, 发送的心跳包过于庞
        1. 在消息头中最占空间的是myslots[CLUSTER_SLOTS/8]; 槽位位65536时, 这块大小是65536/8/1024 = 8kb; 槽位是16384时, 这块大小是16384/8/1024=2kb; 因为每秒钟, redis节点需要发送一定数量的ping消息作为心跳包, 如果槽位为65536, 这个ping消息的消息头太大了, 浪费带宽
    2. redis集群的主节点数量基本不可能超过1000个
        1. 集群节点越多, 心跳包的消息体内携带的数据越多, 如果节点超过1000个, 会导致网络拥堵, 对于在1000个节点以内的redis cluster集群, 16384个槽位够用了, 没有必要扩展到65536个
    3. 槽位越小, 节点越少的情况下压缩比高, 容易传输
        1. Redis主节点的配置信息中它所负责的哈希槽是通过一张bitmap的形式来保存的, 在传输过程中, 会对bitmap进行压缩, 但是如果bitmap的填充率slots/N很高的话, (N表示节点数), bitmap的压缩率就很低, 如果节点数很少, 而哈希槽数量很多的话, bitmap的压缩率就很低

## 12. Redis集群不保证强一致性, 意味着在特定的条件下, redis集群可能会丢掉一些被系统收到的写入请求命令

## 13. 创建集群:
    redis-cli -a Td@123456 --cluster create --cluster-replicas 1 10.57.16.186:6381 10.57.16.186:6382 10.57.16.118:6383 10.57.16.118:6384 10.57.17.164:6385 10.57.17.164:6386
    Cluster nodes/info 查看集群状态
    info repliction 查看节点状态

## 14. 连接集群:
    redis-cli -a Td@123456 -p 6381 -c(路由)

## 15. 查看key槽位
    CLUSTER KEYSLOT key

## 16. 集群节点从属调整
    CLUSTER FAILOVER

## 17. 添加节点:
    添加节点到集群
    redis-cli -a Td@123456 --cluster add-node 10.57.17.130:6387 10.57.16.186:6381

## 18. 查看集群状态
    redis-cli -a Td@123456 --cluster check 10.57.16.186:6381

## 19. 重新分配槽位 reshard
    重新分配成本太高, 从原每个节点分别匀出来一部分给新的节点

    redis-cli -a Td@123456 --cluster reshard 10.57.16.186:6381
    1. 槽位平均数
    2. 谁接收
    3. all

## 20. 挂载slave节点到集群
    redis-cli -a Td@123456 --cluster add-node 10.57.17.130:6388 10.57.17.130:6387 --cluster-slave --cluster-master-id b79283d229484c34bb4b5efee34fafcfb6a13b58

## 21. 删除节点:
    删除从节点:
    redis-cli -a Td@123456 --cluster del-node 10.57.17.130:6388 2df84712b6e60c4b0daf5af17b3b4bbab98d2937(要删除的节点id)

## 22. 清空槽位, 重新分配
    redis-cli -a Td@123456 --cluster reshard 10.57.16.186:6381
    (done)

## 23. 删除多余从节点

## 24. 不在同一个slot槽位下的键值无法使用批处理命令(eg:mget)
    Redis集群有16384个哈希槽, 每个key通过CRC16校验后对16384取模来决定放置在哪个槽位, 集群的每个节点负责一部分hash槽

## 25. 集群常用命令:
    1. 集群是否完整才能对外提供服务: cluster-require-full-coverage yes/no
    2. CLUSTER  COUNTKEYSINSLOT 槽位数字编号
    3. CLUSTER KEYSLOT 键名称

## 26. spring boot 集成 redis
    Jedis->luttuce->Redis Template

## 27. redis4之后开始慢慢支持多线程, redis6/7之后才稳定

## 28. redis4以前为什么单线程的时候还很快?
    1. 基于内存操作: Redis的所有数据都是存放在内存中的, 因此所有运算都是基于内存级别, 所以性能比较高
    2. 数据结构简单: Redis的数据结构是专门设计的, 而这些简单的数据结构的查找和操作的时间复杂度都是O(1), 因此性能比较高
    3. 多路复用和非阻塞I/O: Redis使用I/O多路复用功能来监听多个socket连接客户端, 这样就可以使用一个线程连接来处理多个请求, 较少线程切换带来的开销, 同时也避免了I/O阻塞操作
    4. 避免上下文切换: 因为是单线程模型, 因此就避免了不必要的上下文切换和多线程竞争, 这样就省去了多线程切换带来的时间和性能上的消耗, 而且单线程不会导致死锁问题

## 29. redis4以前一直使用单线程的原因?
    1. 使用单线程模型使Redis的开发和维护更简单, 单线程更容易开发和调试
    2. 即时使用单线程也能处理客户端的请求, 因为使用的是I/O多路复用和非阻塞I/O
    3. 对于Redis来说, 性能的瓶颈是内存和网络带宽, 而非CPU

## 30. 单线程的痛点是? 为什么不得不修改?
    比如大key的del, 会阻塞线程, 类似于加上了一个synchronized锁, 使用unlink key

## 31. Redis6以后, 采用多个IO线程来处理网络请求, 提高网络请求的并行度; 对于读写操作命令仍然是单线程处理

## 32. Redis主线程和IO线程处理请求的过程:
    1. 服务端与客户端建立socket连接, 并分配处理线程
        首先, 主线程负责接收建立连接请求, 当有客户端和实例建立socket连接时, 主线程会创建和客户端的连接, 并把socket放入全局等待队列中, 紧接着, 主线程通过轮询的方式把socket连接分配给IO线程
    2. IO线程读取并解析请求
        主线程一旦把socket连接分配给IO线程, 就会进入阻塞状态, 等待IO线程完成客户端请求读取和解析, 因为有多个IO线程在并行处理, 所以这个操作很快就能完成
    3. 主线程执行请求操作
        等到IO线程解析完请求, 主线程还是会以单线程的方式执行这些命令操作
    4. IO线程回写socket和主线程清空全局队列
        当主线程执行完请求操作后, 会把需要的返回结果写入缓冲区, 然后主线程会阻塞等待IO线程, 把这些结果回写到socket中, 并返回给客户端. 和IO线程读取和解析请求一样, IO线程回写socket时, 也是有多个线程在并发执行, 所以回写socket的速度也很快, 等到IO线程回写socket完毕, 主线程会清空全局队列, 等待客户端的后续请求

## 33. unix编程中的5种IO模型
    1. 阻塞IO
    2. 非阻塞IO
    3. IO多路复用
        一种同步的IO模型, 实现一个线程监听多个FD文件句柄, 一旦某个FD文件句柄准备就绪, 就能通知对应的应用程序进行相应的读写操作, 没有FD文件句柄就绪时, 就会阻塞应用程序, 从而释放CPU资源
        select->poll->epoll
    4. 信号驱动IO
    5. 异步IO

## 34. redis为什么这么快?
    IO多路复用+epoll函数使用, 才是redis这么快的直接原因, 而不是仅仅单线程命令+安装在内存中

## 35. redis7默认关闭多线程, io-threads 配置线程数; io-threads-do-reads yes/no  启动/关闭

## 36. redis批量写入数据 
    cat 目标文件 | redis-cli -h 10.57.16.186 -p 6381 -a Td@123456 --pipe

## 37. redis禁用危险命令
    SECURITY项, 禁用的设为""空字符串
    rename command keys ""
    rename command flushdb ""
    rename command flushall ""

## 38. keys * 生产禁用, 不用keys * 使用scan命令
    SCAN cursor [MATCH pattern] [COUNT count]

## 39. 怎么查找bigkey
    1. redis-cli --bigkeys
    2. MEMORY USAGE key

## 40. 非字符串的bigkey, 禁止使用del删除, 建议渐进式删除; eg: hscan, zscan, sscan等
    1. String, 一般del, 如果过于庞大, 使用unlink
    2. hash, 使用hscan每次获取少量field-value, 再使用hdel删除每个field, 最后再del
    3. list, 使用ltrim渐进式逐步删除
    4. set, 使用sscan渐进式逐步删除
    5. zset, 使用zscan渐进式逐步删除

## 41. 数据一致性?
    双检加锁策略? 
    延迟双删->watchDog,  第二次异步删除? 
    ! 先写入数据库, 再删除缓存, 最终一致性(实时性要求并不是很高)? -> 消息中间件解决
    强一致性: 先写入数据库, 等数据库写入完成, 并且删除完缓存之后, 再允许读取数据

## 42. canal? MYSQL->Redis/ES/MYSQL
        mysql: 
            1. show version();
            2. show master status;
            3. show variables like 'log_bin'; -> on
            4. show variables like 'binlog_format'; -> RAW
            5. show variables like 'server_id'; 不要和canal的slave_id重复
            6. 授权canal访问mysql->8.0以上
                DROP USER IF EXISTS 'canal'@'%';
                CREATE USER 'canal'@'%' IDENTIFIED BY 'canal';
                GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' WITH GRANT OPTION;
                FLUSH PRIVILEGES;

                SELECT * FROM mysql.user;
            7. canal下载
                https://github.com/alibaba/canal/releases/tag/canal-1.1.6 -> developer
            8. 修改canal配置
                /canal/conf/example/instance.properties ->  canal.instance.master.address
            9. 启动
                ./canal/bin/start.sh
            