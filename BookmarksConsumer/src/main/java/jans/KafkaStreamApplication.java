package jans;

import jans.model.Bookmark;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.function.Consumer;

@SpringBootApplication
public class KafkaStreamApplication {

	// name of my local store
	// this name is also included in topic names bookmarks-store-repartition, bookmarks-store-changelog
    public static String STORE_NAME = "store";

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamApplication.class, args);
    }

    public static class BookmarksStream {

        @Bean
        public Consumer<KStream<String, Bookmark>> reduce() {

            return input -> input
					// select new unique key for the kTable (username+bookmarkName)
                    .selectKey((key, value) -> value.getUser().concat(value.getName()))
                    // groupBy is need before aggregation can be executed
                    .groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Bookmark.class)))
                    // aggregate current value and new value of the bookmark object
					.reduce(
                            (aggValue, newValue) -> {
                            	// check if action is SAVE or DELETE
                                if (newValue.getAction().matches("SAVE"))
                                    //check if URL was modified
                                    if (!aggValue.equalsURL(newValue)) {
                                        //save new or modified bookmark in state store
                                        return newValue;
                                    }
                                    //URL not modified return current bookmark
                                    else return aggValue;
                                // action is DELETE remove from state store
                                else return null;
                            },
                            Materialized.as(STORE_NAME)
                    )
                    .toStream();
        }

    }

}
