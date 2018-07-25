#### consumer,producer,server配置文件信息

- consumer.properties

  ```
  # zookeeper连接服务器地址
  zookeeper.connect=zk01:2181,zk02:2181,zk03:2181

  # zookeeper的session过期时间，默认5000ms，用于检测消费者是否挂掉
  zookeeper.session.timeout.ms=5000

  #当消费者挂掉，其他消费者要等该指定时间才能检查到并且触发重新负载均衡
  zookeeper.connection.timeout.ms=10000

  # 指定多久消费者更新offset到zookeeper中。注意offset更新时基于time而不是每次获得的消息。一旦在更新zookeeper发生异常并重启，将可能拿到已拿到过的消息
  zookeeper.sync.time.ms=2000

  #指定消费组
  group.id=xxx

  # 当consumer消费一定量的消息之后,将会自动向zookeeper提交offset信息 
  # 注意offset信息并不是每消费一次消息就向zk提交一次,而是现在本地保存(内存),并定期提交,默认为true
  auto.commit.enable=true

  # 自动更新时间。默认60 * 1000
  auto.commit.interval.ms=1000

  # 当前consumer的标识,可以设定,也可以由系统生成,主要用来跟踪消息消费情况,便于观察
  conusmer.id=xxx

  # 消费者客户端编号，用于区分不同客户端，默认客户端程序自动产生
  client.id=xxxx

  # 最大取多少块缓存到消费者(默认10)
  queued.max.message.chunks=50

  # 当有新的consumer加入到group时,将会reblance,此后将会有partitions的消费端迁移到新的consumer上,如果一个consumer获得了某个partition的消费权限,那么它将会向zk注册 "Partition Owner registry"节点信息,但是有可能此时旧的consumer尚没有释放此节点, 此值用于控制,注册节点的重试次数. 
  rebalance.max.retries=5

  # 获取消息的最大尺寸,broker不会像consumer输出大于此值的消息chunk 每次feth将得到多条消息,此值为总大小,提升此值,将会消耗更多的consumer端内存
  fetch.min.bytes=6553600

  # 当消息的尺寸不足时,server阻塞的时间,如果超时,消息将立即发送给consumer
  fetch.wait.max.ms=5000
  socket.receive.buffer.bytes=655360

  # 如果zookeeper没有offset值或offset值超出范围。那么就给个初始的offset。有smallest、largest、anything可选，分别表示给当前最小的offset、当前最大的offset、抛异常。默认largest
  auto.offset.reset=smallest

  # 指定序列化处理类
  derializer.class=kafka.serializer.DefaultDecoder
  ```

  ​

