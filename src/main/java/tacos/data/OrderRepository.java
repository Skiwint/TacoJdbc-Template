package tacos.data;

import tacos.Taco;
import tacos.TacoOrder;

public interface OrderRepository    {

    TacoOrder save(TacoOrder order);

}
