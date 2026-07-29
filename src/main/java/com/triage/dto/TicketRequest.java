package com.triage.dto;
import java.util.UUID;

public class TicketRequest {
    private UUID customerId;
    private String channel;
    private String text;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
