package com.berk.pairgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class Manager extends Activity {
    private Context context;
    private Drawable arkaresim;
    private int[][] cards;
    private List<Drawable> images;
    private Card IlkKart;
    private static int dogrutahmin = 0;
    private Card IkinciKart;
    private ButtonListener buttonListener;
    private static Object lock = new Object();

    int turns;
    private TableLayout mainTable;
    private UpdateCardsHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new UpdateCardsHandler();
        ResimYukle();
        setContentView(R.layout.main);
        
        buttonListener = new ButtonListener();

        mainTable = (TableLayout) findViewById(R.id.TableLayout03);

        context = mainTable.getContext();

        Button startgame = (Button) findViewById(R.id.startgame);
        arkaresim = getResources().getDrawable(R.drawable.icon);
        startgame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                yenioyun();
            }
        });
    }

    private void yenioyun() {

        cards = new int[4][4];

        mainTable.removeView(findViewById(R.id.TableRow01));
        mainTable.removeView(findViewById(R.id.TableRow02));

        TableRow tr = ((TableRow) findViewById(R.id.TableRow03));
        tr.removeAllViews();

        mainTable = new TableLayout(context);
        tr.addView(mainTable);

        for (int y = 0; y < 4; y++) {
            mainTable.addView(SiraYap(y));
        }

        IlkKart = null;
        KartYukle();

        turns = 0;
        ((TextView) findViewById(R.id.tv1)).setText("Tries: " + turns);

    }

    private void ResimYukle() {
        images = new ArrayList<>();

        images.add(getResources().getDrawable(R.drawable.card1));
        images.add(getResources().getDrawable(R.drawable.card2));
        images.add(getResources().getDrawable(R.drawable.card3));
        images.add(getResources().getDrawable(R.drawable.card4));
        images.add(getResources().getDrawable(R.drawable.card5));
        images.add(getResources().getDrawable(R.drawable.card6));
        images.add(getResources().getDrawable(R.drawable.card7));
        images.add(getResources().getDrawable(R.drawable.card8));
        images.add(getResources().getDrawable(R.drawable.card9));
        images.add(getResources().getDrawable(R.drawable.card10));

    }

    private void KartYukle() {
        try {

            ArrayList<Integer> list = new ArrayList<Integer>();

            for (int i = 0; i < 16; i++) {
                list.add(new Integer(i));
            }


            Random r = new Random();

            for (int i = 15; i >= 0; i--) {
                int t = 0;

                if (i > 0) {
                    t = r.nextInt(i);
                }

                t = list.remove(t).intValue();
                cards[i % 4][i / 4] = t % (8);

                Log.i("KartYukle()", "card[" + (i % 4) +
                        "][" + (i / 4) + "]=" + cards[i % 4][i / 4]);
            }
        } catch (Exception e) {
            Log.e("KartYukle()", e + "");
        }

    }

    private TableRow SiraYap(int y) {
        TableRow row = new TableRow(context);
        row.setHorizontalGravity(Gravity.CENTER);

        for (int x = 0; x < 4; x++) {
            row.addView(ImageButtonYarat(x, y));
        }
        return row;
    }

    private View ImageButtonYarat(int x, int y) {
        Button button = new Button(context);
        button.setBackgroundDrawable(arkaresim);
        button.setId(100 * x + y);
        button.setOnClickListener(buttonListener);
        return button;
    }

    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            synchronized (lock) {
                if (IlkKart != null && IkinciKart != null) {
                    return;
                }
                int id = v.getId();
                int x = id / 100;
                int y = id % 100;
                KartCevir((Button) v, x, y);
            }

        }

        private void KartCevir(Button button, int x, int y) {
            button.setBackgroundDrawable(images.get(cards[x][y]));

            if (IlkKart == null) {
                IlkKart = new Card(button, x, y);
            } else {

                if (IlkKart.x == x && IlkKart.y == y) {
                    return; //ayni kart basildi
                }

                IkinciKart = new Card(button, x, y);

                turns++;
                ((TextView) findViewById(R.id.tv1)).setText("Deneme Sayisi: " + turns);


                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            synchronized (lock) {
                                handler.sendEmptyMessage(0);
                            }
                        } catch (Exception e) {
                            Log.e("log", e.getMessage());
                        }
                    }
                };
                Timer t = new Timer(false);
                t.schedule(tt, 700);
            }
        }

    }

    class UpdateCardsHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                KontrolEt();
            }
        }

        public void KontrolEt() {
            if (cards[IkinciKart.x][IkinciKart.y] == cards[IlkKart.x][IlkKart.y]) {
                IlkKart.button.setVisibility(View.INVISIBLE);
                IkinciKart.button.setVisibility(View.INVISIBLE);
                dogrutahmin++;
                if (dogrutahmin == 8) {
                    dogrutahmin = 0;
                    Toast.makeText(context, "Oyun bitti Tebrikler",
                            Toast.LENGTH_LONG).show();
                    yenioyun();
                }
            } else {
                IkinciKart.button.setBackgroundDrawable(arkaresim);
                IlkKart.button.setBackgroundDrawable(arkaresim);
            }

            IlkKart = null;
            IkinciKart = null;
        }
    }

}