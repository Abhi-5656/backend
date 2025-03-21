package com.wfm.experts.tenancy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RequestWrapper extends HttpServletRequestWrapper {

    private final String modifiedUri;

    public RequestWrapper(HttpServletRequest request, String modifiedUri) {
        super(request);
        this.modifiedUri = modifiedUri;
    }

    @Override
    public String getRequestURI() {
        return this.modifiedUri;  // Return the modified URI without tenantId
    }
}
