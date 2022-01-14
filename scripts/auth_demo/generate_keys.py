#!/usr/bin/env python3
from rsa import newkeys

(public_key, private_key) = newkeys(736)

with open('id_rsa', 'w+') as file:
    file.write(private_key.save_pkcs1("PEM").decode('utf-8'))

with open('id_rsa.pub', 'w+') as file:
    file.write(public_key.save_pkcs1("PEM").decode('utf-8'))
