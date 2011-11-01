package org.openrepose.rnxp.servlet.http.live;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openrepose.rnxp.decoder.partial.HttpMessagePartial;
import org.openrepose.rnxp.http.io.control.HttpMessageSerializer;
import org.openrepose.rnxp.http.io.control.UpdatableHttpMessage;
import org.openrepose.rnxp.http.io.control.HttpConnectionController;
import org.openrepose.rnxp.http.HttpMessageComponent;
import org.openrepose.rnxp.http.HttpMessageComponentOrder;
import org.openrepose.rnxp.servlet.http.ServletInputStream;
import org.openrepose.rnxp.servlet.http.ServletOutputStreamWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public abstract class AbstractUpdatableHttpMessage implements UpdatableHttpMessage {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUpdatableHttpMessage.class);
    private InputStream connectedInputStream;
    private OutputStream connectedOutputStream;
    private HttpConnectionController updateController;
    private HttpMessageComponent lastReadComponent;

    // TODO:Review - Visibility
    protected void setUpdateController(HttpConnectionController updateController) {
        this.updateController = updateController;

        lastReadComponent = HttpMessageComponent.MESSAGE_START;
    }

    public synchronized ServletInputStream getInputStream() {
        if (connectedInputStream == null) {
            connectedInputStream = updateController.connectInputStream();
        }

        return new ServletInputStream(connectedInputStream);
    }

    public synchronized ServletOutputStreamWrapper getOutputStream() throws IOException {
        if (connectedOutputStream == null) {
            connectedOutputStream = updateController.connectOutputStream();
        }
        
        return new ServletOutputStreamWrapper(connectedOutputStream);
    }

    @Override
    public final void applyPartial(HttpMessagePartial partial) {
        lastReadComponent = partial.getHttpMessageComponent();

        mergeWithPartial(partial);
    }

    @Override
    public final void requestUpdate() {
        try {
            updateController.blockingRequestUpdate(this);
        } catch (InterruptedException ie) {
            LOG.error(ie.getMessage(), ie);
        }
    }

    protected void loadComponent(HttpMessageComponent component, HttpMessageComponentOrder order) {
        while (order.isBefore(lastReadComponent(), component)) {
            LOG.info("Requesting more HTTP request data up to " + component + ". Current position: " + lastReadComponent());

            requestUpdate();
        }
    }

    protected HttpMessageComponent lastReadComponent() {
        return lastReadComponent;
    }

    protected abstract void mergeWithPartial(HttpMessagePartial partial);
}
