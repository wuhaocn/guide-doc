### ims

```
sudo apt-get install dnsmasq

sudo systemctl restart dnsmasq
```

```
172.16.106.68 bsf.mnc000.mcc460.pub.3gppnetwork.org
172.16.106.68 config.rcs.mnc000.mcc460.pub.3gppnetwork.org
172.16.106.68 xnq.config.rcs.chinamobile.com
172.16.106.68 config.rcs.chinamobile.com
```

```
dig -x 172.16.106.68 bsf.mnc000.mcc460.pub.3gppnetwork.org
dig -x 172.16.106.68 config.rcs.mnc000.mcc460.pub.3gppnetwork.org
dig -x 172.16.106.68 xnq.config.rcs.chinamobile.com
dig -x 172.16.106.68 config.rcs.chinamobile.com
```
