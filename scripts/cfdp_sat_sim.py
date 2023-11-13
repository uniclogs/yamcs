#!/usr/bin/env python
import cfdp
import argparse


def main():
    parser = argparse.ArgumentParser(prog='cfdp_sat_sim')
    parser.add_argument('filestore_dir',
                        type=str,
                        help='Directory to scan for sendable-files.')
    parser.add_argument('--host', '-H',
                        type=str,
                        dest='host',
                        default='127.0.0.1',
                        help='Host to send to.')
    parser.add_argument('--tx-port',
                        type=int,
                        dest='tx_port',
                        default=10017,
                        help='Port to send on.')
    parser.add_argument('--rx-port',
                        type=int,
                        dest='rx_port',
                        default=10018,
                        help='Port to receive on.')
    args = parser.parse_args()

    fs = cfdp.filestore.native.NativeFileStore(args.filestore_dir)
    print(fs.list_directory())

    config = cfdp.Config(
        local_entity=cfdp.LocalEntity(200, f'127.0.0.1:{args.rx_port}'),
        remote_entities=[cfdp.RemoteEntity(100, f'{args.host}:{args.tx_port}')],
        filestore=fs,
        transport=cfdp.transport.UdpTransport())
    
    


if __name__ == '__main__':
    main()