package searchengine.dao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "statistic")
@Getter
@Setter
@ToString(exclude = {"site"})
@EqualsAndHashCode(of = "id")
public class Statistic implements BaseEntity<Integer>{

    @Id
    private Integer id;

    @Column(name = "pages")
    private Long countOfPages;

    @Column(name = "lemmas")
    private Long countOfLemmas;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId()
    @JoinColumn(name = "id")
    private Site site;

//    @Column(name = "indexes")
//    private Long countOfIndexes;

    public Statistic(){}

    public Statistic(Long countOfPages, Long countOfLemmas, Site site) {
        this.countOfPages = countOfPages;
        this.countOfLemmas = countOfLemmas;
        this.site = site;
        site.setStatistic(this);
    }
}
