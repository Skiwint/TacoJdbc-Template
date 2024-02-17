package tacos.data;

import org.springframework.asm.Type;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tacos.IngredientRef;
import tacos.Taco;
import tacos.TacoOrder;

import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Repository
public class JdbcOrderRepository implements OrderRepository{

    private final JdbcOperations jdbcOperationsl;

    public JdbcOrderRepository(JdbcOperations jdbcOperationsl) {
        this.jdbcOperationsl = jdbcOperationsl;
    }



    @Override
    @Transactional
    public TacoOrder save(TacoOrder order) {
        String sql = "INSERT INTO TacoOrder" +
                "(delivery_Name, delivery_Street, delivery_City, " +
                "delivery_State, delivery_Zip, cc_number, "+
                "cc_expiration, cc_cvv, placed_at)" +
                "VALUES(?,?,?,?,?,?,?,?,?)";

        PreparedStatementCreatorFactory pscf =
                new PreparedStatementCreatorFactory(
                        sql, Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.TIMESTAMP
                );

        pscf.setReturnGeneratedKeys(true);
        order.setPlacedAt(new Date());

        PreparedStatementCreator psc = pscf.newPreparedStatementCreator(
                Arrays.asList(
                        order.getDeliveryName(),
                        order.getDeliveryStreet(),
                        order.getDeliveryCity(),
                        order.getDeliveryState(),
                        order.getDeliveryZip(),
                        order.getCcNumber(),
                        order.getCcExpiration(),
                        order.getCcCVV(),
                        order.getPlacedAt()));
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperationsl.update(psc, keyHolder);
        long orderId=keyHolder.getKey().longValue();
        order.setId(orderId);

        List<Taco> tacos=order.getTacos();
        int i=0;
        for(Taco taco:tacos){
            saveTaco(orderId, i++, taco);
        }
        return order;
    }

    private long saveTaco(Long orderId, int orderKey, Taco taco) {
        taco.setCreateAt(new Date());
        PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(
                "INSERT INTO Taco (name, created_at, taco_order, taco_order_key) "+
                        "values (?,?,?,?)",
                Types.VARCHAR, Types.TIMESTAMP, Type.LONG, Type.LONG
        );

        pscf.setReturnGeneratedKeys(true);

        PreparedStatementCreator psc =
                pscf.newPreparedStatementCreator(
                        Arrays.asList(
                                taco.getName(),
                                taco.getCreateAt(),
                                orderId,
                                orderKey
                        ));

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperationsl.update(psc, keyHolder);
        long tacoId = keyHolder.getKey().longValue();
        taco.setId(tacoId);
        saveIngredientRefs(tacoId, taco.getIngredients());

        return orderId;
    }

    private void saveIngredientRefs(long tacoId, List<IngredientRef> ingredientRefs) {
        int key=0;
        for(IngredientRef ingredientRef: ingredientRefs){
            jdbcOperationsl.update(
                    "INSERT INTO Ingredient_Ref (ingredient, taco, taco_key) "
                    +"values (?,?,?)",
                    ingredientRef.getIngredient(), tacoId, key++);
        }
    }
}
