package searchengine.dao.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lemma")
@Getter
@Setter
@ToString(exclude = {"site","indexes"})
@EqualsAndHashCode(of = {"lemma","site"})
public class Lemma implements BaseEntity<Integer>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private Integer frequency;

    @JsonBackReference("site_lemma")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @JsonManagedReference("lemma_index")
    @OneToMany(mappedBy = "lemma", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Index> indexes = new ArrayList<>();

    public Lemma(){}

    public Lemma(String lemma, Site site) {
        this.lemma = lemma;
        this.site = site;
        this.frequency = 1;
    }

    public Lemma(String lemma, Integer frequency, Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
        site.getLemmas().add(this);
    }
}
