## Ubuntu 18.04 srsLte +LimeSDR
安装前请移除之前软件包
```
sudo apt-get purge -y --auto-remove lime*
sudo apt-get purge -y --auto-remove soapy*
sudo apt-get purge -y --auto-remove pothos*
```

### 1.添加PPA源
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
uhd

```

git clone https://github.com/EttusResearch/uhd
cd uhd 
git checkout release_003_010_000_000 

cd uhd/host/
mkdir build
cd build
cmake ../
make -j8     //由于编译比较慢，所以选择使用8个”处理器”来提速
make test
sudo make install
sudo ldconfig    //更新动态链接库

sudo uhd_images_downloader  

or

 sudo apt-get install -y libboost-all-dev libusb-1.0-0-dev python-cheetah doxygen python-docutils g++ cmake python-setuptools python-mako
 
 
```
zmq
```
apt-get install libzmq3-dev
```

### 2.LimeSuite

这个是官方的驱动，包括配置界面 LimeSuitGUI 和 工具 LimeUtil，USB 驱动等

Lime Suite是一个软件集合，支持多个硬件平台，包括LimeSDR，用于LMS7002M收发器RFIC的驱动程序以及用于使用基于LMS7的硬件进行开发的其他工具。安装Lime Suite可使许多SDR应用程序（例如GQRX）通过捆绑的SoapySDR支持模块与受支持的硬件一起使用。

![](http://wiki.myriadrf.org/images/e/e9/Lime_suite_comps.png)
 

```
sudo apt-get install -y limesuite liblimesuite-dev limesuite-udev limesuite-images
sudo apt-get install -y soapysdr-module-lms7
```

### 3.编译环境

```
sudo apt-get install cmake g++
sudo apt-get install libpython-dev python-numpy swig
```



### 4.SoapySDR
SoapySDR是开源的规范化的软件无线电开发 API 和运行库，用于操作SDR硬件设备。使用SoapySDR，您可以在很多环境下对SDR设备进行实例化、配置以及流输入输出操作。
绝大多数市面的软件无线电设备都受到SoapySDR支持，并且有非常多的开源应用软件依赖SoapySDR运行库。SoapySDR还带有GNU Radio、Pothos SDR开发框架的接口模块。
SoapySDR支持的操作系统包括Linux、Win、OSX，支持的SDR硬件设备包括LimeSDR、HackRF、BladeRF、SDRPlay、PlutoSDR、AirSpy、RTL-SDR、UHD（USRP）、Novena RF等等。
![](https://oscimg.oschina.net/oscnet/up-3048258933efdeb93ee0cf69d2753461b6b.png)


```

sudo apt-get install -y soapysdr-tools
```
or

```
git clone https://github.com/pothosware/SoapySDR.git
cd SoapySDR

git pull origin master

mkdir build
cd build
cmake ..
make -j4
sudo make install
sudo ldconfig #needed on debian systems
```

```
SoapySDRUtil --info
SoapySDRUtil --find
```


安装 Pothos 和 toolkits
sudo apt-get install -y pothos-all
sudo apt-get install -y python-pothos
sudo apt-get install -y python3-pothos
sudo apt-get install -y pothos-python-dev


安装Soapy SDR runtime
sudo apt-get install soapysdr-tools
sudo apt-get install python-soapysdr python-numpy
sudo apt-get install python3-soapysdr python3-numpy

安装Soapy SDR驱动
sudo apt-get install osmo-sdr soapysdr-module-osmosdr

### 5.GNU Radio
这个就是数字信号DSP处理的核心了，多个功能模块，信号发生器，滤波器等等。
```
sudo apt-get install -y gnuradio
sudo apt-get install -y libvolk1-bin libvolk1-dev
sudo apt-get install -y gnuradio gnuradio-dev
sudo apt-get install -y gr-fcdproplus
sudo apt-get install -y gr-iqbal
sudo apt-get install -y gr-osmosdr
sudo apt-get install -y libosmodsp libosmodsp-dev
```

### 6.gr-osmosdr

这个是 osmosdr 针对 gnuradio 的一个插件
```
sudo apt-get install gr-osmosdr
```
### 7.srsGUI


```
sudo apt-get install libboost-system-dev libboost-test-dev libboost-thread-dev libqwt-qt5-dev qtbase5-dev
```

```
git clone https://github.com/suttonpd/srsgui.git
cd srsGUI
mkdir build
cd build
cmake ..
make
sudo make install
sudo ldconfig
```
### 8.srsLTE

```
sudo apt-get install cmake libfftw3-dev libmbedtls-dev libboost-program-options-dev  libboost-thread-dev libconfig++-dev libsctp-dev
```

```
git clone https://github.com/srsLTE/srsLTE.git
cd srsLTE
mkdir build
cd build
cmake ../
make
make test
sudo make install

srslte_install_configs.sh user
```

### 9.run

```
srsepc

srsenb
```