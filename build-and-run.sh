#!/bin/bash

mv .dockerignore .dockerignore-ignore
docker-compose down
docker-compose up --build
mv .dockerignore-ignore .dockerignore
