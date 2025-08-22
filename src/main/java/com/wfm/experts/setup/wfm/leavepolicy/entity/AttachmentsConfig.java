package com.wfm.experts.setup.wfm.leavepolicy.entity;

import com.wfm.experts.setup.wfm.leavepolicy.enums.AllowedFileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "lp_attachments_config")
@Getter
@Setter
public class AttachmentsConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lp_allowed_files", joinColumns = @JoinColumn(name = "attachments_config_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private List<AllowedFileType> allowedFileTypes;
}