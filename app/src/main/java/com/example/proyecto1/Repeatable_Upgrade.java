package com.example.proyecto1;

public class Repeatable_Upgrade extends Generic_Upgrade{

    private int previous_price;
    public Repeatable_Upgrade(int id, String name, String desc, int kind, int upgrade_target, int status, int req_upg, int archived_upgrades, int img, int price, int upgrade_value, int[] unlocks) {
        super(id, name, desc, kind, upgrade_target, status, req_upg, archived_upgrades, img, price, upgrade_value, unlocks);
    }

    @Override
    public void disable_upgrade(){
        if (this.status == 1){
            this.status = 1;
            this.previous_price = this.price;
            int increase = (int) Math.ceil(this.price * 8 / 100.0);
            this.price = this.price + increase;
        }
    }

    public int get_previous_price(){
        return previous_price;
    }


}
