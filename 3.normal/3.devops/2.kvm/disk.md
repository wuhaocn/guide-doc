## CentOS下KVM增加磁盘/磁盘扩容/在线扩容
参考：https://www.cnblogs.com/EasonJim/p/9587909.html
### 一、磁盘镜像操作（适用于raw和qcow2格式）

```
1、创建镜像

qemu-img create -f qcow2(格式) /kvm/centos1_1.qcow2(路径) 5G(容量)
2、修改镜像容量，扩容

qemu-img resize /kvm/centos1_1.qcow2(路径) +10G(增加的容量)
 3、查看镜像

qemu-img info /kvm/centos1_1.qcow2(路径)
4、删除镜像

rm -rf（不用说了吧，秒懂）

```


### 二、给KVM增加硬盘镜像

```
说明：增加只能通过XML

注意：请不要直接修改XML文件，不然会造成无法保存的问题，并且不生效，一定要关机后才可修改！

1、增加

复制代码
virsh edit KVM名称
# 增加如下内容，注意在原有硬盘下面增加
    <disk type='file' device='disk'>
      <driver name='qemu' type='qcow2' cache='none'/>
      <source file='/kvm/centos1_1.qcow2'/>
      <target dev='vdb' bus='virtio'/>
　　　 <address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x0'/>
    </disk>
复制代码
其中<address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x0'/>这个可以省略，不影响。作用是增加的位置。

通常在新建的时候这个最好省略，会自动生成的。

2、修改

略

3、删除

略

```

### 三、在线扩容动态增加硬盘（重点）

```
说明：扩容很简单，只需要修改一下镜像的大小即可。

下面介绍的是动态增加硬盘不关机进行操作。

1、新建硬盘

略

2、热加载硬盘

virsh attach-disk KVM实例名 /kvm/centos1_1.qcow2(路径) vdb(设备码) --subdriver=qcow2(类型)
3、动态分离硬盘

virsh detach-disk KVM实例名 /kvm/centos1_1.qcow2(路径)
4、查看实例

virsh dumpxml KVM实例名
5、保存

virsh save KVM实例名
或者
virsh edit KVM实例名
进行编辑，把新增的信息复制进去保存即可
 
```
 

### 参考：

http://blog.51cto.com/7424593/1735600

http://blog.fens.me/vps-kvm-disk/

http://blog.51cto.com/liqingbiao/1741244

http://blog.51cto.com/daixuan/1743047

http://www.vpsee.com/2012/08/resize-kvm-vm-image/

https://cloud.tencent.com/info/61ac5f3e178ebee40d9b336ef07c2f4d.html