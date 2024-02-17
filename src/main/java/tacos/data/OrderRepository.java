package tacos.data;

import org.springframework.core.annotation.Order;
import org.springframework.data.repository.CrudRepository;
import tacos.Taco;
import tacos.TacoOrder;

public interface OrderRepository extends CrudRepository<TacoOrder, Long> {



}
