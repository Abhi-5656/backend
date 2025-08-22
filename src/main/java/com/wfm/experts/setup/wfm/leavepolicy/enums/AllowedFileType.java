package com.wfm.experts.setup.wfm.leavepolicy.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AllowedFileType {
    @JsonProperty("pdf")
    PDF,
    @JsonProperty("jpg")
    JPG,
    @JsonProperty("png")
    PNG,
    @JsonProperty("docx")
    DOCX
}