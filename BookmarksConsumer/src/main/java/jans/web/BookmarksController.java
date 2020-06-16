package jans.web;

import jans.KafkaStreamApplication;
import jans.model.Bookmark;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Controller
public class BookmarksController {

    private static final String STORE_NAME = KafkaStreamApplication.STORE_NAME;

    @Autowired
    private InteractiveQueryService queryService;

    ReadOnlyKeyValueStore<String, Bookmark> keyValueStore;

    // query the local state store and return bookmarks for some user
    @RequestMapping(value = "/bookmarksConsumer/{user}", method = RequestMethod.GET)
    public String showBookmarks(@PathVariable String user, Model uiModel) {

        // query the local state store
        keyValueStore = queryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.<String, Bookmark>keyValueStore());
        KeyValueIterator<String, Bookmark> kvi = keyValueStore.all();

        // find all bookmarks for defined user
        List<Bookmark> bookmarks = new LinkedList<>();
        while (kvi.hasNext()){
            KeyValue<String, Bookmark> kv = kvi.next();
            if (kv.value.getUser().matches(user)){
                bookmarks.add(kv.value);
            }
        }
        // add them to UI model
        uiModel.addAttribute("currentHostInfo", queryService.getCurrentHostInfo());
        uiModel.addAttribute("bookmarks", bookmarks);
        return "bookmarksConsumer";
    }

    // query all state store and return bookmarks for some user
    @RequestMapping(value = "/bookmarksConsumerAll/{user}", method = RequestMethod.GET)
    public String showAllBookmarks(@PathVariable String user, Model uiModel) {

        //get all remote state stores
        List<HostInfo> hostInfoList = new LinkedList<>();
        hostInfoList = queryService.getAllHostsInfo(STORE_NAME);

        RestTemplate restTemplate = new RestTemplate();
        List<Bookmark> bookmarks = new ArrayList<>();

        for (HostInfo host:hostInfoList) {
            ResponseEntity<Bookmark[]> response =
                    restTemplate.getForEntity(
                            "http://"+host.host()+":"+host.port()+"/bookmarks/"+user,
                            Bookmark[].class);
            Bookmark[] bookmarksArray = response.getBody();
            bookmarks.addAll( Arrays.asList(bookmarksArray));
        }

        // add them to UI model
        uiModel.addAttribute("currentHostInfo", queryService.getCurrentHostInfo());
        uiModel.addAttribute("bookmarks", bookmarks);
        return "bookmarksConsumer";
    }

    @RequestMapping(value = "/processors", method = RequestMethod.GET)
    public String showProcessors( Model uiModel) {

        List<HostInfo> hostInfoList = queryService.getAllHostsInfo(STORE_NAME);
        HostInfo currentHostInfo = queryService.getCurrentHostInfo();

        uiModel.addAttribute("hostInfoList", hostInfoList);
        uiModel.addAttribute("currentHostInfo", currentHostInfo);

        return "processors";
    }


}
