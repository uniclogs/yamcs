create stream cfdp_in(gentime TIMESTAMP, entityId long, seqNum int, pdu binary)
create stream cfdp_out(gentime TIMESTAMP, entityId long, seqNum int, pdu binary)
