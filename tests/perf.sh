#!/bin/bash

SHORT_URL=$(curl -X POST -H 'Content-Type: application/json' -d 'https://www.youtube.com/watch?v=dQw4w9WgXcQ' http://localhost:8080/shorten)
hey -c 100 -h2 -z 30s -disable-redirects $SHORT_URL
