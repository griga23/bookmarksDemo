package jans.web;

import jans.model.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class BookmarksClientController {

    @Autowired
    private StreamBridge streamBridge;

    // show bookmark input dialog for some user
    @RequestMapping(value = "/bookmarksProducer/{user}", method = RequestMethod.GET)
    public String showBookmarks(@PathVariable String user, Model uiModel) {
        uiModel.addAttribute("newBookmark", new Bookmark(user));
        return "bookmarksProducer";
    }

    // process the entered bookmark entry for some user
    @RequestMapping(value = "/saveBookmark", method = RequestMethod.POST)
    public String saveBookmark(@ModelAttribute Bookmark bookmark, BindingResult errors, Model model) {

        bookmark.setAdded(new Date());
        if (bookmark.getUrl()==null){
            bookmark.setAction("DELETE");
        }else{
            bookmark.setAction("SAVE");
        }

        // create the message JSON payload
        // use customer name for the message key
        final Message<Bookmark> message = MessageBuilder
                .withPayload(bookmark)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader(KafkaHeaders.MESSAGE_KEY, bookmark.getUser().getBytes())
                .build();

        // send the message to Kafka topic defined as bindings.output.destination
        streamBridge.send("output-out-0", message);

        model.addAttribute("name", bookmark.getName());
        model.addAttribute("user", bookmark.getUser());
        return "bookmarkSaved";
    }

}
