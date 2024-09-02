package searchengine.dao.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "page")
@Getter
@Setter
@ToString(exclude = "site")
@EqualsAndHashCode(of = {"id","path"})
public class Page implements BaseEntity<Integer>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "path")
    private String path;

    @Column(name = "code")
    private Integer code;

    @Column(name = "content")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    public Page(){}

    public Page(String path, Integer code, String content, Site site) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site = site;
        site.getPages().add(this);
    }
}
