package com.bigsoftware.jh_simple_pos.data;

/**
 * Created by shanesepac on 4/15/19.
 */

public class StoreConfig {

    public static class StoreManager{

        //TODO: Ask for other config settings JH would like
        private static String storeName;
        private static String notes;

        private static StoreManager instance;

        private StoreManager(){}

        public static StoreManager getInstance(){
            if(instance==null){
                try{
                    init();
                }
                catch(StoreConfigException e){
                    e.printStackTrace();
                }
            }

            return instance;
        }

        private static void init() throws StoreConfigException{
            //TODO: Initialize singleton
            instance = new StoreManager();
        }

    }

    class StoreConfigException extends Exception{
        //TODO: Add any config exception messages
    }

}
