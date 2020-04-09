package app.meantime;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

public class AssetReader {
    private AssetManager assetManager;

    public AssetReader(Context context){
        assetManager = context.getAssets();
    }

    public String getTextFile(String fileName){
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();
        try{
            inputStream = assetManager.open(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while((line = bufferedReader.readLine()) != null){
                builder.append(line);
                builder.append("\n");
            }
        }
        catch (IOException e){

        }
        finally {
            if(inputStream != null){
                try{
                    inputStream.close();
                }
                catch (IOException e){

                }
            }
            if(bufferedReader != null){
                try{
                    bufferedReader.close();
                }
                catch (IOException e){

                }
            }
        }
        return builder.toString();
    }
}
