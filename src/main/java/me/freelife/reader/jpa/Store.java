package me.freelife.reader.jpa;

/**
 * Created by jojoldu@gmail.com on 20/08/2018
 * Blog : http://jojoldu.tistory.com
 * Github : https://github.com/jojoldu
 */

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

    public Store(String name) {
        this.name = name;
    }

    public void addProduct(@NonNull Product product){
        product.changeStore(this);
        this.products.add(product);
    }

    public long sumProductsPrice(){
        return products.stream()
                .mapToLong(Product::getPrice)
                .sum();
    }

}
