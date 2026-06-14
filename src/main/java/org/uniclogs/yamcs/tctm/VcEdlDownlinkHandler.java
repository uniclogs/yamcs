package org.uniclogs.yamcs.tctm;

// liberated from VcTmPacketHandler.java
// TODO: figure out what is actually necessary
import org.yamcs.ConfigurationException;
import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.events.EventProducer;
import org.yamcs.events.EventProducerFactory;
import org.yamcs.logging.Log;
import org.yamcs.tctm.AggregatedDataLink;
import org.yamcs.tctm.PacketPreprocessor;
import org.yamcs.tctm.TcTmException;
import org.yamcs.tctm.TmPacketDataLink;
import org.yamcs.tctm.TmSink;
import org.yamcs.tctm.ccsds.VcDownlinkHandler;
import org.yamcs.tctm.ccsds.VcDownlinkManagedParameters;
import org.yamcs.tctm.ccsds.DownlinkTransferFrame;
import org.yamcs.time.Instant;
import org.yamcs.time.TimeService;
import org.yamcs.utils.YObjectLoader;
import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractTmDataLink;
import org.yamcs.tctm.TcTmException;
import org.yamcs.tctm.ccsds.DownlinkTransferFrame;
import org.yamcs.tctm.ccsds.PacketDecoder;
import org.yamcs.tctm.ccsds.VcDownlinkHandler;
import org.yamcs.time.Instant;
import org.yamcs.utils.StringConverter;
import org.yamcs.utils.TimeEncoding;


import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;


// this is stolen from th existing YAMCS code, and a lot of stuff there was private / protected, so I've cut out everything that gave errors and hardcoded it.
// While this is awful, my focus right now is on making it work.
// TODO: fix this.



// liberated from SampleVcaHandler.java
public class VcEdlDownlinkHandler extends AbstractTmDataLink implements VcDownlinkHandler {
    private Instant ertime;
    private long frameSeqCount;
	private boolean hasOcf;

    @Override
    public void init(String instance, String name, YConfiguration config) {
        super.init(instance, name, config);
    }

    @Override
    public void handle(DownlinkTransferFrame frame) {
        if (isDisabled()) {
            log.trace("Dropping frame for VC {} because the link is disabled", frame.getVirtualChannelId());
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Processing frame VC {}, SEQ {}, FHP {}, DS {}, DE {}", frame.getVirtualChannelId(),
                    frame.getVcFrameSeq(),
                    frame.getFirstHeaderPointer(), frame.getDataStart(), frame.getDataEnd());
        }
        ertime = frame.getEarthRceptionTime();
        frameSeqCount = frame.getVcFrameSeq();

		// this implementation ignores the data header.

		// Because of the way that the mission database is set up, we don't want the packet that we process to have stuff from the USLP frame or the data header.
		// This strips that down to just the data field (minus the data header) and creates the used packet.
		byte[] packet_data = new byte[frame.getDataEnd() - frame.getDataStart() - 1]; // - 1 gets rid of the data header, which we don't use.
		System.arraycopy(frame.getData(), frame.getDataStart() + 1, packet_data, 0, packet_data.length);
		handlePacket(packet_data);
    }

    private void handlePacket(byte[] p) {
        log.info("Received packet of length {}: {}", p.length, StringConverter.arrayToHexString(p, true));
        TmPacket pwt = new TmPacket(timeService.getMissionTime(), p);
        pwt.setEarthReceptionTime(ertime);
        pwt.setFrameSeqCount(frameSeqCount);
		pwt.setGenerationTime(TimeEncoding.getWallclockTime());

		pwt.setSequenceCount((int)frameSeqCount); // at present use the frame count as sequence count.

        processPacket(pwt);
        updateStats(p.length);
    }

    @Override
    protected Status connectionStatus() {
        return Status.OK;
    }

    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }
}


