create stream cfdp_in as select substring(packet, 6) as pdu from cfdp_tm_realtime where extract_short(packet, 0) = 6141
create stream cfdp_out (gentime TIMESTAMP, entityId long, seqNum int, pdu binary)
insert into cfdp_tc_realtime select gentime, 'cfdp-service' as origin, seqNum, '/yamcs/cfdp/upload' as cmdName, unhex('17FDC0000000') + pdu as binary from cfdp_out