package jans.service;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface BookmarksStreams {
    String OUTPUT = "output";

    @Output(OUTPUT)
    MessageChannel outboundBookmarks();
}
