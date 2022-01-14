from rsa import verify
from rsa.pkcs1 import VerificationError

OFFSET = 10
SAT_ADDR = ('127.0.0.1', 8700 + OFFSET)
MISC_ADDR = ('127.0.0.1', 8800 + OFFSET)


def addr_to_str(host, port) -> str:
    return f'tcp://{host}:{port}/'


def verify_response(signature, message, public_key) -> str:
    # Split the response payload into the signature and the response message
    #   then verify the message against the signature
    hash_method = None
    status = None
    try:
        hash_method = verify(message, signature, public_key)
        status = 'VERIFIED'
    except VerificationError:
        hash_method = '?'
        status = 'UNVERIFIED'

    # Return the verification result
    return f'[{status}]({len(signature)} byte {hash_method} Signature): {message}'
