package com.example.manatee.pomodoro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by manatee on 2016/4/18.
 */
public class BmobDataBase {
    private MyDataBaseHelper dbHelper;
    private Context mContext;
    private List<BmobObject> remoteInsertList = new ArrayList<>();
    private List<BmobObject> remoteUpdateList = new ArrayList<>();
    private List<BmobObject> remoteDeleteList = new ArrayList<>();
    private List<ScheduleUnit> remoteList = new ArrayList<>();
    private List<ScheduleUnit> localList = new ArrayList<>();
    private List<BmobObject> localInsertList = new ArrayList<>();
    private int remoteInsertNum;                                      //一次同步需要操作的数据个数
    private int remoteUpdateNum;
    private int remoteDeleteNum;
    private int localInsertNum;
    private boolean uploadFailed;                               //为了只让失败显示一次
    private boolean downloadFailed;
    private boolean isDownload;                                 //true代表从download，false代表upload
    private static final int ReadRemoteInit = 0;
    private static final int ReadRemoteFinish = 1;
    private static final int ReadRemoteError = 2;
    private static final int UpLoadRemoteFinish = 3;
    private static final int UpLoadRemoteError = 4;

    public BmobDataBase(Context context) {
        mContext = context;
        dbHelper = new MyDataBaseHelper(context, "Pomodoro.db", null, 1);
        dbHelper.getWritableDatabase();
        uploadFailed = false;
        downloadFailed = false;
    }

    private void getDataFromBmob() {
        BmobQuery<ScheduleUnit> query = new BmobQuery<>();
        query.setLimit(100);                                                                        //查询云端数据
        query.doSQLQuery(mContext, "select * from ScheduleUnit", new SQLQueryListener<ScheduleUnit>() {
            @Override
            public void done(BmobQueryResult<ScheduleUnit> bmobQueryResult, BmobException e) {
                if (e == null) {
                    List<ScheduleUnit> list = bmobQueryResult.getResults();
                    for (Iterator<ScheduleUnit> it = list.iterator(); it.hasNext(); )
                        remoteList.add(it.next());
                    Message message = new Message();
                    message.what = ReadRemoteFinish;
                    handler.sendMessage(message);
                } else {
                    Toast.makeText(mContext, "无法获取云端数据," + e.getMessage(), Toast.LENGTH_SHORT);
                    Message message = new Message();
                    message.what = ReadRemoteError;
                    handler.sendMessage(message);
                }
            }
        });
    }

