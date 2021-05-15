## 使用 srsLTE 搭建 4G 基站

很多 IoT 设备都在使用 4G 网卡，但测 4G 链路安全时候，毕竟不像 WIFI 测试那么方便，CMW500 的价格还是很贵的，尤其是想 IoT 安全爱好者童鞋，
或者其他移动端需要测试 4G 链路的，其实可以使用低成本 SDR 方案，也就是这里要介绍的。
最低成本可以使用 LimeSDR Mini+PC 机，也可以使用 BladeRF x40 或者 x115 来实现。
如果条件允许，还是使用 USRP B200mini、B210 或者 N210 甚至 X310 比较舒服。
srsLTE 是软件无线电系统（SRS）的开源 SDR LTE 软件套件。
是由爱尔兰 SoftwareRadioSystems (SRS)公司开发实现的免费开源 LTE SDR 平台，在 AGPLv3 license 许可下发布，并且在实现中使用了 OpenLTE 的相关功能。
有关文档，指南和项目新闻，请参见 srsLTE 项目页面（www.srslte.com）

### srsLTE 包括以下组件：

srsUE - 完整的 SDR LTE UE 应用，支持从 PHY 到 IP 层特性
srsENB - 完整的 SDR LTE eNodeB 应用
srsEPC - 轻量级 LTE 核心网络实现，支持 MME, HSS 和 S/P-GW
高度模块化的共用库 PHY, MAC, RLC, PDCP, RRC, NAS, S1AP 和 GW 层
通用特性：

支持 LTE Release 10
测试频率: 1.4, 3, 5, 10, 15 and 20 MHz
传输模式 1（单天线），2（发射分集），3（CCD）和 4（闭环空间复用）
基于频率的 ZF 和 MMSE 均衡器
演进的多媒体广播和多播服务（eMBMS）
高度优化的 Turbo 解码器，采用 Intel SSE4.1 / AVX2（+100 Mbps）和标准 C（+25 Mbps）
支持 MAC, RLC, PDCP, RRC, NAS, S1AP 和 GW 层
具有每层日志级别和十六进制转储的详细日志系统
MAC 层 wireshark 数据包捕获
命令行跟踪指标
详细的输入配置文件
用于 EPA，EVA 和 ETU 3GPP 频道的信道模拟器
基于 ZeroMQ 的伪 RF 驱动器，用于 IPC/网络上的 I/Q
srsUE 特性：

FDD 和 TDD 配置
运营商聚合支持
UE 的小区搜索和同步过程
软 USIM 支持 Milenage 和 XOR 身份验证
使用 PCSC 框架的硬 USIM 支持
在网络连接时创建的虚拟网络接口 tun_srsue
QoS 支持
i7 四核 CPU 中 20 MHz MIMO TM3 / TM4 配置中的 150 Mbps DL。
i7 四核 CPU 中 20 MHz SISO 配置中的 75 Mbps DL。
i5 双核 CPU 中 10 MHz SISO 配置中的 36 Mbps DL。
srsUE 在如下网络设备中测试和验证通过：

Amarisoft LTE100 eNodeB 和 EPC
诺基亚 FlexiRadio 系列 FSMF 系统模块具有 1800MHz FHED 无线电模块和 TravelHawk EPC 模拟器
Huawei DBS3900
Octasic Flexicell LTE-FDD NIB
srsENB 特性：

FDD 配置
循环 MAC 调度程序，具有类似 FAPI 的 C ++ API
SR 支持
定期和非周期性 CQI 反馈支持
标准 S1AP 和 GTP-U 与核心网络接口
具有商用 UE 的 20MHz MIMO TM3 / TM4 中的 150Mbps DL
具有商用 UE 的 SISO 配置中的 75 Mbps DL
20 MHz 的 50 Mbps UL，商用 US
用户平面加密
srsENB 已通过以下手机进行测试和验证：

LG Nexus 5 and 4
Motorola Moto G4 plus and G5
Huawei P9/P9lite, P10/P10lite, P20/P20lite
Huawei dongles: E3276 and E398
srsEPC 特性：

单个二进制、轻量级 LTE EPC 实现，具有：
MME（移动性管理实体）具有到 eNB 的标准 S1AP 和 GTP-U 接口
标准 SGi 的 S/P-GW 暴露为虚拟网络接口（TUN 设备）
HSS（归属订户服务器），具有 CSV 格式的可配置用户数据库
支持分页
硬件支持：

