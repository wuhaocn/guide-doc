dubbo泛化调用
 dubbo  968 次阅读  ·  读完需要 5 分钟
一.前言
　 传统的dubbo服务面向接口编程,如果需要调用其他服务则需要引入该服务对应的接口!但是有时候在遇到语言调用的时候,比如一个PHP工程师调用dubbo服务时由于dubbo自身并非跨平台,因此只能对外提供一个controller然后引入对应的dubbo服务!
那么泛化调用的出现完美的解决了这个问题,调用方并不需要引入服务接口也可以完成调用!只需要对外暴露一个统一的接口,即可完成不同dubbo服务间的调用,实现了之前的跨平台需求!
二.使用
本示例基于API方式进行泛化调用

@RestController
public class DemoConsumerController {
    
    private static Logger logger = LoggerFactory.getLogger(DemoConsumerController.class);

    @RequestMapping(value="/call",method=RequestMethod.POST)
    public Object get(@RequestBody DubboDTO dto) {

        // 创建服务实例
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setGeneric(true);
        reference.setInterface(dto.getInterfaceName());
        reference.setVersion(dto.getVersion());
        
        // 获取缓存中的实例
        ReferenceConfigCache cache = ReferenceConfigCache.getCache(); 
        GenericService genericService = cache.get(reference);
        
        // 调用实例
        Object result = genericService.$invoke(dto.getMethod(), dto.getParameterTypes(),dto.getArgs());
        logger.info(">>>>>调用dubbo服务接口,入参:{},出参:{}",dto.toString(),result);
        return result;
    }
}
三.注意点
ReferenceConfig是一个特别重的实例,它里面封装了所有与注册中心及服务提供方连接,所以在使用的时候要注意缓存.如果直接使用reference.get()方法而不是从ReferenceConfigCache.getCache()获取,那么在dubbo管控台会看到每一次调用都会生成一个服务消费者实例.而堆内存也随着请求的不断增加而变大
管控台

堆内存
