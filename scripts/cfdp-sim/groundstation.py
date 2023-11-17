#!/usr/bin/env python


import cfdp
import logging
from time import sleep
from cfdp.transport.udp import UdpTransport
from cfdp.filestore import NativeFileStore

GS_ID = 1
GS_ADDR = ('127.0.0.1', 10017)

SAT_ID = 2
SAT_ADDR = ('127.0.0.1', 10018)


def main():
    # Setup logging
    logging.basicConfig(level=logging.DEBUG)

    # Setup transport
    udp_tp = UdpTransport(routing={"*": [SAT_ADDR]})
    udp_tp.bind(*GS_ADDR)

    # Start the GS file-server
    gs_fs = cfdp.CfdpEntity(
        entity_id=GS_ID,
        filestore=NativeFileStore("./gs"),
        transport=udp_tp
    )
    try:

        print("Press Ctrl+C to stop!")
        while True: sleep(0.05)
    except KeyboardInterrupt:
        print('Goodbye!')
    finally:
        gs_fs.shutdown()
        udp_tp.unbind()


if __name__ == '__main__':
    main()