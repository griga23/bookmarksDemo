package jans.service;

import jans.model.Bookmark;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
public class BookmarksService {
    private final BookmarksStreams bookmarksStreams;

    public BookmarksService(BookmarksStreams bookmarksStreams) {
        this.bookmarksStreams = bookmarksStreams;
    }

    // send to kafka with user as a key
    public void sendBookmark(final Bookmark bookmark) {

        MessageChannel messageChannel = bookmarksStreams.outboundBookmarks();
        messageChannel.send(MessageBuilder
                .withPayload(bookmark)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader(KafkaHeaders.MESSAGE_KEY, bookmark.getUser().getBytes())
                .build());
    }
}
