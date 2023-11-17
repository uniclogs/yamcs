#!/usr/bin/env python

import cfdp
import logging
from time import sleep
from os.path import abspath
from cfdp.filestore import NativeFileStore, VirtualFileStore
from cfdp.transport.udp import UdpTransport


GS_ID = 10
GS_ADDR = ('127.0.0.1', 10017)

SAT_ID = 20
SAT_ADDR = ('127.0.0.1', 10018)

TRNS_MODE = cfdp.TransmissionMode.UNACKNOWLEDGED


def main():
    # Setup logging
    logging.basicConfig(level=logging.DEBUG)

    # Setup transport
    udp_tp = UdpTransport(routing={"*": [GS_ADDR]})
    udp_tp.bind(*SAT_ADDR)

    # Setup the FS Handles
    lfs = cfdp.CfdpEntity(
        entity_id=SAT_ID,
        filestore=NativeFileStore(abspath('./sat')),
        transport=udp_tp
    )

    try:
        while True:
            opt: str = input("> ")

            if(opt == 'ls'):
                ls_res_fn = '/.listing.remote'
                txid = lfs.put(
                    destination_id=GS_ID,
                    transmission_mode=TRNS_MODE,
                    messages_to_user=[
                        cfdp.DirectoryListingRequest(remote_directory='/',
                                                    local_file=ls_res_fn)
                    ]
                )

                print(f'Waiting on transaction with id #{txid}... ', end=None)
                while not lfs.is_complete(txid):
                    report = lfs.report(txid)
                    if(report is not None):
                        print(f'TX-REPORT: {report}')
                print('Done!')
                file = lfs.filestore.open(ls_res_fn, mode='r')
                print(f'REMOTE: {file.read()}')
            elif(opt.lower().startswith('put')):
                pieces = opt.split(' ')
                if(len(pieces) != 2):
                    print('usage: put <filename>')
                    continue
                file_path = lfs.filestore.get_virtual_path(pieces[1])

                if not lfs.filestore.is_file(file_path):
                    print(f'File not found -> {lfs.filestore.get_virtual_path(file_path)}')
                    continue

                txid = lfs.put(
                    destination_id=GS_ID,
                    transmission_mode=TRNS_MODE,
                    source_filename=file_path,
                    destination_filename=file_path,
                    segmentation_control=True
                    
                )

                while not lfs.is_complete(txid):
                    report = lfs.report(txid)
                    if(report is not None):
                        print(f'TX-REPORT: {report}')
            elif(opt == 'q'):
                print('Goodbye!')
                break
    except KeyboardInterrupt:
        pass
    finally:
        lfs.shutdown()
        udp_tp.unbind()

if __name__ == '__main__':
    main()