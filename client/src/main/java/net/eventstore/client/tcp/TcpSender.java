package net.eventstore.client.tcp;

import java.io.IOException;
import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import net.eventstore.client.model.RequestOperation;
import net.eventstore.client.model.ResponseOperation;
import net.eventstore.client.util.Bytes;

/**
 * TcpSender class
 * @author Stasys
 */
@Log4j
@RequiredArgsConstructor
public class TcpSender implements Runnable {

    private final OutputStream out;
    private final TcpSocketManager manager;
    
    @Override
    public void run() {
        try {

            // Wait for the manager to lock itself
            while (!manager.getRunning().hasQueuedThreads()) Thread.sleep(10);
            
            while (manager.getRunning().hasQueuedThreads()) {
                
                RequestOperation op = manager.getSending().take();

                boolean waitingResponse = false;
                if (op instanceof ResponseOperation) {
                    ResponseOperation rop = (ResponseOperation) op;
                    manager.getReceiving().put(op.getCorrelationId(), rop);
                    waitingResponse = true;
                }

                TcpPackage pckg = op.getRequestPackage();

                byte[][] frames = TcpFramer.frame(pckg.AsByteArray());

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Sending... %s", Bytes.debugString(frames)));
                }

                // Send package
                for (byte[] b : frames) out.write(b);
                out.flush();

                if (!waitingResponse) {
                    op.doneProcessing();
                }

            }
            
        } catch (InterruptedException ex) {
            // Ignore
        } catch (IOException ex) {
            if (manager.getRunning().hasQueuedThreads()) {
                log.warn("Error in sender", ex);
                manager.getRunning().release();
            }
        }   
    }
    
}
