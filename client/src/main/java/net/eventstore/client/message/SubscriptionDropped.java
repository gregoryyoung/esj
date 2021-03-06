package net.eventstore.client.message;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import net.eventstore.client.model.ParseException;
import net.eventstore.client.model.Message;
import net.eventstore.client.tcp.TcpCommand;
import net.eventstore.client.message.ClientMessageDtos.SubscriptionDropped.SubscriptionDropReason;

/**
 * WriteEventsCompleted
 *
 * @author Stasys
 */
@Getter
public class SubscriptionDropped extends Message {

    private SubscriptionDropReason dropReason;

    public SubscriptionDropped() {
        super(TcpCommand.SubscriptionDropped);
    }

    @Override
    public void parse(byte[] data) throws ParseException {
        try {
            ClientMessageDtos.SubscriptionDropped dto = ClientMessageDtos.SubscriptionDropped.parseFrom(data);
            dropReason = dto.getReason();
        } catch (InvalidProtocolBufferException ex) {
            throw new ParseException(ex);
        }
    }

    private String getReason() {
        String reason = "";
        switch (dropReason) {
            case Unsubscribed:
                reason = "Unsubscribed";
                break;
            case AccessDenied:
                reason = "Access denied";
                break;
            default:
                reason = "Unknown";
                break;
        }
        return reason;
    }

    protected String toResultInfo() {
        return String.format("Reason = %s.", getReason());
    }

}
