### install

virt-install --name landslide --ram 2048 --vcpus=4 --disk path=lsGenVm_1604_TAS_TS_19.0.0.qcow2,bus=ide,format=qcow2 --network network=default --force --import --autostart

###

kvm 之 virt-install 工具命令详解
一、virt-install 是一个命令行工具，它能够为 KVM、Xen 或其它支持 libvrit API 的 hypervisor 创建虚拟机并完成 GuestOS 安装；此外，它能够基于串行控制台、VNC 或 SDL 支持文本或图形安装界面。安装过程可以使用本地的安装介质如 CDROM，也可以通过网络方式如 NFS、HTTP 或 FTP 服务实现。对于通过网络安装的方式，virt-install 可以自动加载必要的文件以启动安装过程而无须额外提供引导工具。当然，virt-install 也支持 PXE 方式的安装过程，也能够直接使用现有的磁盘映像直接启动安装过程。

###

二、virt-install 命令有许多选项，这些选项大体可分为下面几大类，同时对每类中的常用选项也做出简单说明。

◇ 一般选项：指定虚拟机的名称、内存大小、VCPU 个数及特性等；

 -n NAME, --name=NAME：虚拟机名称，需全局惟一；

 -r MEMORY, --ram=MEMORY：虚拟机内在大小，单位为 MB；

 --vcpus=VCPUS[,maxvcpus=MAX][,sockets=#][,cores=#][,threads=#]：VCPU 个数及相关配置；

 --cpu=CPU：CPU 模式及特性，如 coreduo 等；可以使用 qemu-kvm -cpu ?来获取支持的 CPU 模式；

◇ 安装方法：指定安装方法、GuestOS 类型等；

 -c CDROM, --cdrom=CDROM：光盘安装介质；

 -l LOCATION, --location=LOCATION：安装源 URL，支持 FTP、HTTP 及 NFS 等，如 ftp://172.16.0.1/pub；

 --pxe：基于 PXE 完成安装；

 --livecd: 把光盘当作 LiveCD；

 --os-type=DISTRO_TYPE：操作系统类型，如 linux、unix 或 windows 等；

 --os-variant=DISTRO_VARIANT：某类型操作系统的变体，如 rhel5、fedora8 等；

 -x EXTRA, --extra-args=EXTRA：根据--location 指定的方式安装 GuestOS 时，用于传递给内核的额外选项，例如指定 kickstart 文件的位置，--extra-args "ks=http://172.16.0.1/class.cfg"

 --boot=BOOTOPTS：指定安装过程完成后的配置选项，如指定引导设备次序、使用指定的而非安装的 kernel/initrd 来引导系统启动等 ；例如：

 --boot cdrom,hd,network：指定引导次序；

 --boot kernel=KERNEL,initrd=INITRD,kernel_args=”console=/dev/ttyS0”：指定启动系统的内核及 initrd 文件；

◇ 存储配置：指定存储类型、位置及属性等；

 --disk=DISKOPTS：指定存储设备及其属性；格式为--disk /some/storage/path,opt1=val1，opt2=val2 等；常用的选项有：

 device：设备类型，如 cdrom、disk 或 floppy 等，默认为 disk；

 bus：磁盘总结类型，其值可以为 ide、scsi、usb、virtio 或 xen；

 perms：访问权限，如 rw、ro 或 sh（共享的可读写），默认为 rw；

 size：新建磁盘映像的大小，单位为 GB；

 cache：缓存模型，其值有 none、writethrouth（缓存读）及 writeback（缓存读写）；

 format：磁盘映像格式，如 raw、qcow2、vmdk 等；

 sparse：磁盘映像使用稀疏格式，即不立即分配指定大小的空间；

 --nodisks：不使用本地磁盘，在 LiveCD 模式中常用；

◇ 网络配置：指定网络接口的网络类型及接口属性如 MAC 地址、驱动模式等；

 -w NETWORK, --network=NETWORK,opt1=val1,opt2=val2：将虚拟机连入宿主机的网络中，其中 NETWORK 可以为：

 bridge=BRIDGE：连接至名为“BRIDEG”的桥设备；

 network=NAME：连接至名为“NAME”的网络；

三、其它常用的选项还有

 model：GuestOS 中看到的网络设备型号，如 e1000、rtl8139 或 virtio 等；

 mac：固定的 MAC 地址；省略此选项时将使用随机地址，但无论何种方式，对于 KVM 来说，其前三段必须为 52:54:00；

 --nonetworks：虚拟机不使用网络功能；

◇ 图形配置：定义虚拟机显示功能相关的配置，如 VNC 相关配置；

 --graphics TYPE,opt1=val1,opt2=val2：指定图形显示相关的配置，此选项不会配置任何显示硬件（如显卡），而是仅指定虚拟机启动后对其进行访问的接口；

 TYPE：指定显示类型，可以为 vnc、sdl、spice 或 none 等，默认为 vnc；

 port：TYPE 为 vnc 或 spice 时其监听的端口；

 listen：TYPE 为 vnc 或 spice 时所监听的 IP 地址，默认为 127.0.0.1，可以通过修改/etc/libvirt/qemu.conf 定义新的默认值；

 password：TYPE 为 vnc 或 spice 时，为远程访问监听的服务进指定认证密码；

 --noautoconsole：禁止自动连接至虚拟机的控制台；

◇ 设备选项：指定文本控制台、声音设备、串行接口、并行接口、显示接口等；

 --serial=CHAROPTS：附加一个串行设备至当前虚拟机，根据设备类型的不同，可以使用不同的选项，格式为“--serial type,opt1=val1,opt2=val2,...”，例如：

 --serial pty：创建伪终端；

 --serial dev,path=HOSTPATH：附加主机设备至此虚拟机；

 --video=VIDEO：指定显卡设备模型，可用取值为 cirrus、vga、qxl 或 vmvga；

◇ 虚拟化平台：虚拟化模型（hvm 或 paravirt）、模拟的 CPU 平台类型、模拟的主机类型、hypervisor 类型（如 kvm、xen 或 qemu 等）以及当前虚拟机的 UUID 等；

 -v, --hvm：当物理机同时支持完全虚拟化和半虚拟化时，指定使用完全虚拟化；

 -p, --paravirt：指定使用半虚拟化；

 --virt-type：使用的 hypervisor，如 kvm、qemu、xen 等；所有可用值可以使用’virsh capabilities’命令获取；

◇ 其它：

 --autostart：指定虚拟机是否在物理启动后自动启动；

 --print-xml：如果虚拟机不需要安装过程(--import、--boot)，则显示生成的 XML 而不是创建此虚拟机；默认情况下，此选项仍会创建磁盘映像；

 --force：禁止命令进入交互式模式，如果有需要回答 yes 或 no 选项，则自动回答为 yes；

 --dry-run：执行创建虚拟机的整个过程，但不真正创建虚拟机、改变主机上的设备配置信息及将其创建的需求通知给 libvirt；

 -d, --debug：显示 debug 信息；
