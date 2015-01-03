package com.t28.rxweather.model;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = City.Builder.class)
public class City implements Validatable {
    public static final int NO_ID = -1;

    private final int mId;
    private final String mName;
    private final String mCountryCode;

    private City(Builder builder) {
        mId = builder.mId;
        mName = builder.mName;
        mCountryCode = builder.mCountryCode;
    }

    @Override
    public boolean isValid() {
        if (mId == NO_ID) {
            return false;
        }

        if (TextUtils.isEmpty(mName)) {
            return false;
        }

        if (TextUtils.isEmpty(mCountryCode)) {
            return false;
        }
        return true;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        private int mId = NO_ID;
        private String mName;
        private String mCountryCode;

        public Builder() {
        }

        public void setId(int id) {
            mId = id;
        }

        public void setName(String name) {
            mName = name;
        }

        @JsonProperty("country")
        public void setCountryCode(String code) {
            mCountryCode = code;
        }

        public City build() {
            return new City(this);
        }
    }
}
