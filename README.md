# UniClOGS Yamcs

[![License](https://img.shields.io/github/license/oresat/uniclogs-yamcs)](./LICENSE)
[![issues](https://img.shields.io/github/issues/oresat/uniclogs-yamcs)](https://github.com/oresat/uniclogs-yamcs/issues)

[Yamcs] based mission control software for UniClOGS

&nbsp;

***

## Installation and Usage

### Docker Install:

> **Note:**
>
> The security module is enabled by default for this image, and the default *username/password* is `admin/admin`.

#### Pull the latest image from the official OreSat image via Dockerhub:

`docker pull oresat/uniclogs-yamcs:latest`

#### Run immediately in detatched mode:

```
docker run \
  --detach \
  -p8090:8090 \
  -p10015:10015 \
  -p10016:10016 \
  -p10020:10020 \
  -p10025:10025 \
  --volume yamcs_data:/srv/yamcs \
  --volume yamcs_configs:/etc/yamcs \
  --name yamcs \
  oresat/uniclogs-yamcs:latest
```

#### Run immediately with interactive TTY enabled:

```
docker run \
  --detach \
  --interactive \
  --tty \
  -p8090:8090 \
  -p10015:10015 \
  -p10016:10016 \
  -p10020:10020 \
  -p10025:10025 \
  --volume yamcs_data:/srv/yamcs \
  --volume yamcs_configs:/etc/yamcs \
  --name yamcs \
  oresat/uniclogs-yamcs:latest
```

### Manual Install:

Fetch the latest pre-packaged artifacts from the [UniClOGS-Yamcs Releases](https://github.com/oresat/uniclogs-yamcs/releases) Page.

&nbsp;

Make the relevant directories:

> **Note:**
>
> The token *`pid`*  will represent the path to the permanent install directory from here on out.

* `sudo install -d -o $USER -g daemon -m0755 pid`
* `sudo install -d -m0755 /etc/yamcs`

&nbsp;

Decompress artifacts:

* `tar xzvf uniclogs-yamcs-<version>-bundle.tar.gz`

&nbsp;

Install the binaries, mission database, and libraries to the install directory

* `mv uniclogs-yamcs-<version>-bundle/bin pid/bin`
* `mv uniclogs-yamcs-<version>-bundle/lib pid/lib`jre17-openjdk
* `mv uniclogs-yamcs-<version>-bundle/mdb pid/mdb`
* `sudo ln -s pid/bin/yamcsd /usr/bin`

&nbsp;

Move configs to `/etc/yamcs`:

* `sudo mv uniclogs-yamcs-<version>-bundle/etc/* /etc/yamcs`
* `sudo mv /etc/yamcs/prod/* /etc/yamcs`

&nbsp;

Run the Yamcs Daemon:
`yamcsd --etc-dir /etc/yamcs --data-dir pid`

&nbsp;

***

## Development Quick Start

### Interface Connection Diagram(s):

Below is a set of diagrams outlining both how Yamcs in its current state works, as well as what the target ICD layout is for the first official release of `uniclogs-yamcs`.

![uniclogs-yamcs](docs/uniclogs-yamcs-fbd.png)

#### Install Dev-Dependencies:

##### Non-Project Dependencies
* Debian/Ubuntu: `sudo apt-get install openjdk-17-jre maven npm`
* Archlinux: `sudo pacman -S jre17-openjdk maven npm`

##### Project Dependencies
* `mvn clean yamcs:debug`
* Open [Local Yamcs] in a browser

> **Note:**
>
> The Security module is disabled by default, though, on ocassion, the Yamcs Auth module may send you to `/auth/authorize` erroneously. Simply re-travese to [Local Yamcs] again to bypass this error.

&nbsp;

***

## Command Shell Usage

The user interface for uniclogs yamcs.

How to:

- Install dependencies: `$ pip install -r cmd_shell/requirements.txt`
- Start Yamcs
- Run `$ python3 -m cmd_shell`

&nbsp;

***

## Appendix and Resources

- [XTCE Element Description (CCSDS Green Book)](https://public.ccsds.org/Pubs/660x1g1.pdf)
- [XTCE (CCSDS Green Book)](https://public.ccsds.org/Pubs/660x2g2.pdf)

[Yamcs]:https://yamcs.org/

[Local Yamcs]:http://localhost:8090/