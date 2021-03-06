/*
 * Copyright 2013 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.activity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.games.GamesClient;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import roboguice.inject.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 24.06.13
 * Time: 00:14
 */
public class YoutubeActivity extends GdgActivity implements YouTubePlayer.OnInitializedListener {

    @InjectView(R.id.videoContainer)
    private LinearLayout mContainer;

    private SharedPreferences mPreferences;
    private YouTubePlayerSupportFragment mPlayerFragment;
    private boolean mCounted = false;
    private boolean mGdl = false;

    private static final int PORTRAIT_ORIENTATION = Build.VERSION.SDK_INT < 9
            ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

    @Override
    protected void onStart() {
        super.onStart();
        App.getInstance().getTracker().sendView("/YouTube");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_youtube);

        mPlayerFragment =
                (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        mPlayerFragment.initialize(getString(R.string.android_simple_api_access_key), this);

        setRequestedOrientation(PORTRAIT_ORIENTATION);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoutubeActivity.this.finish();
            }
        });

        mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {

        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onLoaded(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onAdStarted() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onVideoStarted() {

            }

            @Override
            public void onVideoEnded() {
                if(!mCounted) {
                    SharedPreferences.Editor editor = mPreferences.edit();

                    editor.putInt(Const.SETTINGS_VIDEOS_PLAYED, mPreferences.getInt(Const.SETTINGS_VIDEOS_PLAYED,0)+1);

                    if(getIntent().hasExtra("gdl"))
                        editor.putInt(Const.SETTINGS_VIDEOS_PLAYED, mPreferences.getInt(Const.SETTINGS_VIDEOS_PLAYED,0)+1);

                    editor.commit();
                }

                if(mPreferences.getInt(Const.SETTINGS_VIDEOS_PLAYED,0) == 10) {
                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                                @Override
                                public void onGotGamesClient(GamesClient c) {
                                    c.unlockAchievement(Const.ACHIEVEMENT_CINEPHILE);
                                }
                            });
                        }
                    }, 1000);
                }

                if(mPreferences.getInt(Const.SETTINGS_GDL_VIDEOS_PLAYED,0) == 5) {
                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                                @Override
                                public void onGotGamesClient(GamesClient c) {
                                    c.unlockAchievement(Const.ACHIEVEMENT_GDL_ADDICT);
                                }
                            });
                        }
                    }, 1000);
                }
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        if(!wasRestored)
            youTubePlayer.loadVideo(getIntent().getStringExtra("video_id"));
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, getString(R.string.youtube_init_failed), Toast.LENGTH_LONG).show();
    }

}
