package com.billermanagement.consumer;

import com.billermanagement.services.handler.HandlerConstant;
import com.billermanagement.services.handler.IRS;
import com.billermanagement.services.handler.Instamoney;
import com.billermanagement.services.handler.Xfers;
import com.billermanagement.util.InitDB;
import com.billermanagement.vo.InstamoneyVO.Callback.IMCallbackReqVO;
import com.billermanagement.vo.InstamoneyVO.Callback.IMInquiryCallbackReqVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class CallbackRabbitConsumer implements ChannelAwareMessageListener {

    private Logger logger = LoggerFactory.getLogger(CallbackRabbitConsumer.class);

    @Autowired
    private Xfers xfers;

    @Autowired
    private IRS irs;

    @Autowired
    private Instamoney im;


    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            logger.info("Callback Message received :"+payload);
            logger.info("deliveryTag :"+message.getMessageProperties().getDeliveryTag());
            // do something with the message
            Map<String,String> map = mapper.readValue(payload, Map.class);
            for (Map.Entry<String,String> map1 : map.entrySet()){
                if (map1.getKey().equals(String.valueOf(HandlerConstant.XF))){
                    xfers.processCallback(map1.getValue());
                }else if (map1.getKey().equals(String.valueOf(HandlerConstant.IRS))){
                    irs.callback(map1.getValue());
                }else if (map1.getKey().equals(String.valueOf(HandlerConstant.IM))){
                    ObjectMapper mapper1 = new ObjectMapper();
                    IMCallbackReqVO vo= mapper1.convertValue(map1.getValue(),IMCallbackReqVO.class);
                    im.paymentCallback(vo);
                }else if (map1.getKey().equals(String.valueOf(HandlerConstant.IMINQ))){
                    ObjectMapper mapper1 = new ObjectMapper();
                    IMInquiryCallbackReqVO vo= mapper1.convertValue(map1.getValue(),IMInquiryCallbackReqVO.class);
                    im.inpuiryCallback(vo);
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            logger.error("CallbackRabbitConsumer Error :"+e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
        }
    }
}
