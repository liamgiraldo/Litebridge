package me.liamgiraldo.litebridge.models;

import java.util.ArrayList;

public class QueueStorage {
    ArrayList<SignModel> queueSigns;
    public QueueStorage(){

    }
    public void addQueueSign(SignModel sign){
        this.queueSigns.add(sign);
    }
    public void removeQueueSign(SignModel sign){
        this.queueSigns.remove(sign);
    }

}
