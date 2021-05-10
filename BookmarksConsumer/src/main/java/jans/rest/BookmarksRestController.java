package jans.rest;

import jans.KafkaStreamApplication;
import jans.model.Bookmark;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class BookmarksRestController {

    //local store name
    private static final String STORE_NAME = KafkaStreamApplication.STORE_NAME;
    ReadOnlyKeyValueStore<String, Bookmark> keyValueStore;

    @Autowired
    private InteractiveQueryService queryService;

    // rest get call to return all bookmarks stored in local state store for some specific user
    @GetMapping("/bookmarks/{user}")
    public List<Bookmark>  getLocalUserBookmarks(@PathVariable String user) {

        // query the local state store
        keyValueStore = queryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.<String, Bookmark>keyValueStore());
        KeyValueIterator<String, Bookmark> kvi = keyValueStore.all();

        // find all bookmarks for defined user
        List<Bookmark> bookmarks = new ArrayList<>();
        while (kvi.hasNext()){
            KeyValue<String, Bookmark> kv = kvi.next();
            if (kv.value.getUser().matches(user)){
                bookmarks.add(kv.value);
            }
        }
        return bookmarks;
    }

    // rest get call to return one bookmark for some specific key
    @GetMapping("/getOneBookmark/{key}")
    public Bookmark  getOneBookmark(@PathVariable String key) {

        // query the local state store
        keyValueStore = queryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.<String, Bookmark>keyValueStore());
        Bookmark bookmark = keyValueStore.get(key);
        return bookmark;
    }

    // rest get call to return current host of the microservice
    @GetMapping("/currentHost")
    public String showProcessors(Model uiModel) {

        HostInfo currentHostInfo = queryService.getCurrentHostInfo();
        return currentHostInfo.toString();
    }

    // rest get call to return host ip address where some specific key is located
    @GetMapping("/keyHost/{key}")
    public String getHost(@PathVariable String key, Model uiModel) {

        HostInfo hostInfo = queryService.getHostInfo(STORE_NAME, key, Serdes.String().serializer());
        return hostInfo.host()+":"+hostInfo.port();
    }

}
