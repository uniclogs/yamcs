# Run
Sending a file over Yamcs

## Yamcs
Clone from CFDP branch and add submodule
``` bash
# ssh: git clone -b 63-yamcs-uslp-sendingwrite-cfdp-testing git@github.com:uniclogs/yamcs.git
git clone -b 63-yamcs-uslp-sendingwrite-cfdp-testing https://github.com/uniclogs/yamcs.git

git submodule update --init src/main/yamcs

# Build plugin
mvn package -DskipTests -DyamcsVersion=5.12.6
```

``` bash
bash ./scripts/run-with-cfdp.sh
```

## C3

Start virtual CAN for C3
``` bash
sudo modprobe vcan
sudo ip link add dev vcan0 type vcan
sudo ip link set up vcan0
```

``` bash
cd <path-to-c3>/oresat-c3-software
.venv/bin/python3 -m oresat_c3 -m all -t virtual -d -p 8001
```

Yamcs web UI: http://localhost:8090

### Class 2 upload with the web UI

**Filename must follow the C3 convention**: `card-name_key_unix-time.extension`
*example: `c3_test_$(date +%s).txt`*

1. http://localhost:8090 → pick instance **oresat0_5** (top-right).
2. **Storage (Top right folder icon) > groundstation > Upload**
  
3. **File Transfer (Left pannel) -> New Transfer** Pick the bucket + object
  destination entity `satellite`, **Set Reliable**, submit.

