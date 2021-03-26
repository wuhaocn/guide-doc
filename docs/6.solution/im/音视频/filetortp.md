### ffmpeg 转音频流

```
ffmpeg -i 13388.wav -c:a libopencore_amrwb -ac 1 -ar 16000 -b:a 7.95K -y 13388.amr
ffmpeg -i 13388.wav -c:a libspeex -ac 1 -ar 8000 -y 13388.speex
ffmpeg -i 13388.wav -c:a libopus -ac 1 -ar 8000  -y 13388.ogg
ffmpeg -i 13388.wav -c:a libopus -ac 1 -ar 8000 -b:a 2.4K -payload_type 116 -y 13389.ogg

ffmpeg -re -i 13388.amr -vcodec copy -f rtp rtp://127.0.0.1:1234
ffmpeg -re -i 13388.amr -vn -acodec copy -f rtp rtp://192.168.137.127:37028
ffmpeg -re -i 13388.ogg -vn -acodec copy -f rtp -payload_type 116 rtp://172.16.172.18:37026

ffmpeg -re -i 13389.ogg -ac 1 -vn -acodec copy -f rtp -payload_type 116 rtp://172.16.172.18:37048
```
