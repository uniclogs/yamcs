# UniClOGS Yamcs

[![License](https://img.shields.io/github/license/oresat/uniclogs-yamcs)](./LICENSE)
[![issues](https://img.shields.io/github/issues/oresat/uniclogs-yamcs)](https://github.com/oresat/uniclogs-yamcs/issues)

[Yamcs] based mission control software for UniClOGS

## How to run

**Note:** On systems with older versions of Java, like Java v8, the
`<release>11</release>` line in `pom.xml` will cause a release error. Change
that line to `<!-- <release>11</release> -->`) to fix that error. This can't be
merged in as that change will cause problems with newer versions of java that
`uniclogs-yamcs` is more targeted for.

- Install dependencies `maven npm`
- Run `$ mvn yamcs:run`
- Open `http://localhost:8090` in a web browser.
- Default `user:passwd` is `admin:admin`

## Resources

- [XTCE Element Description (CCSDS Green Book)](https://public.ccsds.org/Pubs/660x1g1.pdf)
- [XTCE (CCSDS Green Book)](https://public.ccsds.org/Pubs/660x2g2.pdf)

[Yamcs]:https://yamcs.org/
