package com.example.proyecto1;

public class Repeatable_Upgrade extends Generic_Upgrade{


    public Repeatable_Upgrade(int id, String name, String desc, int kind, int upgrade_target, int status, int req_upg, int archived_upgrades, int img, int price, int upgrade_value, int[] unlocks) {
        super(id, name, desc, kind, upgrade_target, status, req_upg, archived_upgrades, img, price, upgrade_value, unlocks);
    }

    @Override
    public void disable_upgrade(){
        if (this.status == 1){
            this.status = 3;
        }
    }
}
