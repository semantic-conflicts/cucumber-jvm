package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

/**
 * This formatter listens for Cucumber Messages emitted by core and writes them to a URL (file, http or https)
 */
public final class MessageFormatter implements EventListener {
    private final Writer writer;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
        .omittingInsignificantWhitespace();

    public MessageFormatter(URL url) throws IOException {
        this.writer = IO.openWriter(url);
    }

    /**
     * Constructor used in unit test
     */
    public MessageFormatter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, this::writeEnvelope);
    }

    private void writeEnvelope(Envelope envelope) {
        try {
            String json = jsonPrinter.print(envelope);
            writer.write(json);
            writer.write("\n");
            writer.flush();
            if (envelope.hasTestRunFinished()) {
                writer.close();
            }
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

}

