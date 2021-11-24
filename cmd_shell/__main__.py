'''UniClOGS Yamcs command shell
'''
# System imports
import cmd
from cmd import Cmd
from yamcs.client import YamcsClient
from yamcs.core.auth import Credentials

# Relative imports
from . import YAMCS_USERNAME, YAMCS_PASSWORD, YAMCS_URL, YAMCS_INSTANCE
from .opd import opd_cmd, opd_help
from .canopen import node_cmd, node_help, sdo_cmd, sdo_help
from .c3 import c3_cmd, \
                c3_help, \
                fs_cmd, \
                fs_help, \
                fw_cmd, \
                fw_help, \
                rtc_cmd, \
                rtc_help
from .tx import tx_cmd, tx_help


class TelecommandShell(Cmd):
    def __init__(self):
        super().__init__()
        self.prompt = '> '
        self.intro = 'UniClOGS Yamcs command shell. Type ? to list commands'
        self.credentials = Credentials(username=YAMCS_USERNAME, password=YAMCS_PASSWORD)
        try:
            self.client = YamcsClient(YAMCS_URL, credentials=self.credentials)
        except Exception:
            raise ConnectionRefusedError()
        self.processor = self.client.get_processor(instance=YAMCS_INSTANCE, processor='realtime')
        self.session = self.processor.create_command_connection()

        print(f'Loged into to Yamcs (http://{YAMCS_URL}) as {YAMCS_USERNAME}')

    def do_exit(self, inp):
        print('Bye')
        return True

    def help_exit(self):
        print('exit the application. Shorthand: x q Ctrl-D Ctrl-C.')

    def do_opd(self, inp):
        try:
            opd_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_opd(self):
        opd_help()

    def do_node(self, inp):
        try:
            node_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_node(self):
        node_help()

    def do_sdo(self, inp):
        try:
            sdo_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_sdo(self):
        sdo_help()

    def do_c3(self, inp):
        try:
            c3_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_c3(self):
        c3_help()

    def do_fs(self, inp):
        try:
            fs_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_fs(self):
        fs_help()

    def do_fw(self, inp):
        try:
            fw_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_fw(self):
        fw_help()

    def do_rtc(self, inp):
        try:
            rtc_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_rtc(self):
        rtc_help()

    def do_tx(self, inp):
        try:
            tx_cmd(self.session, inp)
        except Exception as exc:
            print(exc)

    def help_tx(self):
        tx_help()

    def default(self, inp):
        if inp == 'x' or inp == 'q':
            return self.do_exit(inp)

        print('Unknown command: {}'.format(inp))


if __name__ == '__main__':
    try:
        shell = TelecommandShell()
        shell.cmdloop()
    except ConnectionRefusedError:
        print(f'Failed to connect to the Yamcs service at http://{YAMCS_URL}\nIs the Yamcs service running?')
    except KeyboardInterrupt:
        print('Forcefully stopping shell...')
