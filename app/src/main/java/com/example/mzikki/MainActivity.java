package com.example.mzikki;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView =  findViewById(R.id.listView);


    }

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_PERMISSIONS = 12345;
    private static final int PERMISSIONS_COUNT = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsDenied(){
        for (int i = 0; i<PERMISSIONS_COUNT; i++){

            if (checkSelfPermission(PERMISSIONS[i])!= PackageManager.PERMISSION_GRANTED){

                return true;
            }
        }

        return  false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
             @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (arePermissionsDenied()){
            ((ActivityManager) (Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))))
                    .clearApplicationUserData();
            recreate();



        }else{

            onResume();


        }

    }

    private boolean isMusicPlayerInit;
    private List<String> musicFilesList;

    private void addMusicFilesFrom(String dirPath){
        Log.d("babe", "addMusicFilesFrom: "+dirPath);

        final File musicDir = new File(dirPath);
        if (!musicDir.exists()){
            musicDir.mkdir();
            return;
        }
        final File[] files = musicDir.listFiles();

        Log.d("bba", "addMusicFilesFrom: " +files);


        for (File file:files){
             String path = file.getAbsolutePath();
            if (path.endsWith(".mp3")){
                musicFilesList.add(path);
            }
        }

    }

    private void fillMusicList(){
        musicFilesList.clear();

//        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_MUSIC
//        )));
//        addMusicFilesFrom(String.valueOf(
//        this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + "fileNameWithExtension")));

//        addMusicFilesFrom(String.valueOf(Environment.DIRECTORY_MUSIC));
//        addMusicFilesFrom(String.valueOf(Environment.DIRECTORY_MUSIC));

        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC
        )));

//        addMusicFilesFrom("/storage/emulat");

//        addMusicFilesFrom(String.valueOf(getExternalFilesDir(
//                Environment.DIRECTORY_RINGTONES
//        )));

        Log.d("bro", "onResume: "+musicFilesList);
    }




    private MediaPlayer mp;
    public int playMusicFile(String path){

        mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
            mp.setLooping(true);





        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp.getDuration();

    }

    private int songPosition;
    private boolean isSongPlaying;
    private int il = 0;


    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        if(!isMusicPlayerInit){

            final SeekBar seekBar = findViewById(R.id.seekBar);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int songProgress;
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    songProgress= progress;

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    songPosition = songProgress;
                    mp.seekTo(songProgress);

                }
            });

            final TextAdapter textAdapter = new TextAdapter();
            musicFilesList = new ArrayList<>();
            fillMusicList();

            textAdapter.setData(musicFilesList);
            listView.setAdapter(textAdapter);

            final TextView songPositionTextView = findViewById(R.id.currentPosition);
            final TextView songDurationTextView = findViewById(R.id.songDuration);
            final ImageButton pauseButton = findViewById(R.id.pauseButton);
            final View playBackControls = findViewById(R.id.playBackButtons);
            final TextView songNameTv = findViewById(R.id.songName);
            songNameTv.setSelected(true);

            final ImageButton nextButton = findViewById(R.id.nextButton);
            final ImageButton prevButton = findViewById(R.id.prevButton);
//            songNameTv.setText(""+musicFilesList);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        final int position, long id) {


                    if (isSongPlaying) {
                        mp.pause();
                        songPosition = 0;

                    }
                    pauseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isSongPlaying){
                                mp.pause();
                                pauseButton.setBackgroundResource(R.drawable.ic_play2);
                            }else{
                                mp.start();
                                pauseButton.setBackgroundResource(R.drawable.ic_pause2);
                            }
                            isSongPlaying = !isSongPlaying;
                        }
                    });




                    pauseButton.setBackgroundResource(R.drawable.ic_pause2);
                    view.setBackgroundColor(Color.RED);
//                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//                    listView.setItemChecked(position,true);

                    songNameTv.setVisibility(View.VISIBLE);

                    String ddf = musicFilesList.get(position).substring(musicFilesList
                            .get(position).lastIndexOf("/")+1);

                    songNameTv.setText(ddf);

                    final String musicFilePath = musicFilesList.get(position);
                    final int songDuration = playMusicFile(musicFilePath)/1000;
                    seekBar.setMax(songDuration);

                    nextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mp.pause();
//                            songPosition = 0;
                            if (!isSongPlaying){
                                songPosition = 0;
                                pauseButton.setBackgroundResource(R.drawable.ic_pause2);}
                            il+=1;

                            final String musicFilePath = musicFilesList.get((position+il)%musicFilesList.size());
                            final int songDuration = playMusicFile(musicFilePath)/1000;
                            seekBar.setMax(songDuration);
                            String ddf = musicFilesList.get((position+il)%musicFilesList.size()).substring(musicFilesList
                                    .get((position+il)%musicFilesList.size()).lastIndexOf("/")+1);

                            songNameTv.setText(ddf);

                        }
                    });


//                    seekBar.setVisibility(View.VISIBLE);
                    playBackControls.setVisibility(View.VISIBLE);
                    songDurationTextView.setText(String.valueOf(songDuration/60)+":"
                            +String.valueOf(songDuration%60));

                    isSongPlaying = true;

                    new Thread(){
                        int song = mp.getCurrentPosition()/1000;
                        public void run(){

                            while (song<songDuration) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (isSongPlaying) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            song = mp.getCurrentPosition()/1000;

                                            seekBar.setProgress(songPosition);
                                            if (song%60<10) {
                                                songPositionTextView.setText(String.valueOf(song / 60 + ":0"
                                                        + song % 60));
                                            }else
                                            songPositionTextView.setText(String.valueOf(song / 60 + ":"
                                                    + song % 60));

                                            if (song==songDuration){


                                            }
                                        }

                                    });

                                }
                                songPosition++;

                            }
                        }
                    }.start();





                }

            });

            isMusicPlayerInit = true;
        }
    }

    class TextAdapter extends BaseAdapter{

        private List<String> data = new ArrayList<>();

        void setData(List<String> mData){

            data.clear();
            data.addAll(mData);
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){

                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.myItem)));
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = data.get(position);
            holder.info.setText(item.substring(item.lastIndexOf('/')+1));


            return convertView;
        }

        class ViewHolder{
            TextView info;
            ViewHolder(TextView mInfo){
                info = mInfo;
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
       mp = new MediaPlayer();
        mp.pause();
        mp.release();
    }
}
