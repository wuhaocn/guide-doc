## ubuntu 16.04 srsLte +LimeSDR

安装前请移除之前软件包

```
sudo apt-get purge -y --auto-remove lime*
sudo apt-get purge -y --auto-remove soapy*
sudo apt-get purge -y --auto-remove pothos*
```

### 1.添加 PPA 源

```
sudo add-apt-repository -y ppa:bladerf/bladerf
sudo add-apt-repository -y ppa:ettusresearch/uhd
sudo add-apt-repository -y ppa:myriadrf/drivers
sudo add-apt-repository -y ppa:myriadrf/gnuradio
sudo add-apt-repository -y ppa:pothosware/framework
sudo add-apt-repository -y ppa:pothosware/support
sudo add-apt-repository -y ppa:ettusresearch/uhd
sudo apt-get update
```

### 2.操作系统调整

- 参考 ubuntu 16.04 低延迟内核安装低延时内核
- CPU 调整为最大性能模式,并且不允许降低频率

```
sudo apt-get install cpufrequtils

sudo touch /etc/default/cpufrequtils

sudo sed -i "/GOVERNOR.*/d" /etc/default/cpufrequtils

test -s /etc/default/cpufrequtils && sudo sed -i '$a\GOVERNOR=\"performance\"' /etc/default/cpufrequtils || echo "GOVERNOR=\"performance\"" | sudo tee /etc/default/cpufrequtils

sudo update-rc.d ondemand disable

sudo reboot
```

### 3.安装依赖包

```
sudo apt-get install cmake g++ libpython-dev python-numpy swig git libsqlite3-dev libi2c-dev libusb-1.0-0-dev libwxgtk3.0-dev freeglut3-dev
```

### 4.编译安装 SoapySDR

```
cd ~

git clone https://github.com/pothosware/SoapySDR.git

cd SoapySDR

git pull origin master

mkdir build && cd build

cmake ..

make -j4

sudo make install

sudo ldconfig
```

### 5.编译安装 LimeSDR

```
cd ~

git clone https://github.com/myriadrf/LimeSuite.git

cd LimeSuite

# 再次更新一下，确保代码到最新
git pull

# 不可删除build目录，清理build目录后要还原被误删除的文件，
# 原因在于build目录下存在mcu程序，默认应用启动后从这个目录提取mcu程序刷新到设备

mkdir build ; cd build

# cmake -DCMAKE_BUILD_TYPE=Debug ..
cmake ..

make -j4

sudo make install

sudo ldconfig

cd ../udev-rules/

sudo ./install.sh
# Download board firmware
# sudo LimeUtil --update
```

### 6.编译安装 srsGUI

```
cd ~

# srsGUI提供了一个软件示波器的功能，我们可以比较直观的观察到波形信息

git clone https://github.com/srsLTE/srsGUI.git

cd srsGUI

# 再次更新一下，确保代码到最新
git pull

mkdir build

cd build

cmake ..

make

sudo make install

sudo ldconfig

cd ~
```

### 编译安装 srsLTE

```
sudo apt-get install git cmake libfftw3-dev libmbedtls-dev libboost-program-options-dev libboost-thread-dev libconfig++-dev libsctp-dev
```

```
cd ~
git clone https://github.com/srsLTE/srsLTE.git

cd srsLTE

# 再次更新一下，确保代码到最新
git pull

# 目前（2018.05.04）以及之前的代码中srsenb/src/upper/rrc.cc文件中
# uint32_t rrc::generate_sibs() 函数中存在一个数组越界问题，
# 已经提交patch ,具体修改
# 参看https://github.com/srsLTE/srsLTE/pull/173

# 调用LimeSDR设备的代码在 lib/src/phy/rf/rf_soapy_imp.c 文件中
# 这个文件中默认的接收天线是"LNAH"，默认的发送天线是"BAND1",需要根据实际情况
# 也就是自己天线接入的接口是哪个，就修改成哪个。
# 调整这里的参数，目前是直接修改代码。比如我这边的发送天线就修改为"BAND2"

mkdir build

cd build

# cmake -DCMAKE_BUILD_TYPE=Debug ..
cmake ../

make

make test
```

