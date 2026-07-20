package hestia.otc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class MTCustomerPersistenceTest {

    @Test
    public void mixin_only1() {
        List<MonitoredTarget> readonly = new ArrayList<>();
        readonly.add(site("a", "H", true));
        List<MonitoredTarget> customer = new ArrayList<>();
  
        readonly = MTCustomerPersistence.mixin(readonly, customer);
        
        Assert.assertEquals("a:H", toString(readonly));
    }

    @Test
    public void mixin_only2() {
        List<MonitoredTarget> readonly = new ArrayList<>();
        readonly.add(site("b", "H", true));
        List<MonitoredTarget> customer = new ArrayList<>();
        customer.add(site("a", "C", true));
  
        readonly = MTCustomerPersistence.mixin(readonly, customer);
        
        Assert.assertEquals("a:C,b:H", toString(readonly));
    }

    @Test
    public void mixin_replace() {
        List<MonitoredTarget> readonly = new ArrayList<>();
        readonly.add(site("a", "H", true));
        List<MonitoredTarget> customer = new ArrayList<>();
        customer.add(site("a", "C", true));
  
        readonly = MTCustomerPersistence.mixin(readonly, customer);
        
        Assert.assertEquals("a:C", toString(readonly));
    }

    @Test
    public void mixin_delete() {
        List<MonitoredTarget> readonly = new ArrayList<>();
        readonly.add(site("a", "H", true));
        List<MonitoredTarget> customer = new ArrayList<>();
        customer.add(site("a", "H", false));
  
        readonly = MTCustomerPersistence.mixin(readonly, customer);
        
        Assert.assertEquals("", toString(readonly));
    }

    @Test
    public void mixin_deleted() {
        List<MonitoredTarget> readonly = new ArrayList<>();
        readonly.add(site("a", "H", false));
        List<MonitoredTarget> customer = new ArrayList<>();
  
        readonly = MTCustomerPersistence.mixin(readonly, customer);
        
        Assert.assertEquals("a:H", toString(readonly));
    }

    private Site site(String id, String name, boolean active) {
        Site mt = new Site();
        mt.setId(id);
        mt.setName(name);
        mt.setActive(active);
        return mt;
    }
    
    private String toString(List<MonitoredTarget> list) {
        return list.stream().map(i -> i.getId() + ":" + i.getName()).collect(Collectors.joining(","));
    }
}
