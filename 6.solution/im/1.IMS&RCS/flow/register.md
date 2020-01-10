### register
* 注册流程
  * cscf为i/s-cscf
  * sbc为pcscf
* 参考

  *[参考流程及报文](http://sharetechnote.com/html/IMS_SIP_Registration.html)    

#### reg1

```puml
ue -> pdngw: register1
pdngw -> sbc: register1
sbc -> cscf: register1
cscf->hss: uar
hss->cscf: uaa
cscf->hss: mar
hss->cscf: maa
cscf -> sbc: 200
sbc -> pdngw: 200
pdngw --> ue: 200

@enduml

```

#### reg2

```puml
ue -> pdngw: register2
pdngw -> sbc: register2
sbc -> cscf: register2
cscf->hss: uar
hss->cscf: uaa
cscf->hss: sar
hss->cscf: saa
cscf -> sbc: 200
sbc -> pdngw: 200
pdngw --> ue: 200


@enduml

```


