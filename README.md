# UniClOGS Yamcs

[![](https://img.shields.io/github/license/oresat/uniclogs-yamcs)](./LICENSE)
[![](https://img.shields.io/github/issues/uniclogs/yamcs/bug?color=red&label=Open%20Bug%20Reports)](https://github.com/oresat/uniclogs-yamcs/issues)

[Yamcs] based mission control software for UniClOGS

&nbsp;

***

## Installation and Usage

### Docker Install:

> **Note:**
>
> The security module is enabled by default for this docker-image, and the default *username/password* is `admin/admin`. Please remember to change this upon first login for non-development environments.

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

### Manual Docker Build:

Run Make at the root of the project

* `make`

Change into the docker directory

* `cd dist/docker`

Build via Docker-Compose

* `docker-compose build`

Verify that the `uniclogs-yamcs` image has succesfully built

* `docker images`

&nbsp;

***

## Development Quick Start

### Initialize yamcs configs submodule

`$` `git submodule update --init --recursive`

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