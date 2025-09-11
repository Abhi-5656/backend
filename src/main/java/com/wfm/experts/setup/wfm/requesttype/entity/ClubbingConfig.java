package com.wfm.experts.setup.wfm.requesttype.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "clubbing_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClubbingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean enabled;

    @ElementCollection
    @CollectionTable(name = "clubbing_with", joinColumns = @JoinColumn(name = "clubbing_id"))
    @Column(name = "with_code")
    private List<String> with;

    private boolean weekends;
    private boolean hols;
    private int max;
}
