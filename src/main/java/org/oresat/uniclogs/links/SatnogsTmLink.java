package org.oresat.uniclogs.links;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yamcs.ConfigurationException;
import org.yamcs.YConfiguration;
import org.yamcs.YamcsServer;
import org.yamcs.tctm.AbstractTmDataLink;
import org.oresat.uniclogs.services.SatnogsTransferService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class SatnogsTmLink extends AbstractTmDataLink {

    SatnogsTransferService tsfrService;

    @Override
    protected Status connectionStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void doStart() {

        try {

            URL urlArtifacts = new URL("https://db.satnogs.org/api/telemetry/?format=json&satellite=98867");
            HttpURLConnection conArtifacts = (HttpURLConnection) urlArtifacts.openConnection();
            conArtifacts.setRequestProperty("Authorization", "Bearer " + System.getenv("SATNOGSDB_AUTH_TOKEN"));
            conArtifacts.setRequestMethod("GET");
            conArtifacts.setRequestProperty("contentType", "application/json; charset=utf-8");

            BufferedReader inArtifacts = new BufferedReader(
                    new InputStreamReader(conArtifacts.getInputStream()));
            String inputLineArtifacts;
            StringBuilder contentArtifacts = new StringBuilder();
            while ((inputLineArtifacts = inArtifacts.readLine()) != null) {
                contentArtifacts.append(inputLineArtifacts);
            }
            inArtifacts.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(String.valueOf(contentArtifacts));

            // Access specific fields from the JSON structure
            String contentArtifactsSet = jsonNode.get("results").asText();

            tsfrService.getDeduplication(contentArtifactsSet);

        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
    }

    @Override
    protected void doStop() {
        // TODO Auto-generated method stub

    }

    public SatnogsTmLink(String instanceName, YConfiguration config) {
        String envName = config.getString("envService");
        this.tsfrService = YamcsServer.getServer().getInstance(instanceName).getService(SatnogsTransferService.class, envName);

        if (this.tsfrService == null) {
            throw new ConfigurationException("Service " + envName + " does not exist or is not of class SatnogsTransferService.");
        }
    }

    public SatnogsTmLink(String instanceName) {
        this(instanceName, YConfiguration.emptyConfig());
    }

}
