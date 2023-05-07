 docker run \
   --interactive \
   --tty \
   -p8090:8090 \
   -p10010:10010 \
   -p10015:10015 \
   -p10016:10016 \
   -p10020:10020 \
   -p10025:10025 \
   -p10026:10026 \
   --volume yamcs_data:/srv/yamcs \
   --volume "$(pwd)/src/main/yamcs/etc:/etc/yamcs" \
   uniclogs-yamcs:latest