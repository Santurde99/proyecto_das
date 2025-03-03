package com.example.proyecto1;

public class Fixed_Value_Upgrade extends Generic_Upgrade {


    private int upgrade_value;

    public Fixed_Value_Upgrade(int id, String name, String desc, int status, int req_upg, int img, int price, int upgrade_value, int[] unlocks) {
        super(id, name, desc, status, req_upg, img, price, upgrade_value, unlocks);
    }


    public int get_upgrade_value(){
        return this.upgrade_value;
    }
}
