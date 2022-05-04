#!/bin/bash

SCRIPTPATH=$(dirname "$0")
docker run --rm -i --network="host" grafana/k6 \
  run -e BASE_URL=http://127.0.0.1:8080 --quiet - <"${SCRIPTPATH}/smoke.js"
