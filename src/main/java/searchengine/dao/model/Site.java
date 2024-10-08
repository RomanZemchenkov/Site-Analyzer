package searchengine.dao.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "site")
@Getter
@Setter
@ToString(exclude = {"pages", "lemmas", "statistic"})
@EqualsAndHashCode(of = {"url", "name"})
@NamedEntityGraph(
        name = "Site.withAllPages",
        attributeNodes =
                {
                        @NamedAttributeNode(
                                value = "pages")
                }
)
public class Site implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "status_time")
    private OffsetDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url")
    private String url;

    @Column(name = "name")
    private String name;

    @OneToOne(mappedBy = "site",fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private Statistic statistic;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Page> pages = new ArrayList<>();

    @JsonManagedReference("site_lemma")
    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private List<Lemma> lemmas = new ArrayList<>();

    public Site() {
    }

    public Site(Status status, OffsetDateTime statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }
}
