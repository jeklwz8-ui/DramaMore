package com.example.dramasdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bytedance.sdk.shortplay.api.PSSDK;
import com.bytedance.sdk.shortplay.api.ShortPlay;

public class ApiTestFragment extends AppFragment {
    private static final String TAG = "ApiTestFragment";
    private TextView supportPlayTV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_api_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindButtonAction(view, R.id.btn_list, DemoShortPlayFeedListFragment.class);
        bindButtonAction(view, R.id.btn_stream, DemoShortPlayStreamFragment.class);
        bindButtonAction(view, R.id.btn_search, DemoSearchDramaFragment.class);
        bindButtonAction(view, R.id.btn_search_by_tag, DemoSearchByTagFragment.class);
        view.findViewById(R.id.btn_get_category_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PSSDK.requestCategoryList("en", new PSSDK.CategoryListResultListener() {
                    @Override
                    public void onFail(PSSDK.ErrorInfo errorInfo) {
                        toast("请求分类列表失败，" + errorInfo.code + ", " + errorInfo.msg);
                    }

                    @Override
                    public void onSuccess(PSSDK.FeedListLoadResult<ShortPlay.ShortPlayCategory> result) {
                        Log.d(TAG, "onSuccess() called with: result = [" + result + "]");
                        runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                showResultDialog(result.dataList.toString());
                            }
                        });

                    }
                });
            }
        });

        TextView aboutTV = view.findViewById(R.id.tv_about);
        aboutTV.setText(PSSDK.getVersion());

        supportPlayTV = view.findViewById(R.id.tv_check_play_status);
        supportPlayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPlay();
            }
        });
        view.findViewById(R.id.btn_set_collect_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userClickSetCollectStatus();
            }
        });
        view.findViewById(R.id.btn_set_like_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userClickSetLikeStatus();
            }
        });
    }

    private void userClickSetLikeStatus() {
        LinearLayout inputContainer = new LinearLayout(getActivity());
        inputContainer.setOrientation(LinearLayout.VERTICAL);
        final EditText inputShortPlayID = new EditText(getActivity());
        inputShortPlayID.setHint("输入短剧ID(ShortPlay.id)");
        inputShortPlayID.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputContainer.addView(inputShortPlayID, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        final EditText episodeIDInput = new EditText(getActivity());
        episodeIDInput.setHint("输入剧集视频ID(EpisodeData.id)");
        episodeIDInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputContainer.addView(episodeIDInput, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        int[] checkedItem = new int[]{0};
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("设置视频点赞状态")
                .setView(inputContainer)
                .setSingleChoiceItems(new String[]{"点赞", "取消点赞"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItem[0] = which;
                    }
                })
                .setPositiveButton("确定", null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog1 = (AlertDialog) dialog;
                Button positiveButton = alertDialog1.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText = inputShortPlayID.getText().toString();
                        if (TextUtils.isEmpty(inputText)) {
                            toast("请输入短剧ID(ShortPlay.id)");
                            return;
                        }
                        String episodeID = episodeIDInput.getText().toString();
                        if (TextUtils.isEmpty(episodeID)) {
                            toast("请输入视频ID(EpisodeData.id)");
                            return;
                        }
                        dialog.dismiss();
                        final boolean isCollect = checkedItem[0] == 0;
                        PSSDK.setLike(Long.parseLong(inputText), Long.parseLong(episodeID), isCollect, new PSSDK.ActionResultListener() {
                            @Override
                            public void onSuccess() {
                                toast(isCollect ? "点赞成功" : "取消点赞成功");
                            }

                            @Override
                            public void onFail(PSSDK.ErrorInfo errorInfo) {
                                toast((isCollect ? "点赞失败" : "取消点赞失败") + ", " + errorInfo.code + ", " + errorInfo.msg);
                            }
                        });
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void userClickSetCollectStatus() {
        final EditText input = new EditText(getActivity());
        input.setHint("输入短剧ID");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        int[] checkedItem = new int[]{0};
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("设置短剧收藏状态")
                .setView(input)
                .setSingleChoiceItems(new String[]{"收藏", "取消收藏"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedItem[0] = which;
                    }
                })
                .setPositiveButton("确定", null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog1 = (AlertDialog) dialog;
                Button positiveButton = alertDialog1.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText = input.getText().toString();
                        if (TextUtils.isEmpty(inputText)) {
                            toast("请输入短剧ID");
                            return;
                        }
                        dialog.dismiss();
                        final boolean isCollect = checkedItem[0] == 0;
                        PSSDK.setCollected(Long.parseLong(inputText), isCollect, new PSSDK.ActionResultListener() {
                            @Override
                            public void onSuccess() {
                                toast(isCollect ? "收藏成功" : "取消收藏成功");
                            }

                            @Override
                            public void onFail(PSSDK.ErrorInfo errorInfo) {
                                toast((isCollect ? "收藏失败" : "取消收藏失败") + ", " + errorInfo.code + ", " + errorInfo.msg);
                            }
                        });
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void showResultDialog(String content) {
        new AlertDialog.Builder(getActivity())
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private void checkPlay() {
        // 检查当前地区是否支持播放功能
        supportPlayTV.setText("检查当前地区是否支持播放...");
        supportPlayTV.setEnabled(false);
        PSSDK.checkPlayStatus(new PSSDK.ServiceCheckResultListener() {
            @Override
            public void onCheckResult(PSSDK.ServiceAvailableResult result) {
                Log.d(TAG, "onCheckResult() called with: result = [" + result + "]");
                FragmentActivity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        supportPlayTV.setEnabled(true);
                        switch (result) {
                            case SUPPORT:
                                toast("当前地区是否支持播放：支持");
                                supportPlayTV.setText("当前地区支持播放");
                                break;
                            case NOT_SUPPORT:
                                toast("当前地区是否支持播放：不支持");
                                supportPlayTV.setText("当前地区不支持播放");
                                break;
                            case NETWORK_ERROR:
                                toast("当前地区是否支持播放：检查失败");
                                supportPlayTV.setText("当前地区支持播放检查失败，点击重试");
                                break;
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        checkPlay();
    }

    private void bindButtonAction(View rootView, int resID, Class<? extends Fragment> fragmentClass) {
        rootView.findViewById(resID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Fragment fragment = fragmentClass.newInstance();
                    getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).show(fragment).addToBackStack(null).commit();
                } catch (Exception e) {
                    Log.e(TAG, "add fragment fail: ", e);
                    toast("add fragment fail, " + e.getMessage());
                }
            }
        });
    }
}