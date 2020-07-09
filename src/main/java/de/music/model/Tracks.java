package de.music.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class Tracks {
    private String ISRC;
    private String trackName;
    private String artistName;
    private Long units;
    private Float amount;
    private String currency;
}
