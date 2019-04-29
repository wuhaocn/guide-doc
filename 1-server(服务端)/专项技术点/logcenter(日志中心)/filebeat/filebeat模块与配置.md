#### Filebeat 模块与配置

##### 1.关于Filebeat

    当你要面对成百上千、甚至成千上万的服务器、虚拟机和容器生成的日志时，请告别 SSH 吧！Filebeat 将为你提供一种轻量型方法，用于转发和汇总日志与文件，让简单的事情不再繁杂。
    关于Filebeat，记住两点：
    轻量级日志采集器
    输送至 Elasticsearch 或 Logstash，在 Kibana 中实现可视化
 
##### 2.Filebeat是如何工作的

    Filebeat由两个主要组件组成：inputs 和  harvesters （直译：收割机，采集器）。这些组件一起工作以跟踪文件，并将事件数据发送到你指定的输出。
    2.1.  harvester是什么
    一个harvester负责读取一个单个文件的内容。
    harvester逐行读取每个文件（一行一行地读取每个文件），并把这些内容发送到输出。
    每个文件启动一个harvester。
    harvester负责打开和关闭这个文件，这就意味着在harvester运行时文件描述符保持打开状态。
    在harvester正在读取文件内容的时候，文件被删除或者重命名了，那么Filebeat会续读这个文件。这就有一个问题了，就是只要负责这个文件的harvester没用关闭，那么磁盘空间就不会释放。默认情况下，Filebeat保存文件打开直到close_inactive到达。
    2.2.  input是什么
    一个input负责管理harvesters，并找到所有要读取的源。
    如果input类型是log，则input查找驱动器上与已定义的glob路径匹配的所有文件，并为每个文件启动一个harvester。
    每个input都在自己的Go例程中运行。
    下面的例子配置Filebeat从所有匹配指定的glob模式的文件中读取行：
    filebeat.inputs:
    - type: log
      paths:
        - /var/log/*.log
        - /var/path2/*.log
     2.3.  Filebeat如何保持文件状态
    Filebeat保存每个文件的状态，并经常刷新状态到磁盘上的注册文件（registry）。状态用于记住harvester读取的最后一个偏移量，并确保所有日志行被发送（到输出）。如果输出，比如Elasticsearch 或者 Logstash等，无法访问，那么Filebeat会跟踪已经发送的最后一行，并只要输出再次变得可用时继续读取文件。当Filebeat运行时，会将每个文件的状态新保存在内存中。当Filebeat重新启动时，将使用注册文件中的数据重新构建状态，Filebeat将在最后一个已知位置继续每个harvester。
    对于每个输入，Filebeat保存它找到的每个文件的状态。因为文件可以重命名或移动，所以文件名和路径不足以标识文件。对于每个文件，Filebeat存储惟一标识符，以检测文件是否以前读取过。
    如果你的情况涉及每天创建大量的新文件，你可能会发现注册表文件变得太大了。
    （画外音：Filebeat保存每个文件的状态，并将状态保存到registry_file中的磁盘。当重新启动Filebeat时，文件状态用于在以前的位置继续读取文件。如果每天生成大量新文件，注册表文件可能会变得太大。为了减小注册表文件的大小，有两个配置选项可用：clean_remove和clean_inactive。对于你不再访问且被忽略的旧文件，建议您使用clean_inactive。如果想从磁盘上删除旧文件，那么使用clean_remove选项。）
     2.4.  Filebeat如何确保至少投递一次（at-least-once）？
    Filebeat保证事件将被投递到配置的输出中至少一次，并且不会丢失数据。Filebeat能够实现这种行为，因为它将每个事件的投递状态存储在注册表文件中。
    在定义的输出被阻塞且没有确认所有事件的情况下，Filebeat将继续尝试发送事件，直到输出确认收到事件为止。
    如果Filebeat在发送事件的过程中关闭了，则在关闭之前它不会等待输出确认所有事件。当Filebeat重新启动时，发送到输出（但在Filebeat关闭前未确认）的任何事件将再次发送。这确保每个事件至少被发送一次，但是你最终可能会将重复的事件发送到输出。你可以通过设置shutdown_timeout选项，将Filebeat配置为在关闭之前等待特定的时间。
 
##### 3.模块

    
    Filebeat模块简化了公共日志格式的收集、解析和可视化。
    一个典型的模块（例如，对于Nginx日志）是由一个或多个fileset组成的（以Nginx为例，access 和 error）。
    一个fileset包含以下内容：
    Filebeat 输入配置，其中包含要默认的查找或者日志文件路径。这些默认路径取决于操作系统。Filebeat配置还负责在需要的时候拼接多行事件。
    Elasticsearch Ingest Node 管道定义，用于解析日志行。
    字段定义，用于为每个字段在Elasticsearch中配置正确类型。它们还包含每个字段的简短描述。
    简单的Kibana dashboards，用于可视化日志文件。
    Filebeat会根据你的环境自动调整这些配置，并将它们加载到相应的 Elastic stack 组件中。
    3.1.  常用日志格式的模块
    Filebeat提供了一组预先构建的模块，你可以使用这些模块快速实现并部署一个日志监控解决方案，包括样例指示板和数据可视化，完成这些大约只需要5分钟。
    这些模块支持常见的日志格式，如Nginx、Apache2和MySQL，可以通过一个简单的命令来运行。
    3.1.1.  先决条件
    在运行Filebeat模块之前：
    安装并配置Elastic stack
    完成Filebeat的安装
    安装 Ingest Node GeoIP 和 User Agent 插件。这些插件用来捕获地理位置和浏览器信息，以供可视化组件所用
    检查Elasticsearch和Kibana是否正在运行，以及Elasticsearch是否准备好从Filebeat那里接收数据
    你可以在Elasticsearch主目录下运行下列命令来安装这些插件：
    sudo bin/elasticsearch-plugin install ingest-geoip
    sudo bin/elasticsearch-plugin install ingest-user-agent
    然后，重启Elasticsearch
    3.1.2.  运行Filebeat模块：
    第1步：在filebeat.yml配置文件中设置Elasticsearch安装的位置。默认情况下，Filebeat假设Elasticsearch运行在9200端口上。
    如果你是运行在Elastic Cloud上，指定你的Cloud ID。例如：
    cloud.id: "staging:dXMtZWFzdC0xLmlOTYyNTc0Mw=="
    如果你是运行在自己的硬件设备上，设置主机和端口。例如：
    output.elasticsearch:
        hosts: ["myEShost:9200"]
    第2步：如果Elasticsearch配置了安全策略，你需要在filebeat.yml中指定访问的凭证。
    如果你运行在Elastic Cloud上，请指定你的授权凭证。例如：
    cloud.auth: "elastic:YOUR_PASSWORD"
    如果你运行在自己的硬件设备上，请指定你的Elasticsearch和Kibana凭证。例如：
    
    output.elasticsearch:
      hosts: ["myEShost:9200"]
      username: "filebeat_internal"
      password: "YOUR_PASSWORD" 
    setup.kibana:
      host: "mykibanahost:5601"
      username: "my_kibana_user"  
      password: "YOUR_PASSWORD"
    
    第3步：启用你想运行的模块。例如：
    ./filebeat modules enable system nginx mysql
    第4步：设置初始环境：
    ./filebeat setup -e
    第5步：运行Filebeat
    ./filebeat -e
    第6步：在Kibana中查看你的数据
    3.1.3.  设置路径变量
    每个模块和fileset都有变量，你可以设置它们，以改变模块的默认行为，包括模块查找日志文件的路径。
    可以在配置文件或者命令行下设置这些路径。例如：
    - module: nginx
      access:
        var.paths: ["/var/log/nginx/access.log*"] 
    或者
    ./filebeat -e -M "nginx.access.var.paths=[/usr/local/var/log/nginx/access.log*]"
     3.2.  Nginx模块
    nginx模块解析Nginx创建的access和error日志
    当你运行模块的时候，它在底层执行一些任务：
    设置默认的日志文件路径
    确保将每个多行日志事件作为单个事件发送
    使用ingest节点解析和处理日志行，将数据塑造成适合在Kibana中可视化的结构
    部署显示日志数据的dashboards
    这个模块需要 ingest-user-agent 和 ingest-geoip 两个Elasticsearch插件
    3.2.1.  设置并运行模块
    第1步：启用模块
    ./filebeat modules enable nginx
    为了查看启用或者禁用的模块列表，运行：
    ./filebeat modules list
    第2步：设置初始环境
    ./filebeat setup -e
    第3步：运行
    ./filebeat -e
    3.2.2.  配置模块
    通过在modules.d/nginx.yml中指定变量设置，或者在命令行中重写设置来改变模块的行为。例如：
    
    - module: nginx
      access:
        enabled: true
        var.paths: ["/path/to/log/nginx/access.log*"]
      error:
        enabled: true
        var.paths: ["/path/to/log/nginx/error.log*"]
    
    或者
    -M "nginx.access.var.paths=[/path/to/log/nginx/access.log*]" -M "nginx.error.var.paths=[/path/to/log/nginx/error.log*]"
    3.3.  Kafka模块
    3.3.1.  配置
    
    - module: kafka
      log:
        enabled: true
        var.paths:
          - "/path/to/logs/controller.log*"
          - "/path/to/logs/server.log*"
          - "/path/to/logs/state-change.log*"
          - "/path/to/logs/kafka-*.log*"
    
    或者
    -M "kafka.log.var.paths=[/path/to/logs/controller.log*, /path/to/logs/server.log*, /path/to/logs/state-change.log*, /path/to/logs/kafka-*.log*]"
    其它模块配置大同小异，不再赘述，更多请参考  https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-modules.html
 
##### 4.  配置Filebeat
    为了配置Filebeat，你可以编辑配置文件 filebeat.yml。还有一个完整的配置文件示例 filebeat.reference.yml
    4.1.  指定运行哪个模块
    Filebeat提供了几种启用模块的不同方式：
    用modules.d目录下的配置启用模块
    运行Filebeat的时候启用模块
    用filebeat.yml启用模块配置
    4.1.1.  用modules.d目录启用模块配置
    ./filebeat modules enable apache2 mysql
    4.1.2.  运行Filebeat是启用模块
    ./filebeat --modules nginx,mysql,system
    4.1.3.  在filebeat.yml中启用模块
    如果可能的话，你应该用modules.d目录下的配置文件
    filebeat.modules:
    - module: nginx
    - module: mysql
    - module: system
     4.2.  配置inputs
    为了手动配置Filebeat（代替用模块），你可以在filebeat.yml中的filebeat.inputs区域下指定一个inputs列表。
    列表时一个YMAL数组，并且你可以指定多个inputs，相同input类型也可以指定多个。例如：
    
    filebeat.inputs:
    - type: log
      paths:
        - /var/log/system.log
        - /var/log/wifi.log
    - type: log
      paths:
        - "/var/log/apache2/*"
      fields:
        apache: true
      fields_under_root: true
    
    4.2.1.  Log input
    从日志文件读取行
    为了配置这种input，需要指定一个paths列表，列表中的每一项必须能够定位并抓取到日志行。例如：
    filebeat.inputs:
    - type: log
      paths:
        - /var/log/messages
        - /var/log/*.log
    你还可以应用设置其它额外的配置项（比如，fields, include_lines, exclude_lines, multiline等等）来从这些文件中读取行。你设置的这些配置对所有这种类型的input在获取日志行的时候都生效。
    为了对不同的文件应用不同的配置，你需要定义多个input区域：
    
    filebeat.inputs:
    - type: log 　　# 从system.log和wifi.log中读取日志行
      paths:
        - /var/log/system.log
        - /var/log/wifi.log
    - type: log 　　# 从apache2目录下的每一个文件中读取日志行，并且在输出的时候会加上额外的字段apache
      paths:
        - "/var/log/apache2/*"
      fields:
        apache: true
      fields_under_root: true
    
    配置项
    paths
    例如：/var/log/*/*.log 将会抓取/var/log子目录目录下所有.log文件。它不会从/var/log本身目录下的日志文件。如果你应用recursive_glob设置的话，它将递归地抓取所有子目录下的所有.log文件。
    recursive_glob.enabled
    允许将**扩展为递归glob模式。启用这个特性后，每个路径中最右边的**被扩展为固定数量的glob模式。例如：/foo/**扩展到/foo， /foo/*， /foo/**，等等。如果启用，它将单个**扩展为8级深度*模式。这个特性默认是启用的，设置recursive_glob.enabled为false可以禁用它。
    encoding
    读取的文件的编码
    下面是一些W3C推荐的简单的编码：
    plain, latin1, utf-8, utf-16be-bom, utf-16be, utf-16le, big5, gb18030, gbk, hz-gb-2312
    euc-kr, euc-jp, iso-2022-jp, shift-jis, 等等
    plain编码是特殊的，因为它不校验或者转换任何输入。
    exclude_lines
    一组正则表达式，用于匹配你想要排除的行。Filebeat会删除（PS：我觉得用“丢弃”更合适）这组正则表达式匹配的行。默认情况下，没有行被删除。空行被忽略。
    如果指定了multiline，那么在用exclude_lines过滤之前会将每个多行消息合并成一个单行。（PS：也就是说，多行合并成单行后再支持排除行的过滤）
    下面的例子配置Filebeat删除以DBG开头的行：
    filebeat.inputs:
    - type: log
      ...
      exclude_lines: ['^DBG']
    include_lines
    一组正则表达式，用于匹配你想要包含的行。Filebeat只会导出那些匹配这组正则表达式的行。默认情况下，所有的行都会被导出。空行被忽略。
    如果指定了multipline设置，每个多行消息先被合并成单行以后再执行include_lines过滤。
    下面是一个例子，配置Filebeat导出以ERR或者WARN开头的行：
    filebeat.inputs:
    - type: log
      ...
      include_lines: ['^ERR', '^WARN']
    （画外音：如果 include_lines 和 exclude_lines 都被定义了，那么Filebeat先执行 include_lines 后执行 exclude_lines，而与这两个选项被定义的顺序没有关系。include_lines 总是在 exclude_lines选项前面执行，即使在配置文件中 exclude_lines 出现在 include_lines的前面。）
    下面的例子导出那些除了以DGB开头的所有包含sometext的行：
    filebeat.inputs:
    - type: log
      ...
      include_lines: ['sometext']
      exclude_lines: ['^DBG']
    harvester_buffer_size
    当抓取一个文件时每个harvester使用的buffer的字节数。默认是16384。
    max_bytes
    单个日志消息允许的最大字节数。超过max_bytes的字节将被丢弃且不会被发送。对于多行日志消息来说这个设置是很有用的，因为它们往往很大。默认是10MB（10485760）。
    json
    这些选项使得Filebeat将日志作为JSON消息来解析。例如：
    json.keys_under_root: true
    json.add_error_key: true
    json.message_key: log
    为了启用JSON解析模式，你必须至少指定下列设置项中的一个：
    　　keys_under_root
    　　默认情况下，解码后的JSON被放置在一个以"json"为key的输出文档中。如果你启用这个设置，那么这个key在文档中被复制为顶级。默认是false。
    　　overwrite_keys
    　　如果keys_under_root被启用，那么在key冲突的情况下，解码后的JSON对象将覆盖Filebeat正常的字段
    　　add_error_key
    　　如果启用，则当JSON反编排出现错误的时候Filebeat添加 "error.message" 和 "error.type: json"两个key，或者当没有使用message_key的时候。
    　　message_key
    　　一个可选的配置，用于在应用行过滤和多行设置的时候指定一个JSON key。指定的这个key必须在JSON对象中是顶级的，而且其关联的值必须是一个字符串，否则没有过滤或者多行聚集发送。
    　　ignore_decoding_error
    　　一个可选的配置，用于指定是否JSON解码错误应该被记录到日志中。如果设为true，错误将被记录。默认是false。
    multiline
    用于控制Filebeat如何扩多行处理日志消息
    exclude_files
    一组正则表达式，用于匹配你想要忽略的文件。默认没有文件被排除。
    下面是一个例子，忽略.gz的文件
    filebeat.inputs:
    - type: log
      ...
      exclude_files: ['\.gz$']
    ignore_older
    如果启用，那么Filebeat会忽略在指定的时间跨度之前被修改的文件。如果你想要保留日志文件一个较长的时间，那么配置ignore_older是很有用的。例如，如果你想要开始Filebeat，但是你只想发送最近一周最新的文件，这个情况下你可以配置这个选项。
    你可以用时间字符串，比如2h（2小时），5m（5分钟）。默认是0，意思是禁用这个设置。
    你必须设置ignore_older比close_inactive更大。
    close_*
    close_*配置项用于在一个确定的条件或者时间点之后关闭harvester。关闭harvester意味着关闭文件处理器。如果在harvester关闭以后文件被更新，那么在scan_frequency结束后改文件将再次被拾起。然而，当harvester关闭的时候如果文件被删除或者被移动，那么Filebeat将不会被再次拾起，并且这个harvester还没有读取的数据将会丢失。
    close_inactive
    当启用此选项时，如果文件在指定的持续时间内未被获取，则Filebeat将关闭文件句柄。当harvester读取最后一行日志时，指定周期的计数器就开始工作了。它不基于文件的修改时间。如果关闭的文件再次更改，则会启动一个新的harvester，并且在scan_frequency结束后，将获得最新的更改。
    推荐给close_inactive设置一个比你的日志文件更新的频率更大一点儿的值。例如，如果你的日志文件每隔几秒就会更新，你可以设置close_inactive为1m。如果日志文件的更新速率不固定，那么可以用多个配置。
    将close_inactive设置为更低的值意味着文件句柄可以更早关闭。然而，这样做的副作用是，如果harvester关闭了，新的日志行不会实时发送。
    关闭文件的时间戳不依赖于文件的修改时间。代替的，Filebeat用一个内部时间戳来反映最后一次读取文件的时间。例如，如果close_inactive被设置为5分钟，那么在harvester读取文件的最后一行以后，这个5分钟的倒计时就开始了。
    你可以用时间字符串，比如2h（2小时），5m（5分钟）。默认是5m。
    close_renamed
     当启用此选项时，Filebeat会在重命名文件时关闭文件处理器。默认情况下，harvester保持打开状态并继续读取文件，因为文件处理器不依赖于文件名。如果启用了close_rename选项，并且重命名或者移动的文件不再匹配文件模式的话，那么文件将不会再次被选中。Filebeat将无法完成文件的读取。
    close_removed
    当启用此选项时，Filebeat会在删除文件时关闭harvester。通常，一个文件只有在它在由close_inactive指定的期间内不活跃的情况下才会被删除。但是，如果一个文件被提前删除，并且你不启用close_removed，则Filebeat将保持文件打开，以确保harvester已经完成。如果由于文件过早地从磁盘中删除而导致文件不能完全读取，请禁用此选项。
    close_timeout
    当启用此选项是，Filebeat会给每个harvester一个预定义的生命时间。无论读到文件的什么位置，只要close_timeout周期到了以后就会停止读取。当你想要在文件上只花费预定义的时间时，这个选项对旧的日志文件很有用。尽管在close_timeout时间以后文件就关闭了，但如果文件仍然在更新，则Filebeat将根据已定义的scan_frequency再次启动一个新的harvester。这个harvester的close_timeout将再次启动，为超时倒计时。
    scan_frequency
    Filebeat多久检查一次指定路径下的新文件（PS：检查的频率）。例如，如果你指定的路径是 /var/log/* ，那么会以指定的scan_frequency频率去扫描目录下的文件（PS：周期性扫描）。指定1秒钟扫描一次目录，这还不是很频繁。不建议设置为小于1秒。
    如果你需要近实时的发送日志行的话，不要设置scan_frequency为一个很低的值，而应该调整close_inactive以至于文件处理器保持打开状态，并不断地轮询你的文件。
    默认是10秒。
    scan.sort
    如果你指定了一个非空的值，那么你可以决定用scan.order的升序或者降序。可能的值是 modtime 和 filename。为了按文件修改时间排序，用modtime，否则用 filename。默认此选项是禁用的。
    scan.order
    可能的值是 asc 或者 desc。默认是asc。
    更多配置请查看 https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html
     
    （画外音：
    这里再重点说一下 ignore_older , close_inactive , scan_frequency 这三个配置项 
    ignore_older： 它是设置一个时间范围（跨度），不在这个跨度范围之内的文件更新都不管
    scan_frequency： 它设置的是扫描文件的频率，看看文件是否更新
    close_inactive：它设置的是文件如果多久没更新的话就关闭文件句柄，它是有一个倒计时，如果在倒计时期间，文件没有任何变化，则当倒计时结束的时候关闭文件句柄。不建议设置为小于1秒。
    如果文件句柄关了以后，文件又被更新，那么在下一个扫描周期结束的时候变化发现这个改变，于是会再次打开这个文件读取日志行，前面我们也提到过，每个文件上一次读到什么位置（偏移量）都记录在registry文件中。
    ）
     
    4.3.  管理多行消息
    Filebeat获取的文件可能包含跨多行文本的消息。例如，多行消息在包含Java堆栈跟踪的文件中很常见。为了正确处理这些多行事件，你需要在filebeat.yml中配置multiline以指定哪一行是单个事件的一部分。
    
    4.3.1.  配置项
    你可以在filebeat.yml的filebeat.inputs区域指定怎样处理跨多行的消息。例如：
    multiline.pattern: '^\['
    multiline.negate: true
    multiline.match: after
    上面的例子中，Filebeat将所有不以 [ 开始的行与之前的行进行合并。
    multiline.pattern
    指定用于匹配多行的正则表达式
    multiline.negate
    定义模式是否被否定。默认false。
    multiline.match
    指定Filebeat如何把多行合并成一个事件。可选的值是 after 或者 before。
    这种行为还收到negate的影响：
    
    multiline.flush_pattern
    指定一个正则表达式，多行将从内存刷新到磁盘。
    multiline.max_lines
    可以合并成一个事件的最大行数。如果一个多行消息包含的行数超过max_lines，则超过的行被丢弃。默认是500。
    4.4.  多行配置示例
    4.4.1.  Java堆栈跟踪
    Java堆栈跟踪由多行组成，在初始行之后的每一行都以空格开头，例如下面这样：
    Exception in thread "main" java.lang.NullPointerException
            at com.example.myproject.Book.getTitle(Book.java:16)
            at com.example.myproject.Author.getBookTitles(Author.java:25)
            at com.example.myproject.Bootstrap.main(Bootstrap.java:14)
     为了把这些行合并成单个事件，用写了多行配置：
    multiline.pattern: '^[[:space:]]'
    multiline.negate: false
    multiline.match: after
    这个配置将任意以空格开始的行合并到前一行
    下面是一个稍微更复杂的例子
    
    Exception in thread "main" java.lang.IllegalStateException: A book has a null property
           at com.example.myproject.Author.getBookIds(Author.java:38)
           at com.example.myproject.Bootstrap.main(Bootstrap.java:14)
    Caused by: java.lang.NullPointerException
           at com.example.myproject.Book.getId(Book.java:22)
           at com.example.myproject.Author.getBookIds(Author.java:35)
           ... 1 more
    
    为了合并这个，用下面的配置：
    multiline.pattern: '^[[:space:]]+(at|\.{3})\b|^Caused by:'
    multiline.negate: false
    multiline.match: after
    在这个例子中，模式匹配下列行：
    以空格开头，后面跟 at 或者 ... 的行
    以 Caused by: 开头的行
     
    一些编程语言使用行尾的反斜杠(\)字符表示该行继续，如本例所示:
    printf ("%10.10ld  \t %10.10ld \t %s\
      %f", w, x, y, z );
    为了把这样的多行合并成单个事件，用下列配置：
    multiline.pattern: '\\$'
    multiline.negate: false
    multiline.match: before
    这段配置合并任意以 \ 结尾的行
    4.4.2.  时间戳
    下面是以时间戳开始的日志
    [2015-08-24 11:49:14,389][INFO ][env                      ] [Letha] using [1] data paths, mounts [[/
    (/dev/disk1)]], net usable_space [34.5gb], net total_space [118.9gb], types [hfs]
    为了合并这种行，用下列配置：
    multiline.pattern: '^\[[0-9]{4}-[0-9]{2}-[0-9]{2}'
    multiline.negate: true
    multiline.match: after
    4.4.3.  应用事件
    有时你的应用日志包含事件，自定义的开始和结束时间，例如：
    [2015-08-24 11:49:14,389] Start new event
    [2015-08-24 11:49:14,395] Content of processing something
    [2015-08-24 11:49:14,399] End event
    为了合并这种行，用下面的多行配置：
    multiline.pattern: 'Start new event'
    multiline.negate: true
    multiline.match: after
    multiline.flush_pattern: 'End event'
    4.5.  加载外部配置文件
    Filebeat允许将配置分隔为多个较小的配置文件，然后加载外部配置文件。
    4.5.1.  输入配置
    filebeat.config.inputs:
      enabled: true
      path: configs/*.yml
    每一个在path下的文件都必须包含一个或多个input定义，例如：
    
    - type: log
      paths:
        - /var/log/mysql.log
      scan_frequency: 10s
    
    - type: log
      paths:
        - /var/log/apache.log
      scan_frequency: 5s
    
    4.5.2.  模块配置
    filebeat.config.modules:
      enabled: true
      path: ${path.config}/modules.d/*.yml
    每个被发现的配置文件必须包含一个或多个模块定义，例如：
    
    - module: apache2
      access:
        enabled: true
        var.paths: [/var/log/apache2/access.log*]
      error:
        enabled: true
        var.paths: [/var/log/apache2/error.log*]
    
    4.6.  配置output
    4.6.1.  配置Elasticsearch output  
    当你指定Elasticsearch作为output时，Filebeat通过Elasticsearch提供的HTTP API向其发送数据。例如：
    output.elasticsearch:
      hosts: ["https://localhost:9200"]
      index: "filebeat-%{[beat.version]}-%{+yyyy.MM.dd}"
      ssl.certificate_authorities: ["/etc/pki/root/ca.pem"]
      ssl.certificate: "/etc/pki/client/cert.pem"
      ssl.key: "/etc/pki/client/cert.key"
    为了启用SSL，只需要在hosts下的所有URL添加https即可
    output.elasticsearch:
      hosts: ["https://localhost:9200"]
      username: "filebeat_internal"
      password: "YOUR_PASSWORD"
    如果Elasticsearch节点是用IP:PORT的形式定义的，那么添加protocol:https。
    output.elasticsearch:
      hosts: ["localhost"]
      protocol: "https"
      username: "{beatname_lc}_internal"
      password: "{pwd}"
    配置项
    enabled
    启用或禁用该输出。默认true。
    hosts
    Elasticsearch节点列表。事件以循环顺序发送到这些节点。如果一个节点变得不可访问，那么自动发送到下一个节点。每个节点可以是URL形式，也可以是IP:PORT形式。如果端口没有指定，用9200。
    output.elasticsearch:
      hosts: ["10.45.3.2:9220", "10.45.3.1:9230"]
      protocol: https
      path: /elasticsearch
    username
    用于认证的用户名
    password
    用户认证的密码
    protocol
    可选值是：http 或者 https。默认是http。
    path
    HTTP API调用前的HTTP路径前缀。这对于Elasticsearch监听HTTP反向代理的情况很有用。
    headers
    将自定义HTTP头添加到Elasticsearch输出的每个请求。
    index
    索引名字。（PS：意思是要发到哪个索引中去）。默认是"filebeat-%{[beat.version]}-%{+yyyy.MM.dd}"（例如，"filebeat-6.3.2-2017.04.26"）。如果你想改变这个设置，你需要配置 setup.template.name 和 setup.template.pattern 选项。如果你用内置的Kibana dashboards，你也需要设置setup.dashboards.index选项。
    indices
    索引选择器规则数组，支持条件、基于格式字符串的字段访问和名称映射。如果索引缺失或没有匹配规则，将使用index字段。例如：
    
    output.elasticsearch:
      hosts: ["http://localhost:9200"]
      index: "logs-%{[beat.version]}-%{+yyyy.MM.dd}"
      indices:
        - index: "critical-%{[beat.version]}-%{+yyyy.MM.dd}"
          when.contains:
            message: "CRITICAL"
        - index: "error-%{[beat.version]}-%{+yyyy.MM.dd}"
          when.contains:
            message: "ERR"
    
    timeout
    请求超时时间。默认90秒。
    4.6.2.  配置Logstash output
    output.logstash:
      hosts: ["127.0.0.1:5044"]
    上面是配置Filebeat输出到Logstash，那么Logstash本身也有配置，例如：
    
    input {
      beats {
        port => 5044
      }
    }
    
    output {
      elasticsearch {
        hosts => ["http://localhost:9200"]
        index => "%{[@metadata][beat]}-%{[@metadata][version]}-%{+YYYY.MM.dd}" 
      }
    }
    
    4.6.3.  配置Kafka output
    
    output.kafka:
      # initial brokers for reading cluster metadata
      hosts: ["kafka1:9092", "kafka2:9092", "kafka3:9092"]
    
      # message topic selection + partitioning
      topic: '%{[fields.log_topic]}'
      partition.round_robin:
        reachable_only: false
    
      required_acks: 1
      compression: gzip
      max_message_bytes: 1000000
    
    4.7.  负载均衡
    为了启用负载均衡，当你配置输出的时候你需要指定 loadbalance: true
    output.logstash:
      hosts: ["localhost:5044", "localhost:5045"]
      loadbalance: true
    4.8.  加载索引模板
    在filebeat.yml配置文件的setup.template区域指定索引模板，用来设置在Elasticsearch中的映射。如果模板加载是启用的（默认的），Filebeat在成功连接到Elasticsearch后自动加载索引模板。
    你可以调整下列设置或者覆盖一个已经存在的模板。
    setup.template.enabled
    设为false表示禁用模板加载
    setup.template.name
    模板的名字。默认是filebeat。Filebeat的版本总是跟在名字后面，所以最终的名字是 filebeat-%{[beat.version]}
    setup.template.pattern
    模板的模式。默认模式是filebeat-*。例如：
    setup.template.name: "filebeat"
    setup.template.pattern: "filebeat-*"
    setup.template.fields
    描述字段的YAML文件路径。默认是 fields.yml。
    setup.template.overwrite
    是否覆盖存在的模板。默认false。
    setup.template.settings._source
    setup.template.name: "filebeat"
    setup.template.fields: "fields.yml"
    setup.template.overwrite: false
    setup.template.settings:
      _source.enabled: false
    4.9.  监控Filebeat
    X-Pack监控使得可以很容易从Kibana监控Filebeat。
    为了配置Filebeat收集并发送监控信息，
    第1步：创建一个具有适当权限向Elasticsearch发送系统级监视数据的用户。
    第2步：在Filebeat配置文件中添加xpack.monitoring设置。如果你配置了Elasticsearch输出，指定下面最小的配置：
    xpack.monitoring.enabled: true
    如果你配置的是其它输出，比如Logstash，那么你必须指定一些额外的配置项。例如：
    xpack.monitoring:
      enabled: true
      elasticsearch:
        hosts: ["https://example.com:9200", "https://example2.com:9200"]
        username: beats_system
        password: beatspassword
    （注意：目前，你必须将监视数据发送到与所有其他事件相同的集群。如果你配置了Elasticsearch输出，请不要在监视配置中指定其他主机。）
    第3步：在Kibana中配置监控
    第4步：在Kibana中查看监控
 
##### 5.  FAQ
    --------------------------------------------------------------------------------
    5.1.  Too many open file handler？（太多打开的文件句柄）
    Filebeat保持文件处理器打开，以防它到达文件的末尾，以便它可以实时读取新的日志行。如果Filebeat正在收集大量文件，那么打开文件的数量可能成为一个问题。在大多数环境中，主动更新的文件数量很少。应该相应地设置close_inactive配置选项，以关闭不再活动的文件。
    5.2.  Filebeat没有从一个文件收集行
    为了解决这个问题：
    确保路径配置正确
    检查这个文件是不是比指定的ignore_older值更旧
    确保Filebeat能够发送时间到配置的输出。以debug模式运行Filebeat来检查是否可以成功发送事件：
    ./filebeat -c config.yml -e -d "*"
     
    https://www.elastic.co/guide/en/beats/filebeat/current/faq.html
    5.3.  Filebeat占用了太多CPU资源
    Filebeat可能配置扫描文件太过频繁。检查filebeat.yml中的scan_frequency设置。
     

参考来源:
https://www.cnblogs.com/cjsblog/p/9495024.html