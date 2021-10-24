import com.example.orderservice.OrderServiceManagerApplication;
import com.example.orderservice.dao.DeliverymanMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/22 上午11:48
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootTest(classes = OrderServiceManagerApplication.class)
public class MapperTest {
    @Autowired
    private DeliverymanMapper deliverymanMapper;

    @Test
    public void selectDelivery(){
        System.out.println(deliverymanMapper.selectAll());
    }
}
