package com.zt;

import com.zt.service.IShopService;
import com.zt.service.impl.ShopServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SpringTestApplications {


    @Autowired
    private ShopServiceImpl shopService;


    @Test
    public void syncShopData2Redis() {
        for (int i = 1; i < 14; i++) {
            shopService.saveData2Redis((long) i, 1800);
        }
    }


}
