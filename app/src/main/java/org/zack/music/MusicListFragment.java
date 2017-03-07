package org.zack.music;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;



public class MusicListFragment extends Fragment {


    private MainLeftListener listener;


    private List<Music> musics;
    private ListAdapter listAdapter;

    private RecyclerView rv;
    private LinearLayoutManager llm;


    private int current = -1;


    public MusicListFragment() {
        // Required empty public constructor
    }

    public static MusicListFragment newInstance() {
        MusicListFragment fragment = new MusicListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listAdapter = new ListAdapter();

/*        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                musics = getMusicList();
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
        }.execute();*/


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        rv = (RecyclerView) view.findViewById(R.id.left_music_list);
        llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
//        listAdapter = new ListAdapter();
        rv.setAdapter(listAdapter);
    }


    public void setMusics(final List<Music> musics) {
        this.musics = musics;
        listAdapter.notifyDataSetChanged();
/*        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 0) {
                    listAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Music music : musics) {
                    music.setImage(Music.createAlbumArt(music.getPath()));
                }
                Message message = handler.obtainMessage();
                message.what = 0;
                handler.sendMessage(message);
            }
        }).start();*/
    }


    public void initRecyclerViewPosition(int position) {
        llm.scrollToPositionWithOffset(position, 0);
    }

    public void initRecyclerViewItemDisplay(int current, int last) {
        Log.d("TAG", "current = " + current);
        this.current = current;
        listAdapter.notifyItemChanged(current);
        listAdapter.notifyItemChanged(last);
    }


    private class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView titleView;
        private TextView albumView;
        private ImageView iconView;
        private TextView artistView;
        private ImageView playingView;
        public ListViewHolder(View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.music_title);
            iconView = (ImageView) itemView.findViewById(R.id.music_icon);
            albumView = (TextView) itemView.findViewById(R.id.music_album);
            artistView = (TextView) itemView.findViewById(R.id.music_artist);
            playingView = (ImageView) itemView.findViewById(R.id.music_playing);

        }

        public void initView(final int position) {
            final Music music = musics.get(position);
            titleView.setText(!TextUtils.isEmpty(music.getTitle()) ? music.getTitle() : music.getName());
            albumView.setText(music.getAlbum());
            artistView.setText(music.getArtist());
            playingView.setVisibility(position == current ? View.VISIBLE : View.INVISIBLE);
/*            new Thread(new Runnable() {
                @Override
                public void run() {
                    final byte[] iconByte = Music.getAlbumByte(music.getPath());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MusicListFragment.this)
                                    .load(iconByte)
                                    .placeholder(R.drawable.album_icon)
                                    .into(iconView);
                        }
                    });
                }
            }).start();*/
//            Glide.with(MusicListFragment.this).load(Music.getAlbumByte(music.getPath())).into(iconView);
//            Bitmap icon = music.getImage();

//            backgroundView.setImageBitmap(background == null ?
//                    BitmapFactory.decodeResource(getResources(), R.drawable.album_icon) : background);
/*            if (icon != null) {
                iconView.setImageBitmap(icon);
            } else {
                iconView.setImageResource(R.drawable.album_icon);
            }*/



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.clickPosition(position);
                    }
                }
            });


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
//        void putMusicList(List<Music> musics);
        void clickPosition(int position);
    }
    

}
