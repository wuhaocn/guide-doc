#!/bin/bash

echo '' > readme.md
echo '##### 文件目录' >> readme.md
echo '' >> readme.md
echo '##### 时间' >> readme.md
date +%Y%m%d%s >> readme.md
echo '##### 详细文件目录' >> readme.md
echo '```' >> readme.md
ls -lR >> readme.md
echo '```' >> readme.md