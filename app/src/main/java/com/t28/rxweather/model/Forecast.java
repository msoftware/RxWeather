package com.t28.rxweather.model;

import com.android.volley.Request;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.t28.rxweather.request.ForecastRequest;
import com.t28.rxweather.util.CollectionUtils;
import com.t28.rxweather.volley.RxSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

@JsonDeserialize(builder = Forecast.Builder.class)
public class Forecast implements Validatable {
    private final City mCity;
    private final List<Weather> mWeathers;

    private Forecast(Builder builder) {
        mCity = builder.mCity;
        if (CollectionUtils.isEmpty(builder.mWeathers)) {
            mWeathers = Collections.emptyList();
        } else {
            mWeathers = new ArrayList<>(builder.mWeathers);
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public static Observable<Forecast> findByName(RxSupport support, String name) {
        final ForecastRequest request = new ForecastRequest.Builder("")
                .setCityName(name)
                .build();
        return support.createObservableRequest(request);
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        private City mCity;
        private List<Weather> mWeathers;

        public Builder() {
        }

        public Builder setCity(City city) {
            mCity = city;
            return this;
        }

        @JsonProperty("list")
        @JsonDeserialize(using = WeatherListDeserializer.class)
        public Builder setWeathers(List<Weather> weathers) {
            mWeathers = weathers;
            return this;
        }

        public Forecast build() {
            return new Forecast(this);
        }
    }

    public static class WeatherListDeserializer extends JsonDeserializer<List<Weather>> {
        private static final TypeReference TYPE_REFERENCE = new WeatherListTypeReference();

        @Override
        public List<Weather> deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            final ObjectCodec codec = parser.getCodec();
            final Iterator<Weather> iterators = codec.readValues(parser, TYPE_REFERENCE);
            return CollectionUtils.newList(iterators);
        }

        private static class WeatherListTypeReference extends TypeReference<List<Weather>> {
        }
    }
}