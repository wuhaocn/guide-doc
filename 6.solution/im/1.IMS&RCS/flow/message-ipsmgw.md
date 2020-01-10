### 消息流程
#### 消息流程
* cscf代表i/s-cscf网元
* sbc代表pcscf网元
* im代表ipsmgw网元

#### 参考资料
* [TS-124.341 V8.3.0](https://www.etsi.org/deliver/etsi_ts/124300_124399/124341/08.03.00_60/ts_124341v080300p.pdf)
* [Short Message Service (SMS) Test Solutions](https://www.gl.com/newsletter/short-message-service-sms-test-solutions-over-lte-ims-umts-gsm-networks-newsletter.html)

```puml

uea -> pdngw: message(SMS-SUBMIT)
pdngw -> sbc: message(SMS-SUBMIT)
sbc -> cscf: message(SMS-SUBMIT)
cscf -> im: message(SMS-SUBMIT)

im --> cscf: 202
cscf --> sbc: 202
sbc --> pdngw: 202
pdngw --> uea: 202


im -> cscf: message(SMS-DELIVER)
cscf -> sbc: message(SMS-DELIVER)
sbc -> pdngw: message(SMS-DELIVER)
pdngw -> ueb: message(SMS-DELIVER)
ueb --> pdngw: 200
pdngw --> sbc: 200
sbc --> cscf: 200
cscf --> im: 200


im -> cscf: message(SMS-SUBMIT-REPORT)
cscf -> sbc: message(SMS-SUBMIT-REPORT)
sbc -> pdngw: message(SMS-SUBMIT-REPORT)
pdngw -> uea: message(SMS-SUBMIT-REPORT)

uea --> pdngw: 200
pdngw --> sbc: 200
sbc --> cscf: 200
cscf --> im: 200


@enduml

```


