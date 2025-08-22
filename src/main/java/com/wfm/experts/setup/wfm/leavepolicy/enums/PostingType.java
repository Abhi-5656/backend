package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PostingType {
    @JsonProperty("Post at start")
    POST_AT_START,
    @JsonProperty("Post at end")
    POST_AT_END
}