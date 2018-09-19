# elastic-sharding

为数据处理集群所有节点动态配置分片的组件，自动故障转移，重新分片。

## 背景

应对不断增长的业务流量，扩容几乎是互联网最常见的话题之一。对于无状态的业务系统来说，当然能够通过加机器进行解决，这也就不是问题了。

但是对于有些系统，例如数据处理系统，要求数据的处理不能重复并还有一定的处理时限要求，那么仅仅增加机器就不一定能达到增强系统的处理能力的目的，这就需要可靠的并行处理方案来支撑，这样才能够充分利用多节点并行的优势。

## 解决方案

**分布式系统增加节点的目的就是为了提高集群的并行度，增强集群的并行处理能力 。**

然而，要确保数据能够不争、不抢、不重复处理，就需要分析数据有哪些可以被并行处理的维度，然后指定不同的节点操作不同维度的数据，这样就达到并行处理数据的目的。
维度是什么？例如：男、女就是以性别为维度进行区分的。
当然我们最常听说的数据区分的维度，可能就是分片，当然咯，因系统而异，有的存在维度类型、角色等等。

**但对节点和分片之间如何匹配，在不同的系统，不同的环境、不同的需求下，会选择不同的处理方式，下面是常用到的处理方式：**

- 对于只能单机处理的数据，例如一个请求来了大批量数据，需要并行处理，可以考虑主线程进行数据的分片，工作线程负责处理每片的数据，并发增强处理能力。
- 对于节点少的分布式系统，可能需要处理的数据并不是很大，可以使用资源的抢占（类似锁的实现），也是不错的实现方案。
- 对于节点多，并行度要求比较高，数据量比较大的系统，就需要对节点处理的分片预先分配，避免抢占产生性能损耗和时延。

### 优缺点比较

方式1、主线程分片，工作线程并发处理的思路，单机处理简单的，集群处理困难。
方式2、所有节点都会争抢相同的锁，那么所有节点都会遍历全量的锁，这种实现有些不够优雅，也会造成对锁的竞争比较激烈，会考验锁的实现，但好在实现简单，对于数据量不大的需求，还是不错的实现方式。但是在节点多分片多的时候会有以下缺陷：一是全量遍历浪费资源、二效率不高、三是会占用线程去抢占锁，造成线程被占用，可能造成数据处理不及时；
方式3、确实是性能最优的方案，但是提前分片就不得不考虑，节点宕机，需要重新分片的问题，要实现稳定的可靠的系统编码会有些复杂的。


**对于使用方式3进行数据处理的系统，主要的难点在于实现分配和重分配的逻辑，当然亦可以使用现成的组件 *《elastic-sharding》***

## 《elastic-sharding》节点分片，自动重新分片组件

elastic-sharding是基于zookeeper实现的动态分配分片的组件，只需要简单的实例化对象，设置总分片，调用对象的模版方法，传递自己的业务逻辑，节点运行时，组件会计算出当前节点所分配的片号。

组件是分布式的无中心化的，故障自动冲洗分配的节点与分片的组件，《elastic-sharding》。

组件特点：

1. 完全的包装了分配与再分配的实现细节。
2. 依赖简单，仅仅配置zookeeper。
3. 弹性扩容缩容：一旦有新执行器机器上线或者下线，下次调度时将会重新为节点分片；
4. 使用简单，只需要引入组件提供的模板方法，实现接口进行业务处理，组件会将分片序号自动传递。

### 接入指南
#### 接入步骤
1. git clone https://github.com/esiyuan/elastic-sharding/tree/master
2. mvn clean install -DskipTests
3. pom.xml 引入

#### 使用样例
1. 创建ZookeeperCoordinator对象
```
        ZookeeperCoordinator zookeeperCoordinator = new ZookeeperCoordinator.Builder()
                .connectString("192.168.0.113:2181")
                .namespace("elasticshardingDemo")
                .sessionTimeoutMs(1000).connectionTimeoutMs(1000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

```

2. 创建组件模板方法并启动
```
        final ElasticShardingTemplate elasticShardTemplate = ElasticShardingTemplate.newInstance(
                new ElasticShardingFacade(zookeeperCoordinator), 4);  // 4时片的总数，应该和实际的分片数目一样
        elasticShardTemplate.start();
```
3. 通过模板进行业务处理
```
                elasticShardTemplate.execute(new ElasticShardingTemplate.ShardOperator() {
                    //integer为传递给业务逻辑的分片号，一个节点可能会处理多个分片号，会使用多线程的方式触发业务代码
                    @Override
                    public void operate(Integer integer) {
                        System.out.println(InstanceService.getLocalInstanceId() + "--" + integer);
                    }
                });
```

#### 一下是一个可以执行的样例
```
public class ElasticSharingDemo {

    public static void main(String[] args) throws IOException {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(5);

        ZookeeperCoordinator zookeeperCoordinator = new ZookeeperCoordinator.Builder()
                .connectString("192.168.0.113:2181")
                .namespace("elasticshardingDemo")
                .sessionTimeoutMs(1000).connectionTimeoutMs(1000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();


        final ElasticShardingTemplate elasticShardTemplate = ElasticShardingTemplate.newInstance(
                new ElasticShardingFacade(zookeeperCoordinator), 4);
        elasticShardTemplate.start();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("time = " + System.currentTimeMillis());
                elasticShardTemplate.execute(new ElasticShardingTemplate.ShardOperator() {
                    @Override
                    public void operate(Integer integer) {
                        System.out.println(InstanceService.getLocalInstanceId() + "--" + integer);
                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
```
**运行多个虚拟机，会把4片平均的分配到不同的虚拟机上，如果虚拟机数量多于4则序号大于4的虚拟机则不会处理。**
        
