### zabbix

zabbix 是一个基于 WEB 界面的提供分布式系统监视以及网络监视功能的企业级的开源解决方案。
zabbix 能监视各种网络参数，保证服务器系统的安全运营；并提供灵活的通知机制以让系统管理员快速定位/解决存在的各种问题。
zabbix 由 2 部分构成，zabbix server 与可选组件 zabbix agent。
zabbix server 可以通过 SNMP，zabbix agent，ping，端口监视等方法提供对远程服务器/网络状态的监视，数据收集等功能，它可以运行在 Linux，Solaris，HP-UX，AIX，Free BSD，Open BSD，OS X 等平台上。

### 安装使用

zabbix agent 需要安装在被监视的目标服务器上，它主要完成对硬件信息或与操作系统有关的内存，CPU 等信息的收集。zabbix agent 可以运行在 Linux,Solaris,HP-UX,AIX,Free BSD,Open BSD, OS X, Tru64/OSF1, Windows NT4.0, Windows (2000/2003/XP/Vista)等系统之上。
zabbix server 可以单独监视远程服务器的服务状态；同时也可以与 zabbix agent 配合，可以轮询 zabbix agent 主动接收监视数据（agent 方式），同时还可被动接收 zabbix agent 发送的数据（trapping 方式）。
另外 zabbix server 还支持 SNMP (v1,v2)，可以与 SNMP 软件(例如：net-snmp)等配合使用。

### 搭建 Zabbix 监控环境

要想搭建一个 Zabbix 的工作环境，需要从服务器入手。与服务器通信，管理员需要使用一个 Zabbix 前端界面，与 Zabbix 服务器和数据库进行通信。三个关键（界面、服务器和数据库）可以安装在同一台服务器上，但是如果你拥有一个更大更复杂的环境，将它们安装在不同的主机上也是一个选项。Zabbix 服务器能够直接监控到同一网络中的设备，如果其他网络的设备也需要被监控，那还需要一台 Zabbix 代理服务器。

### zabbix 的主要特点：

- 安装与配置简单，学习成本低
- 支持多语言（包括中文）
- 免费开源
- 自动发现服务器与网络设备
- 分布式监视以及 WEB 集中管理功能
- 可以无 agent 监视
- 用户安全认证和柔软的授权方式
- 通过 WEB 界面设置或查看监视结果
- email 等通知功能
  等等

### Zabbix 主要功能：

- CPU 负荷
- 内存使用 -磁盘使用
- 网络状况
- 端口监视
- 日志监视。