- producer.properties

  ```
  #指定kafka节点列表，用于获取metadata，不必全部指定
  metadata.broker.list=kafka01:9092,kafka02:9092

  # 指定分区处理类。默认kafka.producer.DefaultPartitioner，表通过key哈希到对应分区
  #partitioner.class=kafka.producer.DefaultPartitioner

  # 是否压缩，默认0表示不压缩，1表示用gzip压缩，2表示用snappy压缩。压缩后消息中会有头来指明消息压缩类型，故在消费者端消息解压是透明的无需指定。
  compression.codec=none

  # 指定序列化处理类
  serializer.class=kafka.serializer.DefaultEncoder

  # 如果要压缩消息，这里指定哪些topic要压缩消息，默认empty，表示不压缩。
  #compressed.topics=

  # 设置发送数据是否需要服务端的反馈,有三个值0,1,-1
  # 0: producer不会等待broker发送ack 
  # 1: 当leader接收到消息之后发送ack 
  # -1: 当所有的follower都同步消息成功后发送ack. 
  request.required.acks=0 

  # 在向producer发送ack之前,broker允许等待的最大时间 ，如果超时,broker将会向producer发送一个error ACK.意味着上一次消息因为某种原因未能成功(比如follower未能同步成功) 
  request.timeout.ms=10000

  # 同步还是异步发送消息，默认“sync”表同步，"async"表异步。异步可以提高发送吞吐量,
  也意味着消息将会在本地buffer中,并适时批量发送，但是也可能导致丢失未发送过去的消息
  producer.type=sync

  # 在async模式下,当message被缓存的时间超过此值后,将会批量发送给broker,默认为5000ms
  # 此值和batch.num.messages协同工作.
  queue.buffering.max.ms = 5000

  # 在async模式下,producer端允许buffer的最大消息量
  # 无论如何,producer都无法尽快的将消息发送给broker,从而导致消息在producer端大量沉积
  # 此时,如果消息的条数达到阀值,将会导致producer端阻塞或者消息被抛弃，默认为10000
  queue.buffering.max.messages=20000

  # 如果是异步，指定每次批量发送数据量，默认为200
  batch.num.messages=500

  # 当消息在producer端沉积的条数达到"queue.buffering.max.meesages"后 
  # 阻塞一定时间后,队列仍然没有enqueue(producer仍然没有发送出任何消息) 
  # 此时producer可以继续阻塞或者将消息抛弃,此timeout值用于控制"阻塞"的时间 
  # -1: 无阻塞超时限制,消息不会被抛弃 
  # 0:立即清空队列,消息被抛弃 
  queue.enqueue.timeout.ms=-1


  # 当producer接收到error ACK,或者没有接收到ACK时,允许消息重发的次数 
  # 因为broker并没有完整的机制来避免消息重复,所以当网络异常时(比如ACK丢失) 
  # 有可能导致broker接收到重复的消息,默认值为3.
  message.send.max.retries=3

  # producer刷新topic metada的时间间隔,producer需要知道partition leader的位置,以及当前topic的情况 
  # 因此producer需要一个机制来获取最新的metadata,当producer遇到特定错误时,将会立即刷新 
  # (比如topic失效,partition丢失,leader失效等),此外也可以通过此参数来配置额外的刷新机制，默认值600000 
  topic.metadata.refresh.interval.ms=60000
  ```

  ​

- server.properties

```
#broker的全局唯一编号，不能重复
broker.id=0

#用来监听链接的端口，producer或consumer将在此端口建立连接
port=9092

#处理网络请求的线程数量
num.network.threads=3

#用来处理磁盘IO的线程数量
num.io.threads=8

#发送套接字的缓冲区大小
socket.send.buffer.bytes=102400

#接受套接字的缓冲区大小
socket.receive.buffer.bytes=102400

#请求套接字的缓冲区大小
socket.request.max.bytes=104857600

#kafka消息存放的路径
log.dirs=/export/servers/logs/kafka

#topic在当前broker上的分片个数
num.partitions=2

#用来恢复和清理data下数据的线程数量
num.recovery.threads.per.data.dir=1

#segment文件保留的最长时间，超时将被删除
log.retention.hours=168

#滚动生成新的segment文件的最大时间
log.roll.hours=168

#日志文件中每个segment的大小，默认为1G
log.segment.bytes=1073741824

#周期性检查文件大小的时间
log.retention.check.interval.ms=300000

#日志清理是否打开
log.cleaner.enable=true

#broker需要使用zookeeper保存meta数据
zookeeper.connect=192.168.52.106:2181,192.168.52.107:2181,192.168.52.108:2181

#zookeeper链接超时时间
zookeeper.connection.timeout.ms=6000

#partion buffer中，消息的条数达到阈值，将触发flush到磁盘
log.flush.interval.messages=10000

#消息buffer的时间，达到阈值，将触发flush到磁盘
log.flush.interval.ms=3000

#删除topic需要server.properties中设置delete.topic.enable=true否则只是标记删除
delete.topic.enable=true

#此处的host.name为本机IP(重要),如果不改,则客户端会抛出:Producer connection to localhost:9092 unsuccessful 错误!
host.name=kafka01
```