    private void getDataFormLocal() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from events", null);                                  //查询本地数据
        if (cursor.moveToFirst()) {
            do {
                ScheduleUnitPO unitPO = new ScheduleUnitPO(cursor);
                localList.add(unitPO.toBmob());
            } while (cursor.moveToNext());
        }
    }

    private ScheduleUnit findUnitInListById(List<ScheduleUnit> list, Long id) {
        ScheduleUnit unitBmob;
        for (Iterator<ScheduleUnit> it = list.iterator(); it.hasNext(); ) {
            unitBmob = it.next();
            if (unitBmob.getUnitId() == id)
                return unitBmob;
        }
        return null;
    }

    public void upload() {
        isDownload = false;
        //清零所有list，重新查询
        remoteInsertList.clear();
        remoteUpdateList.clear();
        remoteList.clear();
        remoteInsertNum = 0;
        remoteUpdateNum = 0;
        remoteDeleteNum = 0;
        uploadFailed = false;
        //查询本地数据和云端数据
        getDataFormLocal();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromBmob();
            }
        }).start();
    }

    private void uploadSync() {
        //根据本地和云端数据的不同，计算update、delete、insert的list
        for (Iterator<ScheduleUnit> it = localList.iterator(); it.hasNext(); ) {
            ScheduleUnit unitLocal = it.next();
            ScheduleUnit unitRemote = findUnitInListById(remoteList, unitLocal.getUnitId());
            if (unitRemote == null) {                                         //云端没有，则需要insert
                remoteInsertList.add(unitLocal);
            } else {
                ScheduleUnitPO localPO = new ScheduleUnitPO(unitLocal);
                ScheduleUnitPO remotePO = new ScheduleUnitPO(unitRemote);
                if (!localPO.isSame(remotePO)) {                          //云端有，但数据和本地不同，需要update
                    ScheduleUnit unitTmp = localPO.toBmob();
                    unitTmp.setObjectId(unitRemote.getObjectId());
                    remoteUpdateList.add(unitTmp);
                }
                remoteList.remove(unitRemote);                            //云端和本地都有的数据移除，剩下的就是云端需要删除的
            }
        }
        if (remoteInsertList.size() != 0) {                                   //最后再把剩下的上传
            remoteInsertNum = remoteInsertList.size();
            insertRemote();
        }
        if (remoteUpdateList.size() != 0) {
            remoteUpdateNum = remoteUpdateList.size();
            updateRemote();
        }

        for (Iterator<ScheduleUnit> it = remoteList.iterator(); it.hasNext(); ) {  //云端列表剩下的就是需要删除的了
            remoteDeleteList.add(it.next());
        }
        if (remoteDeleteList.size() != 0) {
            remoteDeleteNum = remoteDeleteList.size();
            deleteRemote();
        }
        if (remoteDeleteNum == 0 && remoteUpdateNum == 0 && remoteInsertNum == 0)
            Toast.makeText(mContext, "已与云端同步", Toast.LENGTH_SHORT).show();
    }

    public void download() {
        isDownload = true;
        localInsertNum = 0;
        localInsertList.clear();
        downloadFailed = false;
        //查询本地数据和云端数据
        getDataFormLocal();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDataFromBmob();
            }
        }).start();
    }

    private void downloadSync() {
        //只要本地存在数据，则以本地为准
        //只从云端获取本地没有的数据
        for (Iterator<ScheduleUnit> it = remoteList.iterator(); it.hasNext(); ) {
            ScheduleUnit unitRemote = it.next();
            ScheduleUnit unitLocal = findUnitInListById(localList, unitRemote.getUnitId());
            if (unitRemote == null) {                                         //本地没有，需要insert
                localInsertList.add(unitLocal);
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        while (localInsertList.size() != 0){

        }

    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ReadRemoteFinish:
                    if (isDownload)
                        downloadSync();
                    else
                        uploadSync();
                    break;
                case ReadRemoteError:
                    Toast.makeText(mContext, "无法从云端获取数据", Toast.LENGTH_SHORT).show();   //只会失败一次，不需要alreadyFailed
                    break;
                case UpLoadRemoteError:
                    if (!uploadFailed) {                                                    //增删改都要报这个错误，所以需要置位
                        Toast.makeText(mContext, "上传云端出错", Toast.LENGTH_SHORT).show();//alreadyFailed让Toast只显示一次
                        uploadFailed = true;
                    }
                    break;
                case UpLoadRemoteFinish:
                    if (remoteDeleteNum <= 0 && remoteUpdateNum <= 0 && remoteInsertNum <= 0)
                        Toast.makeText(mContext, "已与云端同步", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void insertRemote() {
        while (remoteInsertList.size() != 0) {
            List<BmobObject> tmpList = new ArrayList<BmobObject>();
            int len;
            if (remoteInsertList.size() >= 50) {
                len = 50;
            } else {
                len = remoteInsertList.size();
            }
            for (int i = 0; i < len; i++) {
                tmpList.add(remoteInsertList.get(0));
                remoteInsertList.remove(0);
            }
            new BmobObject().insertBatch(mContext, tmpList, new SaveListener() {
                @Override
                public void onSuccess() {
                    remoteInsertNum -= 50;
                    Message message = new Message();
                    message.what = UpLoadRemoteFinish;
                    handler.sendMessage(message);
                }

                @Override
                public void onFailure(int i, String s) {
                    Message message = new Message();
                    message.what = UpLoadRemoteError;
                    handler.sendMessage(message);
                }
            });
        }
    }

    private void updateRemote() {
        while (remoteUpdateList.size() != 0) {
            List<BmobObject> tmpList = new ArrayList<BmobObject>();
            int len;
            if (remoteUpdateList.size() >= 50) {
                len = 50;
            } else {
                len = remoteUpdateList.size();
            }
            for (int i = 0; i < len; i++) {
                tmpList.add(remoteUpdateList.get(0));
                remoteUpdateList.remove(0);
            }
            new BmobObject().updateBatch(mContext, tmpList, new UpdateListener() {
                @Override
                public void onSuccess() {
                    remoteUpdateNum -= 50;
                    Message message = new Message();
                    message.what = UpLoadRemoteFinish;
                    handler.sendMessage(message);
                }

                @Override
                public void onFailure(int i, String s) {
                    Message message = new Message();
                    message.what = UpLoadRemoteError;
                    handler.sendMessage(message);
                }
            });
        }
    }

    private void deleteRemote() {
        while (remoteDeleteList.size() != 0) {
            List<BmobObject> tmpList = new ArrayList<BmobObject>();
            int len;
            if (remoteDeleteList.size() >= 50) {
                len = 50;
            } else {
                len = remoteDeleteList.size();
            }
            for (int i = 0; i < len; i++) {
                tmpList.add(remoteDeleteList.get(0));
                remoteDeleteList.remove(0);
            }
            new BmobObject().deleteBatch(mContext, tmpList, new DeleteListener() {
                @Override
                public void onSuccess() {
                    remoteDeleteNum -= 50;
                    Message message = new Message();
                    message.what = UpLoadRemoteFinish;
                    handler.sendMessage(message);
                }

                @Override
                public void onFailure(int i, String s) {
                    Message message = new Message();
                    message.what = UpLoadRemoteError;
                    handler.sendMessage(message);
                }
            });
        }
    }
}
