package com.wfm.experts.setup.wfm.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup/wfm")
@PreAuthorize("hasAuthority('wfm:setup:manage')")
public abstract class WfmSetupController {
}