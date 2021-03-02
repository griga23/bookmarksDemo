package jans;

import jans.model.Bookmark;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
public class KafkaStreamApplication {

	public static String STORE_NAME="store";

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamApplication.class, args);
	}

	public static class BookmarksStream {

		@Bean
		public Consumer<KStream<String, Bookmark>> reduce(){

			return input -> input
					.selectKey((key, value) -> value.getUser().concat(value.getName()))
					.groupByKey(Grouped.with(Serdes.String(), new JsonSerde<>(Bookmark.class)))
					.reduce(
							(aggValue, newValue)-> {
								if (newValue.getAction().matches("SAVE"))
									if (!newValue.getUrl().equals(aggValue.getUrl())){
										//return new bookmark
										return newValue;
									}
									//return current bookmark
									else return aggValue;
								// delete from state store
								else return null;
							},
							Materialized.as(STORE_NAME)
					)
			.toStream();
		}

	}

}