### 测试

```
cd ~/srsLTE

cd build/lib/examples

#以20M带宽的形式进行基站的扫描，LTE基站带宽20M，目前版本测试不支持，无法扫描到基站
# ./cell_search -b 20

#目前测试仅支持此命令的运行
./pdsch_enodeb

cd ~/srsLTE

cd build/lib/examples

#以20M带宽的形式进行基站的扫描，LTE基站带宽20M，目前版本测试不支持，无法扫描到基站
# ./cell_search -b 20

#目前测试仅支持此命令的运行
./pdsch_enodeb

搭建LTE测试环境

cd ~/srsLTE

mkdir lteCell

cd lteCell


# 生成配置文件
mkdir lteENB

cp ../srsenb/enb.conf.example lteENB/enb.conf

cp ../srsenb/rr.conf.example lteENB/rr.conf

cp ../srsenb/sib.conf.example lteENB/sib.conf

cp ../srsenb/drb.conf.example lteENB/drb.conf

mkdir lteEPC

cp ../srsepc/epc.conf.example lteEPC/epc.conf

cp ../srsepc/user_db.csv.example lteEPC/user_db.csv


# 生成运行脚本
echo "echo -ne \"\033]0;ENB\007\"" >> run_enb.sh

echo "cd lteENB" >> run_enb.sh


# 需要gdb的话使用如下
#echo "#gdb -args ../../build/srsenb/src/srsenb enb.conf" >> run_enb.sh

echo "../../build/srsenb/src/srsenb enb.conf" >> run_enb.sh

echo "echo -ne \"\033]0;EPC\007\"" >> run_epc.sh

echo "cd lteEPC" >> run_epc.sh

# 需要配置如下转发规则，否则不能正常工作，配置信息参考openair-cn的SPGW中的代码
# sudo sysctl -w net.ipv4.ip_forward=1
# sudo sync
# sudo iptables -t mangle -F FORWARD
# sudo iptables -t nat -F POSTROUTING
# export LANG=C
# 如果没有修改过配置文件，则默认使用如下配置即可，
# 如果修改过sgi_if_addr的地址（默认sgi_if_addr=172.16.0.1），需要更改这个字段
# export FORDING_IPs=172.16.0.0/12
# 有线网卡一般以"en"开头，比如"enp3s0",此处需要根据自身机器上的网卡进行设置，修改
# export NIC_NAME=`ls /sys/class/net | grep en`
# export NIC_IP=`ifconfig $NIC_NAME | grep 'inet addr:' | awk '{print $2}' | cut -c 6-`
# sudo iptables -t nat -I POSTROUTING -s $FORDING_IPs -o  $NIC_NAME ! --protocol sctp -j SNAT --to-source $NIC_IP

echo "sysctl -w net.ipv4.ip_forward=1" >> run_epc.sh

echo "sync" >> run_epc.sh

echo "iptables -t mangle -F FORWARD" >> run_epc.sh

echo "iptables -t nat -F POSTROUTING" >> run_epc.sh

echo "export LANG=C" >> run_epc.sh

echo 'export FORDING_IPs=172.16.0.0/12' >> run_epc.sh

echo 'export NIC_NAME=`ls /sys/class/net | grep en`' >> run_epc.sh

echo "export NIC_IP=\`ifconfig \$NIC_NAME | grep 'inet addr:' | awk '{print \$2}' | cut -c 6-\`" >> run_epc.sh

echo 'iptables -t nat -I POSTROUTING -s $FORDING_IPs -o  $NIC_NAME ! --protocol sctp -j SNAT --to-source $NIC_IP' >> run_epc.sh

echo "../../build/srsepc/src/srsepc epc.conf" >> run_epc.sh

echo "gnome-terminal -e \"bash run_epc.sh\"" >> run.sh

echo "sleep 2" >> run.sh

echo "gnome-terminal -e \"bash run_enb.sh\"" >> run.sh




# 运行测试
sudo bash run.sh

#日志查看 /tmp/enb.log, /tmp/epc.log

如果设备（比如手机）的设置是按照ubuntu 16.04系统LimeSDR V1.4使用OpenAirInterface搭建LTE实验环境里面的设置的，设备参数如下图：
注意上图与OpenAirInterface中设置的不同之处，差别就是一个选中OPC，一个选中OP

则需要对配置文件进行如下调整：

cd ~/srsLTE/lteCell

# 修改MNC，MCC
sed -i -r "s/^mnc[ \t]*=[ \t0-9]*/mnc = 92/g" lteENB/enb.conf
sed -i -r "s/^mcc[ \t]*=[ \t0-9]*/mcc = 208/g" lteENB/enb.conf
sed -i -r "s/^tac[ \t]*=[ \t]*0x[0-9]*/tac = 0x0001/g" lteENB/enb.conf

sed -i -r "s/^mnc[ \t]*=[ \t0-9]*/mnc = 92/g" lteEPC/epc.conf
sed -i -r "s/^mcc[ \t]*=[ \t0-9]*/mcc = 208/g" lteEPC/epc.conf
sed -i -r "s/^tac[ \t]*=[ \t]*0x[0-9]*/tac = 0x0001/g" lteEPC/epc.conf

# 修改认证加密算法
sed -i -r "s/^auth_algo[ \t]*=[ \ta-Z]*/auth_algo = milenage/g" lteEPC/epc.conf


# 需要软件示波器界面的话，修改 lteEPC/epc.conf 里面的 [gui]部分的enable = true
# 但是打开示波器会导致CPU开销加大，降低实时性，稳定性变低

# 认证数据库中增加Name,IMSI,Key,OP,AMF,SQN 这里主要的就是IMSI,Key,OP这几个参数
# OPc=AES128(Ki,OP) XOR OP 因此需要反算 OP
# openair-cn需要OPc,而 srsLTE 的epc需要 OP 这是两者的主要区别
# OPc = 504f20634f6320504f50206363500a4f
# Ki = 6874736969202073796d4b2079650a73
# OP = 11111111111111111111111111111111
# 最后的 SQN 目前测试发现如果一直不能正常注册，并且提示
# "Sequence number synch failure" 则会观察到，当EPC退出时候，应用一定回写
# 成000000001b02 ，一旦出现这个数字，我们就没办法注册设备了。应该是个BUG
# http://www.mobibrw.com/?p=12688 中我们持续跟进这个问题,目前猜测是要比设备最后
# 一次通信记录的数据号大就可以了，如果要复位可以让设备关机，应该就从0开始了。

sed -i '$a\ue3,208920100001100,6874736969202073796d4b2079650a73,11111111111111111111111111111111,8000,000000001b03' lteEPC/user_db.csv

注意目前srsLTE的CPU开销远远高于OpenAirInterface，差不多一个内核满负载，以及稳定性是低于OpenAirInterface的,经常出现连接困难以及中途掉线，这部分需要后续的持续修改。目前测试发现，在"lteEPC/epc.conf"中关闭GUI的显示，可以显著减低CPU开销，并增加稳定性，但是还是比OpenAirInterface要多消耗CPU。另外在"lteEPC/epc.conf"中日志设置成"all_level = none"也可以降低CPU开销，并且增加稳定性。

另外，注意"/tmp/enb.log",这个日志文件默认情况下，写入的比较多，文件大小增长很快，注意磁盘占用情况。

目前测试发现，使用Intel MKL加速的情况下，CPU降低并不明显，而稳定性下降非常多应该是代码的适配问题。因此，暂时不要使用Intel MKL。

目前测试发现，使用最新的FFTW3版本，使用AVX，AVX2加速的情况下，CPU开销更高，性能更差，应该是代码存在BUG。因此，暂时不要使用自己编译的FFTW3，使用系统自带的版本即可。

手机等设备的设置参考ubuntu 16.04系统LimeSDR V1.4使用OpenAirInterface搭建LTE实验环境最后的介绍。

上述的代码如果下载困难，可以从本站下载一份拷贝。
```
