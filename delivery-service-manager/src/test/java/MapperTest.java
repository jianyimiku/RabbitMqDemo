import com.example.delivery.DeliveryServiceManagerApplication;
import com.example.delivery.constant.DeliveryManStatus;
import com.example.delivery.dao.DeliverymanMapper;
import com.example.delivery.pojo.Deliveryman;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author ：sjq
 * @date ：Created in 2021/10/23 下午4:38
 * @description：
 * @modified By：
 * @version: $
 */
@SpringBootTest(classes = DeliveryServiceManagerApplication.class)
public class MapperTest {

    @Autowired
    private DeliverymanMapper deliverymanMapper;

    @Test
    public void selectDeliverMan(){
        List<Deliveryman> deliverymanList
                = deliverymanMapper.selectDeliveryManByStatus(DeliveryManStatus.AVAILABLE);
        System.out.println(deliverymanList);
    }
}
