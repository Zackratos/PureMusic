package org.zack.music;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainLeftFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainLeftFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainLeftFragment extends Fragment {


    private MainLeftListener listener;


    private List<Music> musics;
    private ListAdapter listAdapter;


    public MainLeftFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainLeftFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainLeftFragment newInstance() {
        MainLeftFragment fragment = new MainLeftFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new ListAdapter();

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                musics = getMusicList();
                if (listener != null) {
                    listener.putMusicList(musics);
                }
                return true;
            }


            @Override
            protected void onPostExecute(Boolean aBoolean) {
//                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    listAdapter.notifyDataSetChanged();
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            for (Music music : musics) {
                                music.setImage(createAlbumArt(music.getPath()));
                            }
                            return true;
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            if (aBoolean) {
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                    }.execute();
                }

            }
        }.execute();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_left, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.left_music_list);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
//        listAdapter = new ListAdapter();
        rv.setAdapter(listAdapter);
    }


    private List<Music> getMusicList() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<Music> musics = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    long duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    Music music = new Music.MusicBuilder()
                            .title(title)
                            .album(album)
                            .path(path)
                            .name(name)
                            .duration(duration)
                            .builder();
//                    music.setBackground(createAlbumArt(path));
                    musics.add(music);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return musics;
    }

    private void setMusicBackground() {

    }

    public Bitmap createAlbumArt(String filePath) {
        Bitmap bitmap = null;
        //能够获取多媒体文件元数据的类
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath); //设置数据源
            byte[] art = retriever.getEmbeddedPicture(); //得到字节型数据
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length); //转换为图片
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    private class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView albumView;
        private ImageView iconView;
        public ListViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.music_title);
            iconView = (ImageView) itemView.findViewById(R.id.music_icon);
        }

        public void initView(int position) {
            final Music music = musics.get(position);
            titleView.setText(music.getTitle() != null ? music.getTitle() : music.getName());
            Bitmap icon = music.getImage();

//            backgroundView.setImageBitmap(background == null ?
//                    BitmapFactory.decodeResource(getResources(), R.drawable.album_icon) : background);
            if (icon != null) {
                iconView.setImageBitmap(icon);
            } else {
                iconView.setImageResource(R.drawable.album_icon);
            }

/*            if (background != null) {
                backgroundView.setImageBitmap(background);
                music.setBackground(background);
            } else {
                Log.d("TAG", "background = " + background);
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... voids) {
                        Log.d("TAG", "doInBackground");
                        Bitmap bitmap = createAlbumArt(music.getPath());
                        music.setBackground(bitmap);
                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap != null) {
                            backgroundView.setImageBitmap(bitmap);
                        } else {
                            backgroundView.setImageResource(R.drawable.album_icon);
                        }
                    }
                }.execute();
            }*/
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

        @Override
        public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListViewHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.music_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {
            holder.initView(position);
//            holder.backgroundView.setImageBitmap();
        }

        @Override
        public int getItemCount() {
            return musics == null ? 0 : musics.size();
        }
    }


    public void setMainLeftListener(MainLeftListener listener) {
        this.listener = listener;
    }

    interface MainLeftListener {
        void putMusicList(List<Music> musics);
    }
    

}
