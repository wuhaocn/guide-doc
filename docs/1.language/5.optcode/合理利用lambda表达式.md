# 1.使用 lambda 替代设计模式

connectproxy 利用 lambda 做责任链模式。可以把所有 http 请求的合法性校验做成类似 servlet 的过滤器模式，例如跨域，认证等。这里用 lambda 表达式做更简单，也不用像老的责任链模式，继承接口，然后构造。

- 改造后，踢出掉 request if else 判断。增加其他校验，只需要找到 UirHanlder 增加对应的枚举值，并增加到 stream 的集合里。

```java
public enum UriHandler {

    /**
     * 检查uri是否可用
     */
    URI_CHECK((Pair<ChannelHandlerContext, FullHttpRequest> param) -> {
        Optional<Boolean> optional;
        try {
            new URI(param.getSecond().uri());
            optional = Optional.empty();
        } catch (URISyntaxException e) {
            optional = Optional.of(true);
        }
        return optional;
    }),
    /**
     * 解码失败请求处理
     */
    DECODE_FAILURE((Pair<ChannelHandlerContext, FullHttpRequest> param) -> {
        if (!param.getSecond().decoderResult().isSuccess()) {
            ResponseHelper.respWithBody(param.getFirst(), param.getSecond(),
                new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return Optional.of(true);
        }
        return Optional.empty();
    }),

    /**
     * 健康检查
     */
    CHECKUP((Pair<ChannelHandlerContext, FullHttpRequest> param) -> {
        try {
            if (RequestFilter.HEALTH_CHECK_PATH.equals(new URI(param.getSecond().uri()).getPath())
                && param.getSecond().method() == HttpMethod.HEAD) {

                ResponseHelper
                    .respClose(param.getFirst(), new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK));

                return Optional.of(true);
            }
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }),

    /**
     * 处理客户端发送上来的ping
     */
    PING((Pair<ChannelHandlerContext, FullHttpRequest> param) -> {
        try {
            if (param.getSecond().method() == GET && RequestFilter.PING_PATH
                .equals(new URI(param.getSecond().uri()).getPath())) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
                response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");
                response.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                ResponseHelper.respClose(param.getFirst(), response);

                return Optional.of(true);
            }
        } catch (URISyntaxException e) {
            return Optional.empty();
        }

        return Optional.empty();
    }),

    /**
     * 非法资源访问
     */
    METHOD_ILLEGAL((Pair<ChannelHandlerContext, FullHttpRequest> param) -> {
        if (param.getSecond().method() != GET) {
            ResponseHelper.respWithBody(param.getFirst(), param.getSecond(),
                new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return Optional.of(true);
        }

        return Optional.empty();
    });

    private final Function<Pair<ChannelHandlerContext, FullHttpRequest>, Optional<Boolean>> handler;

    UriHandler(Function<Pair<ChannelHandlerContext, FullHttpRequest>, Optional<Boolean>> handler) {
        this.handler = handler;
    }

    public Function<Pair<ChannelHandlerContext, FullHttpRequest>, Optional<Boolean>> getHandler() {
        return handler;
    }
}
```

```java
/**
 * 类似servlet filter模式， 使用lambda 模拟责任链模式
 *
 * @author zhaodong
 */
public class RequestFilter {

    public static final String HEALTH_CHECK_PATH = "/health";
    public static final String PING_PATH = "/ping";

    public Optional<Boolean> dispatch(ChannelHandlerContext ctx, FullHttpRequest request, ProxyType proxyType) {
        //创建过滤器
        Stream<Function<Pair<ChannelHandlerContext, FullHttpRequest>, Optional<Boolean>>> filter;
        if (proxyType == ProxyType.WEBSOCKET) {
            filter = Stream
                .of(UriHandler.URI_CHECK.getHandler(), UriHandler.DECODE_FAILURE.getHandler(),
                    UriHandler.CHECKUP.getHandler(),
                    UriHandler.METHOD_ILLEGAL.getHandler(), UriHandler.PING.getHandler());
        } else {
            filter = Stream.of(UriHandler.DECODE_FAILURE.getHandler(), UriHandler.CHECKUP.getHandler(),
                UriHandler.PING.getHandler());
        }

        //过滤出合适的handler处理请求
        return filter.map(f -> f.apply(Pair.of(ctx, request))).filter(Optional::isPresent).findFirst()
            .flatMap(Function.identity());
    }
}
```

# 2.使用 lambda 更好的抽象业务

connectmanager 使用了 caffeince 做缓存，使用 lambda 表达式，更简单的描述了缓存使用规则

```java
/**
 * 缓存管理:一级缓存, 二级缓存, 三级缓存
 * <p>
 * 一级缓存是caffeine 二级缓存是redis 三级缓存是db
 *
 * <p>
 * 首先去caffeine取，取到了写值进去，取不到写占位进去，避免重复去redis写。 redis 取到了写值进去，取不到写占位进去，避免重复去数据库读。 db 有值返回值
 *
 * @author zhaodong
 */
public class RongCacheBuilder {

    private RongCacheBuilder() {
    }

    public static <K extends CacheKey, V extends CacheValue> AsyncLoadingCache<K, V> build(
        Function<K, Optional<V>> redisReader,
        BiPredicate<K, V> redisWriter,
        Function<K, Optional<V>> dbLoader, V defaultValue, int cacheExpireTime, long cacheSize) {

        Function<K, V> redisWorker = key -> {
            Optional<V> result = redisReader.apply(key);
            return result.orElseGet(() -> {
                Optional<V> dbResult = dbLoader.apply(key);

                V value = null;
                value = dbResult.orElse(defaultValue);
                if(!redisWriter.test(key, value)){
                    return defaultValue;
                }

                return  value;
            });
        };

        return Caffeine.newBuilder().expireAfterAccess(cacheExpireTime, TimeUnit.MINUTES).maximumSize(cacheSize)
            .buildAsync(redisWorker::apply);
    }

}
```

