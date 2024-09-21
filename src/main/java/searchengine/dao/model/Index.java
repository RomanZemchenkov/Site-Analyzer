package searchengine.dao.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "index")
@Getter
@Setter
@ToString(exclude = {"page", "lemma"})
@EqualsAndHashCode(of = "id")
@NamedEntityGraph(
        name = "Index.withLemma",
        attributeNodes = {@NamedAttributeNode(value = "lemma")}
)

public class Index implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

    @JsonBackReference("lemma_index")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;

    @Column(name = "rank")
    private Float rank;

    public Index() {
    }

    public Index(Page page, Lemma lemma, Float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
        page.getIndexes().add(this);
        lemma.getIndexes().add(this);
    }
}
