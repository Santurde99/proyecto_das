package com.example.proyecto1;

import com.example.proyecto1.workers.SaveUU_Worker;
import com.example.proyecto1.workers.LoadUpgrades_Worker;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.HashMap;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class Data_Load {

    private static Data_Load the_dataload;
    private final ArrayList<Generic_Upgrade> upgrade_list = new ArrayList<>();

    private Data_Load() {
    }

    public static Data_Load getDL() {
        if (the_dataload == null) {
            the_dataload = new Data_Load();
        }
        return the_dataload;
    }


    public Generic_Upgrade get_upgrade_by_id(int id) {
        for (Generic_Upgrade element : this.upgrade_list) {
            if (element.get_id() == id) {
                return element;
            }
        }
        return null;
    }

    public void load_from_database(Context context, LifecycleOwner lifecycleOwner, String user_upgrades) {
        OneTimeWorkRequest loadUpgradesWorkRequest =
                new OneTimeWorkRequest.Builder(LoadUpgrades_Worker.class)
                        .build();

        WorkManager.getInstance(context).enqueue(loadUpgradesWorkRequest);

        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(loadUpgradesWorkRequest.getId())
                .observe(lifecycleOwner, workInfo -> {
                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        try {
                            Log.i("LoadUPG", "Trabajo completado");
                            // Get the output data from worker
                            Data outputData = workInfo.getOutputData();
                            String upgradesJson = outputData.getString(LoadUpgrades_Worker.KEY_RESULT);

                            // Parse both JSON strings
                            JSONParser parser = new JSONParser();

                            // 1. Parse the user_upgrades JSON array
                            JSONArray userUpgradesArray = (JSONArray) parser.parse(user_upgrades);

                            // 2. Parse the upgrades JSON array
                            JSONArray upgradesDataArray = (JSONArray) parser.parse(upgradesJson);

                            // Create a map of upgrade_id to status from user_upgrades
                            Map<Integer, Integer> userUpgradeStatusMap = new HashMap<>();
                            for (Object obj : userUpgradesArray) {
                                JSONObject userUpgrade = (JSONObject) obj;
                                int upgradeId = Integer.parseInt(userUpgrade.get("upgrade_id").toString());
                                int status = Integer.parseInt(userUpgrade.get("status").toString());
                                userUpgradeStatusMap.put(upgradeId, status);
                            }

                            this.upgrade_list.clear();

                            // Iterate through upgrades data and create upgrade objects
                            for (int i = 0; i < upgradesDataArray.size(); i++) {
                                JSONObject upgradeJson = (JSONObject) upgradesDataArray.get(i);

                                // Extract all fields from JSON
                                int id = Integer.parseInt(upgradeJson.get("id").toString());
                                String name = (String) upgradeJson.get("name");
                                String description = (String) upgradeJson.get("description");
                                int kind = Integer.parseInt(upgradeJson.get("kind").toString());
                                int upgrade_target = Integer.parseInt(upgradeJson.get("upgrade_target").toString());
                                int status = userUpgradeStatusMap.getOrDefault(id, 0);
                                int price = Integer.parseInt(upgradeJson.get("price").toString());
                                int upgrade_value = Integer.parseInt(upgradeJson.get("upgrade_value").toString());
                                String images = (String) upgradeJson.get("images");
                                String unlocks_string = (String) upgradeJson.get("unlocks");
                                int repeatable = Integer.parseInt(upgradeJson.get("repeatable").toString());

                                // Parse unlocks string into an array of ints
                                int[] unlocks = {};
                                if (unlocks_string != null && !unlocks_string.isEmpty()) {
                                    unlocks = Arrays.stream(unlocks_string.split(","))
                                            .mapToInt(Integer::parseInt)
                                            .toArray();
                                }

                                // Get string resources
                                String stringTitleId = context.getString(
                                        context.getResources().getIdentifier(name, "string", context.getPackageName()));
                                String stringDescId = context.getString(
                                        context.getResources().getIdentifier(description, "string", context.getPackageName()));
                                int imageResId = context.getResources().getIdentifier(
                                        images, "drawable", context.getPackageName());

                                // Create the appropriate upgrade based on kind
                                if (kind == 1) {
                                    this.upgrade_list.add(new Repeatable_Upgrade(
                                            id, stringTitleId, stringDescId, kind, upgrade_target,
                                            status, imageResId, price, upgrade_value, unlocks));
                                } else {
                                    this.upgrade_list.add(new Generic_Upgrade(
                                            id, stringTitleId, stringDescId, kind, upgrade_target,
                                            status, imageResId, price, upgrade_value, unlocks));
                                }
                            }

                            Log.d("UpgradeLoad", "Successfully loaded " + this.upgrade_list.size() + " upgrades");

                        } catch (Exception e) {
                            Log.e("UpgradeLoad", "Error processing upgrades data", e);
                        }
                    } else if (workInfo != null && workInfo.getState() == WorkInfo.State.FAILED) {
                        Log.e("UpgradeLoad", "Failed to load upgrades from server");
                    }
                });
    }

    public void save_upgrades(Context context, LifecycleOwner lifecycleOwner, String username) {
        try {
            // Convertir upgrade_list a JSON
            JSONArray upgradesArray = new JSONArray();
            for (Generic_Upgrade upgrade : upgrade_list) {
                JSONObject upgradeJson = new JSONObject();
                upgradeJson.put("upgrade_id", upgrade.get_id());
                upgradeJson.put("status", upgrade.get_status());
                upgradesArray.add(upgradeJson);
            }

            // Crear el trabajo para guardar en el servidor
            Data inputData = new Data.Builder()
                    .putString(SaveUU_Worker.KEY_USERNAME, username)
                    .putString(SaveUU_Worker.KEY_UPGRADES_JSON, upgradesArray.toString())
                    .build();

            OneTimeWorkRequest saveWorkRequest =
                    new OneTimeWorkRequest.Builder(SaveUU_Worker.class)
                            .setInputData(inputData)
                            .build();

            WorkManager.getInstance(context).enqueue(saveWorkRequest);

            // Opcional: Observar el resultado
            WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(saveWorkRequest.getId())
                    .observe(lifecycleOwner, workInfo -> {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Log.d("SaveUpgrades", "Upgrades sincronizados exitosamente con el servidor");
                        } else if (workInfo != null && workInfo.getState() == WorkInfo.State.FAILED) {
                            Log.e("SaveUpgrades", "Error al sincronizar upgrades con el servidor");
                        }
                    });

        } catch (Exception e) {
            Log.e("SaveUpgrades", "Error al preparar datos para el servidor", e);
        }
    }

    public ArrayList<Generic_Upgrade> get_upgrade_list(){
        return upgrade_list;
    }
}
