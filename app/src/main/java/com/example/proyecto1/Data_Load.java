package com.example.proyecto1;

import java.util.ArrayList;

public class Data_Load {

    private  static Data_Load the_dataload;
    private final ArrayList<Generic_Upgrade> upgrade_list = new ArrayList<>();

    private Data_Load(){
        generateUpgrades();
    }

    public static Data_Load getDL() {
        if (the_dataload == null) {
            the_dataload = new Data_Load();
        }
        return the_dataload;
    }

    public Generic_Upgrade get_upgrade_by_id(int id){
        for (Generic_Upgrade element : this.upgrade_list) {
            if (element.get_id() == id) {
                return element;
            }
        }
        return null;
    }

    private void generateUpgrades(){
        //KIND -> 0 = valor fijo, 1 = porcentual
        //UPGRADE_TARGET -> 0 = pasivo, 1 = click, n = mejora concreta
        this.upgrade_list.add(new Generic_Upgrade(1,"Pico de madera","Gana +5 pepitas por toque",0,1,1,0,R.drawable.placeholder,10,5,new int[]{2}));
        this.upgrade_list.add(new Generic_Upgrade(2,"Pico de piedra","Gana +8 pepitas por segundo",0,0,0,1,R.drawable.placeholder,8,10,new int[]{3}));
        this.upgrade_list.add(new Generic_Upgrade(3,"Taladro","Gana 50% más de pepitas por segundo",1,0,0,1,R.drawable.placeholder,30,50,new int[]{}));

    }

    public ArrayList<Generic_Upgrade> get_upgrade_list(){
        return upgrade_list;
    }
}
