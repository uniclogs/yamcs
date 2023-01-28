# Yamcs Grafana

## Install grafana

### Arch Linux

- `$ pacman -S grafana`

### Debian Linux

- `$ sudo apt-get install -y apt-transport-https software-properties-common wget`
- `$ wget -q -O - https://packages.grafana.com/gpg.key | sudo apt-key add -`
- `$ echo "deb https://packages.grafana.com/oss/deb stable main" | sudo tee -a /etc/apt/sources.list.d/grafana.list`
- `sudo apt-get update`
- `sudo apt-get install grafana`

## Add Yamcs plugin to Grafana

- `$ sudo grafana-cli --pluginUrl https://github.com/yamcs/grafana-yamcs/releases/download/v2.2.0/yamcs-yamcs-datasource-2.2.0.zip plugins install yamcs-yamcs-datasource`
- Edit `allow_loading_unsigned_plugins` line under `[plugins]` in `/etc/grafana.ini` under:
  - `allow_loading_unsigned_plugins = yamcs-yamcs-datasource`
- `$ sudo systemctl restart grafana-server`

## Log into Grafana and add Yamcs

- Open `http://localhost:3000` in a web browser.
- Default `user:passwd` is `admin:admin`
- Go to Configurations > Data sources:
  - Type in `Yamcs` and click on it
  - **Type** in `http://localhost:8090` into URL
  - Click `Save & test`

## Import Grafana Configs

- `Create >  Import` and click `Upload JSON file` and add `grafana-dashboard.json`

## Save Grafana Configs

- `Share > Export > View JSON` and dump contents into `grafana-dashboard.json`
