package ca.qc.hydro.epd.service.wsclient.dto;

public record JolokiaRequest(String type, String mbean, String operation) {
}
