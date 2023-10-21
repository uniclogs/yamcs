package org.oresat.uniclogs.links;


import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractTmDataLink;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class SatnogsTmLink extends AbstractTmDataLink {




    public SatnogsTmLink() {
        super();
    }


    @Override
    protected Status connectionStatus() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    protected void doStart() {
        try {
            URL urlArtifacts = new URL("https://db-dev.satnogs.org/api/artifacts/");
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
            this.log.info("DB Satnogs Artifacts: " + inputLineArtifacts);
            inArtifacts.close();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        try {
            URL urlModes = new URL("https://db-dev.satnogs.org/api/modes/");
            HttpURLConnection conModes = (HttpURLConnection) urlModes.openConnection();
            conModes.setRequestProperty("Authorization", "Bearer " + System.getenv("SATNOGSDB_AUTH_TOKEN"));
            conModes.setRequestMethod("GET");
            conModes.setRequestProperty("contentType", "application/json; charset=utf-8");


            BufferedReader inModes = new BufferedReader(
                    new InputStreamReader(conModes.getInputStream()));
            String inputLineModes;
            StringBuilder contentModes = new StringBuilder();
            while ((inputLineModes = inModes.readLine()) != null) {
                contentModes.append(inputLineModes);
            }
            this.log.info("DB Satnogs Modes: " + inputLineModes);
            inModes.close();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        try {
            URL urlSatellites = new URL("https://db-dev.satnogs.org/api/satellites/52017/?format=json");
            HttpURLConnection conSatellites = (HttpURLConnection) urlSatellites.openConnection();
            conSatellites.setRequestProperty("Authorization", "Bearer " + System.getenv("SATNOGSDB_AUTH_TOKEN"));
            conSatellites.setRequestMethod("GET");
            conSatellites.setRequestProperty("contentType", "application/json; charset=utf-8");


            BufferedReader inSatellites = new BufferedReader(
                    new InputStreamReader(conSatellites.getInputStream()));
            String inputLineSatellites;
            StringBuilder contentSatellites = new StringBuilder();
            while ((inputLineSatellites = inSatellites.readLine()) != null) {
                contentSatellites.append(inputLineSatellites);
            }
            this.log.info("DB Satnogs Satellites: " + inputLineSatellites);
            inSatellites.close();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        try {
            URL urlTelemetry = new URL("https://db-dev.satnogs.org/api/telemetry/?satellite=52017");
            HttpURLConnection conTelemetry = (HttpURLConnection) urlTelemetry.openConnection();
            conTelemetry.setRequestProperty("Authorization", "Bearer " + System.getenv("SATNOGSDB_AUTH_TOKEN"));
            conTelemetry.setRequestMethod("GET");
            conTelemetry.setRequestProperty("contentType", "application/json; charset=utf-8");


            BufferedReader inTelemetry = new BufferedReader(
                    new InputStreamReader(conTelemetry.getInputStream()));
            String inputLineTelemetry;
            StringBuilder contentTelemetry = new StringBuilder();
            while ((inputLineTelemetry = inTelemetry.readLine()) != null) {
                contentTelemetry.append(inputLineTelemetry);
            }
            this.log.info("DB Satnogs Telemetry: " + inputLineTelemetry);
            inTelemetry.close();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        try {
            URL urlTle = new URL("https://db-dev.satnogs.org/api/tle/");
            HttpURLConnection conTle = (HttpURLConnection) urlTle.openConnection();
            conTle.setRequestMethod("GET");
            conTle.setRequestProperty("contentType", "application/json; charset=utf-8");


            BufferedReader inTle = new BufferedReader(
                    new InputStreamReader(conTle.getInputStream()));
            String inputLineTle;
            StringBuilder contentTle = new StringBuilder();
            while ((inputLineTle = inTle.readLine()) != null) {
                contentTle.append(inputLineTle);
            }
            this.log.info("DB Satnogs Tle: " + inputLineTle);
            inTle.close();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        try {
            URL urlTransmitters = new URL("https://db-dev.satnogs.org/api/transmitters/VTFA-5315-6080-2422-3537/");
            HttpURLConnection conTransmitters = (HttpURLConnection) urlTransmitters.openConnection();
            conTransmitters.setRequestProperty("Authorization", "Bearer " + System.getenv("SATNOGSDB_AUTH_TOKEN"));
            conTransmitters.setRequestMethod("GET");
            conTransmitters.setRequestProperty("contentType", "application/json; charset=utf-8");


            BufferedReader inTransmitters = new BufferedReader(
                    new InputStreamReader(conTransmitters.getInputStream()));
            String inputLineTransmitters;
            StringBuilder contentTransmitters = new StringBuilder();
            while ((inputLineTransmitters = inTransmitters.readLine()) != null) {
                contentTransmitters.append(inputLineTransmitters);
            }
            this.log.info("DB Satnogs Transmitters: " + inputLineTransmitters);
            inTransmitters.close();
        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        try {
            URL urlOpticalObservations = new URL("https://db-dev.satnogs.org/api/optical-observations/");
            HttpURLConnection conOpticalObservations = (HttpURLConnection) urlOpticalObservations.openConnection();
            conOpticalObservations.setRequestProperty("Authorization", "Bearer " + System.getenv("SATNOGSDB_AUTH_TOKEN"));
            conOpticalObservations.setRequestMethod("GET");
            conOpticalObservations.setRequestProperty("contentType", "application/json; charset=utf-8");


            BufferedReader inOpticalObservations = new BufferedReader(
                    new InputStreamReader(conOpticalObservations.getInputStream()));
            String inputLineOpticalObservations;
            StringBuilder contentOpticalObservations = new StringBuilder();
            while ((inputLineOpticalObservations = inOpticalObservations.readLine()) != null) {
                contentOpticalObservations.append(inputLineOpticalObservations);
            }
            this.log.info("DB Satnogs Optical Observations: " + inputLineOpticalObservations);
            inOpticalObservations.close();


        } catch (IOException e) {
            this.log.error(e.getMessage());
        }
        this.notifyStarted();
    }


    @Override
    protected void doStop() {
        // TODO Auto-generated method stub
        
    }
    
}