支持如下硬件:

USRP B200
USRP B210
USRP B200mini
USRP B205mini
USRP N210
USRP X310
limeSDR
bladeRF
Github 网址：https://github.com/srsLTE/srsLTE

安装部署 srsLTE 建议用物理机，因为一些指令优化、时序要求非常精准，比如双向验证鉴权的时候，差一点都不行，
如果不使用 USRP N210 或者 X310 一类通过网络的 SDR 硬件稳定性，而使用 USB 接口的话虚拟机稳定性略差。
这里使用 Ubuntu 16.04 搭配 USRP B210 进行测试，使用 root 账户直接安装配置。

### 先安装低延时内核，之后重启加载新内核：

```
sudo apt-get install linux-lowlatency
sudo apt-get install linux-image-`uname -r | cut -d- -f1-2`-lowlatency
sudo apt-get install linux-headers-`uname -r | cut -d- -f1-2`-lowlatency
sudo reboot
```

```
需要注意的是，之后如果升级内核，需要手动再安装低延迟内核！

先安装UHD，使用pybombs安装或者源码直接安装都可以，具体如前文所述，这里不再重复。
如果是BladeRF、LimeSDR也一样，先装驱动。
```

在 Ubuntu 下也可以选择用 apt 安装：

```
add-apt-repository ppa:srslte/releases
apt-get update
apt-get install srsepc srcenb srsue stelte
```

但需要注意的是，uhd 驱动不能用最新的，否则加载 usrp 会失败。所以，还是建议使用源码编译安装。

这里再讲下用源码安装。

```
安装srsGUI：

apt-get install cmake g++ libpython-dev python-numpy swig git libsqlite3-dev libi2c-dev libusb-1.0-0-dev libwxgtk3.0-dev freeglut3-dev libfftw3-dev libmbedtls-dev libboost-program-options-dev libconfig++-dev libsctp-dev libboost-system-dev libboost-test-dev libboost-thread-dev libqwt-dev libqt4-dev
git clone https://github.com/srsLTE/srsGUI.git
cd srsGUI
mkdir build
cd build
cmake ../
make -j4
make install
ldconfig
```

安装 SoapySDR：

```
git clone https://github.com/pothosware/SoapySDR.git
cd SoapySDR
git checkout soapy-sdr-0.7.2
mkdir build
cd build
cmake ..
make -j4
make install
ldconfig
```

安装 srsLTE:

```
git clone https://github.com/srsLTE/srsLTE.git
cd srsLTE
mkdir build
cd build
cmake ../
make -j8
make test
make install
ldconfig
```

安装完成后生成配置文件，配置文件 user 生成到用户路径~/.config/srsLTE/，service 生成到/etc/srsLTE 路径：

srslte_install_configs.sh user
配置网络接口，参数是默认能上网的网口，这里是 eth0：

srsepc_if_masq.sh eth0
最后再添加 SIM 卡的配置，主要是 IMSI、KI、OPC，编辑~/.config/srsLTE/user_db.csv 文件，格式为：

(ue_name),(algo),(imsi),(K),(OP/OPc_type),(OP/OPc_value),(AMF),(SQN),(QCI),(IP_alloc)
ue3,mil,221010123456789,6874736969202073796D4B2079650A76,opc,504F20634F6320504F50206363500A4F,8000,000000001234,7,dynamic

再将这些参数写入 SIM 卡，这里建议使用 GPSIMWrite，简单便捷，相比 pysim 还是方便不少，缺点是只能在 Windows 下。写卡器可以使用 MCR3512、M100 等营业厅所用写卡器都可以，用 Bludrive IISIM 也可以。

白卡选择 LTE 卡就可以，很多手机或者终端厂商需要用白卡配合 CMW500 一类综合测试仪进行测试，所以检索测试白卡关键字能买到，价格不便宜。仅供学习使用，请勿用于非法用途！

具体可以设置完 LTE 的 IMSI15、KI 和 OPC 之后，点击 Same With LTE，之后点击 Write Card 写入白卡：

把写好的卡装入手机，再分别启动 srsepc 和 srsenb：

