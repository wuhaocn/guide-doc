### sip 呼叫流程

#### 说明

- cscf 为 i/s-cscf
- sbc 为 pcscf

#### 参考

[流程参考](https://www.gsma.com/futurenetworks/wp-content/uploads/2014/05/FCM.01-v1.1.pdf)

http://www.eventhelix.com/lte/volte/volte-originating-call.pdf
http://www.3glteinfo.com/volte-call-flow-procedures/
https://www.etsi.org/deliver/etsi_ts/129200_129299/129211/06.02.00_60/ts_129211v060200p.pdf
https://www.netmanias.com/en/post/blog/11082/lte-pcrf-volte/policy-control-over-the-rx-interface-using-diameter-for-volte

#### 呼叫振铃及接听

- 流程图

```puml
uea -> pdngw: INVITE
pdngw --> uea: 100 Trying
pdngw -> sbc: INVITE
sbc --> pdngw: 100 Trying
sbc -> cscf: INVITE
cscf --> sbc: 100 Trying

cscf -> tas: INVITE(主叫)
tas --> cscf: 100 Trying

tas -> cscf: INVITE
cscf --> tas: 100 Trying

cscf -> tas: INVITE(被叫)
tas --> cscf: 100 Trying

tas -> cscf: INVITE
cscf --> tas: 100 Trying


cscf --> sbc: INVITE
sbc --> cscf: 100 Trying

sbc --> pdngw: INVITE
pdngw --> sbc: 100 Trying

pdngw --> ueb: INVITE
ueb --> pdngw: 100 Trying

ueb --> pdngw: 183 Session progress
pdngw --> sbc: 183 Session progress
sbc -> cscf: 183 Session progress

sbc->pcrf: aar
pcrf->sbc: aaa

cscf -> tas: 183 Session progress（被叫）
tas -> cscf: 183 Session progress
cscf -> tas: 183 Session progress（主叫）
tas -> cscf: 183 Session progress
cscf --> sbc: 183 Session progress
sbc --> pdngw: 183 Session progress
pdngw --> uea: 183 Session progress


uea --> pdngw: 200
pdngw --> sbc: 200
sbc -> cscf: 200
cscf -> tas: 200(主叫)
tas -> cscf: 200
cscf -> tas: 200(被叫)
tas -> cscf: 200
cscf --> sbc: 200
sbc --> pdngw: 200
pdngw --> ueb: 200

@enduml
```

- 流程说明
