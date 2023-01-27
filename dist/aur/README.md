# uniclogs-yamcs.aur

The AUR package for uniclogs-yamcs

This package is automatically built via Github-Actions and published to [Oresat Packages](https://packages.oresat.org/arch).

## Adding OreSat-Packages to your mirrors list

Your default mirrors may look something like:

```
/etc/pacman.d/mirrorlist
___
Server = http://ftp.halifax.rwth-aachen.de/archlinux/$repo/os/$arch
```

In order to tell pacman to use OreSat's "mirror"

Add the following to the above file:

```
Server = https://packages.oresat.org/arch/$repo/$arch/
```

Once the mirrors list has been updated run the following command:

`$` `sudo pacman -Syu`

This will refresh the package mirrors indices, ensuring the OreSat repo will now be polled for packages.
