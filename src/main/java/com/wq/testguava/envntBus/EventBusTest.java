package com.wq.testguava.envntBus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author 万强
 * @version 1.0
 * @date 2020/8/20 09:55
 * @desc
 */
@Slf4j
public class EventBusTest {

    @Test
    public void test(){
        EventBus eventBus = new EventBus("Joker");
        eventBus.register(new EventListener());
        eventBus.post(new Event1("Hello all listeners, this is event1..."));
        eventBus.post(new Event2("Hello all listeners, this is event2..."));
    }

    public static class Event{}

    @Data
    public static class Event1 extends Event{
        private String message;

        Event1(String message){
            this.message = message;
        }
    }

    @Data
    public static class Event2 extends Event{
        private String message;

        Event2(String message){
            this.message = message;
        }
    }

    public static class EventListener{
        /**
         * 监听方法，必须标注{@link Subscribe}，且方法只能有1个参数，当有事件发布时会根据参数类型触发方法
         * @param event1
         */
        @Subscribe
        public void listen(Event1 event1){
            log.info("监听到【Event1】事件，event = {}", event1);
        }

        @Subscribe
        public void listen(Event2 event2){
            log.info("监听到【Event2】事件，event = {}", event2);
        }

        @Subscribe
//        @AllowConcurrentEvents
        public void listen(Event event){
            log.info("监听到【Event】事件，event = {}", event);
        }
    }

}
 