这样 srsLTE 就启动了，下行 2685MHz 上行 2565MHz。如果不使用 USRP，使用 BladerRF 或者 LimeSDR 也类似，在启动时候会自动加载。

注：如果 USRP、BladeRF 驱动都安装了，个别人的环境会出现从 USRP 换 BladeRF 无法加载的问题，进入 srsLTE 的 build 目录，检查 CMakeCache.txt 文件中 BLADERF_FOUND 是 TRUE。如果是 FALSE，就删掉 CMakeCache.txt 再重新“cmake..”，按照前面的步骤重新编译安装 srsLTE。

注：为了获得最佳性能，建议禁用 CPU 频率动态调整，比如以下脚本：

for f in /sys/devices/system/cpu/cpu[0-9]\*/cpufreq/scaling_governor ; do
echo performance > $f
done
在 srsenb 界面可以输入 t 回车来实时查看 snr、bler 等性能指标参数，如图所示：

如果电脑配置高的话，可以打开星座图，修改 enb.conf，将 enable 设置为 true，会增加 CPU 负载：

打开手机的网络选择界面，将手机注册进入网络：

如果手机无法搜索到信号，或者搜到了无法加入，先用其他 SDR 或者频谱仪检查下是否有信号发出，其次检查信号是否有频偏，因为每种手机对频偏的容忍度是不一样的。
笔者实测中，华为的兼容性是最好的。

如果有频偏，修改“ue.conf”中的“freq_offset”参数，如图所示。正常情况下如果使用官方原版 SDR 设备，而不是山寨 SDR 设备，很少出现这种情况。
另外，使用 GPSDO 或者其他稳定的时钟输入，也能避免这种问题。

如果还是无法搜索到信号，那就可能是手机的基带问题，比如在默认配置下，测试发现小米 MIX 就无法搜索到，可以修改下频段，也就是“EARFCN”参数，也可以直接在 srsenb 执行时加参数：

srsenb ~/.config/srslte/enb.conf --rf.dl_earfcn 1575

再测试就能搜索到了，如图所示。

EARFCN 和频段对照关系如下图所示：

当然，注册进网络后，还需要在电脑上配置 nat 转发：

echo 1 > /proc/sys/net/ipv4/ip_forward
iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
在手机上配置 APN，也就是 epc.conf 中默认设置的 srsapn：

手机上 APN 设置里新建，只需要设置名称和 APN 即可：

然后手机就可以愉快地上网了：

再进行一次测速：

受限于家里宽带的速度，总体还可以。

同时，在部署的电脑上可以使用 wireshark 进行抓包，可以对手机的 4G 流量进行分析，用于手机、平板、工控机等使用 4G 网络的设备进行分析研究。

除了 enb 用于手机，还可以使用 ue 让电脑与电脑连接，这里使用另一台电脑外接一个 USRP B210，安装部署流程一致，只是执行应用的时候不是 srsepc 和 srsenb，直接执行 srsue 即可：

相当于这个 USRP 就是一个 LTE 基带，连上基站后会创建一个虚拟网卡 tun_srsue，分配的 IP 是 172.16.0.2 和 srsenb 做创建的虚拟网卡在一个网段，可以互相 ping 一下：

和 srsenb 类似 srsue 也可以打开星座图，修改 ue.conf，将 enable 设置为 true，如图所示。同样会增加 CPU 负载，效果如图所示。

相比 OAI，srsLTE 对电脑的性能要求更高一些，但配置更加简便。

更多内容可以看官方文档：
https://docs.srslte.com/en/latest/index.html

最后附一个淘宝的参考购买链接：

https://item.taobao.com/item.htm?id=615846456252
这篇文章转载自简老师的博客：

https://www.white-alone.com/%E4%BD%BF%E7%94%A8srsLTE%E6%90%AD%E5%BB%BA4G%E5%9F%BA%E7%AB%99%E7%94%A8%E4%BA%8E%E6%97%A5%E5%B8%B8%E6%B5%8B%E8%AF%95/

欢迎大家关注简老师的博客

本文分享自微信公众号 - 物联网 IOT 安全（IOTsafety），作者：简云定

原文出处及转载信息见文内详细说明，如有侵权，请联系 yunjia_community@tencent.com 删除。

原始发表时间：2020-08-15

本文参与腾讯云自媒体分享计划，欢迎正在阅读的你也加入，一起分享。