- 构建了缓存体系。caffience 本地缓存作为一级缓存，redis 作为二级缓存，使用数据库作为三级缓存。
- 将 redis 读写，数据库读取抽象给业务层。本类构建时只关注读写机制。那么所有业务均可复用此构建机制，自己业务传对应的函数。
- 利用 Optional 减少 if else 嵌套。

```java
/**
 * @author zhaodong
 */
public class UserCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCacheManager.class);

    private static final int REDIS_EXPIRED_TIME = 3600 * 24;

    /**
     * cache Item 过期时间, 30分钟
     */
    private static final int CACHE_EXPIRED_TIME = 30;

    private static final long CACHE_SIZE = 500000;

    public static final String REDIS_PREFIX = "ui";

    private UserCacheManager() {
        throw new IllegalStateException("Utility class");
    }

    private static ShardedJedis getRedisResource() {
        ShardedJedisPool shardedJedisPool = RedisPoolManager.getInstance()
            .getShardedJedisPool(RedisConstants.USER_INFO);
        return shardedJedisPool.getResource();
    }

    private static final Function<UserCacheKey, Optional<UserCacheValue>> REDIS_READER = k -> {
        ShardedJedis shardedJedis = getRedisResource();

        Map<String, String> fields = shardedJedis.hgetAll(k.toId());
        if (fields == null || fields.isEmpty()) {
            return Optional.empty();
        }

        UserCacheValue userCacheValue = new UserCacheValue(fields);
        return Optional.of(userCacheValue);
    };

    private static final BiPredicate<UserCacheKey, UserCacheValue> REDIS_WRITER = UserCacheManager::test;

    private static boolean test(UserCacheKey userCacheKey, UserCacheValue userCacheValue) {
        if (userCacheValue == null || !userCacheValue.isHasValue()) {
            return false;
        }

        ShardedJedis shardedJedis = getRedisResource();
        boolean result = false;
        try {
            if (userCacheValue.getBlockTime() != null) {
                shardedJedis.hset(userCacheKey.toId(), String.valueOf(KVBusType.CMP_FORBID_USER.getType()),
                    String.valueOf(userCacheValue.getBlockTime()));
            }

            if (userCacheValue.getExpireTime() != null) {
                shardedJedis.hset(userCacheKey.toId(), String.valueOf(KVBusType.CMP_TOKEN_EXPIRE.getType()),
                    String.valueOf(userCacheValue.getExpireTime()));
            }

            if (userCacheValue.getWhite() != null && userCacheValue.getWhite()) {
                shardedJedis.hset(userCacheKey.toId(), String.valueOf(KVBusType.CMP_WHITE_USER.getType()),
                    String.valueOf(userCacheValue.getWhite()));
            }

            shardedJedis.expire(userCacheKey.toId(), REDIS_EXPIRED_TIME);
            result = true;
        } catch (Exception e) {
            LOGGER.error("{} write redis error, key:{}, value:{}", RedisConstants.USER_INFO, userCacheKey.toId(),
                REDIS_EXPIRED_TIME, e);
        }
        return result;
    }


    private static final Function<UserCacheKey, Optional<UserCacheValue>> DB_LOADER = key -> {
        if (key.getAppId() == 0L) {
            LOGGER.error("db Load param {} error appId is 0, key:{}", KVBusType.CMP_TOKEN_EXPIRE, key);
            return Optional.empty();
        }

        List<KeyValueExt> result = null;
        try {
            result = KeyValueExt
                .getKeyValueExts(key.getAppId(), key.getUserId(), KVBusType.CMP_FORBID_USER, KVBusType.CMP_WHITE_USER,
                    KVBusType.CMP_TOKEN_EXPIRE);
        } catch (ServerException e) {
            LOGGER.error("db query {} error , key:{}", KVBusType.CMP_TOKEN_EXPIRE.name(), key);
        }
        if (result != null && !result.isEmpty()) {
            UserCacheValue userConfig = new UserCacheValue(result);
            return Optional.of(userConfig);
        }
        return Optional.empty();
    };

    private static final AsyncLoadingCache<UserCacheKey, UserCacheValue> USER_INO_CACHE = RongCacheBuilder
        .build(REDIS_READER, REDIS_WRITER, DB_LOADER, new UserCacheValue(), CACHE_EXPIRED_TIME, CACHE_SIZE);


    public static void check(long appId, String userId, Consumer<UserCacheValue> consumer) {
        UserCacheKey userCacheKey = UserCacheKey.of(appId, userId);
        CompletableFuture<UserCacheValue> tokenExpireFuture = USER_INO_CACHE.get(userCacheKey);
        tokenExpireFuture.thenAccept(consumer);
    }
```
