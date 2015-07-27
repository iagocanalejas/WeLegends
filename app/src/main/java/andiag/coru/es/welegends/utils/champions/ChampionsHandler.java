package andiag.coru.es.welegends.utils.champions;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import andiag.coru.es.welegends.DTOs.championsDTOs.ChampionDto;
import andiag.coru.es.welegends.DTOs.championsDTOs.ChampionListDto;

/**
 * Created by Iago on 25/07/2015.
 */
public abstract class ChampionsHandler {
    private static final String HISTORY_FILE_NAME = "ChampionsData";
    private static ChampionListDto champions;
    private static SharedPreferences settings;

    private static ContextWrapper cw;
    private static File directory;

    public static ChampionListDto getChampions() {
        return champions;
    }

    public static void setChampions(ChampionListDto c, Activity activity) throws JSONException {
        if (c != null) {
            champions = c;
            saveChampionsInFile(activity);
            return;
        }
        retrieveChampionsFromFile(activity);
    }

    private static void retrieveChampionsFromFile(Activity activity) throws JSONException {
        HashMap<Integer, ChampionDto> hash = new HashMap<>();
        champions = new ChampionListDto();
        settings = activity.getSharedPreferences(HISTORY_FILE_NAME, 0);

        Map<String, String> map = (Map<String, String>) settings.getAll();

        if (map == null) {
            return;
        }

        Set<String> keys = map.keySet();

        champions.setType(map.get("type"));
        champions.setVersion(map.get("version"));

        ChampionDto championDto;
        JSONObject jsonObject;
        for (String s : keys) {
            if (s.equals("type") || s.equals("version")) {
                continue;
            }
            jsonObject = new JSONObject(map.get(s));
            championDto = new ChampionDto();

            championDto.setId(jsonObject.getInt("id"));
            championDto.setName(jsonObject.getString("name"));
            championDto.setKey(jsonObject.getString("key"));
            championDto.setTitle(jsonObject.getString("title"));

            hash.put(Integer.valueOf(s), championDto);
        }

        champions.setData(hash);
    }

    private static void saveChampionsInFile(Activity activity) throws JSONException {
        settings = activity.getSharedPreferences(HISTORY_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.clear();
        editor.apply();

        editor.putString("type", champions.getType());
        editor.putString("version", champions.getVersion());

        ChampionDto championDto;
        JSONObject jsonObject;
        for (Integer i : champions.getData().keySet()) {
            championDto = champions.getData().get(i);
            jsonObject = new JSONObject();

            jsonObject.put("id", championDto.getId());
            jsonObject.put("name", championDto.getName());
            jsonObject.put("key", championDto.getKey());
            jsonObject.put("title", championDto.getTitle());

            editor.putString(Integer.toString(i), jsonObject.toString());
        }
        editor.apply();

    }

    public static String getServerVersion(Activity activity) {
        settings = activity.getSharedPreferences(HISTORY_FILE_NAME, 0);
        return settings.getString("version", "0");
    }

    public static String getChampName(int id) {
        if (champions == null) return "NOT FOUND";
        return champions.getData().get(id).getName();
    }

    public static String getChampKey(int id) {
        return champions.getData().get(id).getKey();
    }

    public static Bitmap getChampImage(Activity activity, int id) {
        if (cw == null) {
            cw = new ContextWrapper(activity.getApplicationContext());
        }
        if (directory == null) {
            directory = cw.getDir("Images", Context.MODE_PRIVATE);
        }

        File f = new File(directory, id + ".png");
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return b;
    }

}
