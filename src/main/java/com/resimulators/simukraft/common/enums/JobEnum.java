package com.resimulators.simukraft.common.enums;


public enum JobEnum {

    UNEMPLOYED("Unemployed",1),
    BUILDER("Builder",2),
    MINER("Miner", 3);


    public String name;
    public int id;
    JobEnum(String string, int index){
        name = string;
        id = index;
    }

    public static JobEnum getEnumById(int id){
        for (JobEnum enums: JobEnum.values()){
            if (enums.id == id){
                return enums;

            }

        }
        return null;
    }


    }