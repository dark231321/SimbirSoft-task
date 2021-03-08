package danil.teterin.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "url")
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Url extends AbstractEntity {

    @Column(name = "path")
    private String urlPath;

    @OneToMany(mappedBy = "url",fetch = FetchType.EAGER,
            cascade = CascadeType.MERGE, orphanRemoval = true)
    private Collection<Word> words = new ArrayList<>();

}
