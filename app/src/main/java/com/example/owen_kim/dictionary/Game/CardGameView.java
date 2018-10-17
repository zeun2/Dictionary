package com.example.owen_kim.dictionary.Game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.example.owen_kim.dictionary.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class CardGameView extends View {
    MediaPlayer m_Sound_BackGround; //배경 음악

    public static final int STATE_READY = 0; // 게임 시작 전 준비 상태
    public static final int STATE_GAME = 1; // 게임 중
    public static final int STATE_END = 2; // 게임 종료

    // 게임의 상태 값을 저장하는 멤버 변수
    private int m_state = STATE_READY; // 초깃값은 준비 상태
    private CardGameThread _thread; // 쓰레드

    Bitmap m_Card_Backside;
    Bitmap m_BackGroundImage;
    Bitmap m_Card_Picture_Book;
    Bitmap m_Card_Picture_Chair;
    Bitmap m_Card_Picture_Laptop;
    Bitmap m_Card_Word_Book;
    Bitmap m_Card_Word_Chair;
    Bitmap m_Card_Word_Laptop;
    Bitmap m_Start_Button;

    // 화면에 표시할 카드
    Card m_Shuffle[][];

    // 짝맞추기 비교를 위한 변수
    private Card m_SelectCard_1 = null; // 첫 번째로 선택한 카드
    private Card m_SelectCard_2 = null; // 두 번째로 선택한 카드

    //크기 받아오는 변수
    private int width;
    private int height;
    private int card_w;
    private int card_h;
    private int button_w;
    private int button_h;

    //타이머 변수
    private CountDownTimer countDownTimer;
    private long timeLeftInMilliseconds = 3999; // 3초
    private boolean timerRunning;
    private Paint countdownText = new Paint();
    private String timeLeftText = "";
    //걸린시간 계산
    private long now;
    private Date startTime;
    private Date endTime;
    private int clear=0;
    private Paint recordText = new Paint();
    private String record;

    public CardGameView(Context context) {
        super(context);
        //MediaPlayer를 이용해서 리소스 로드
        m_Sound_BackGround = MediaPlayer.create(context, R.raw.pororo);
        m_Sound_BackGround.start();

        //  키 입력을 위해 포커스를 줍니다.
        setFocusable(true);

        m_BackGroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.pororo);
        m_Card_Backside = BitmapFactory.decodeResource(getResources(), R.drawable.lion);
        m_Card_Picture_Book = BitmapFactory.decodeResource(getResources(), R.drawable.picturebook);
        m_Card_Picture_Chair = BitmapFactory.decodeResource(getResources(), R.drawable.picturechair);
        m_Card_Picture_Laptop = BitmapFactory.decodeResource(getResources(), R.drawable.picturelaptop);
        m_Card_Word_Book = BitmapFactory.decodeResource(getResources(), R.drawable.wordbook);
        m_Card_Word_Chair = BitmapFactory.decodeResource(getResources(), R.drawable.wordchair);
        m_Card_Word_Laptop  = BitmapFactory.decodeResource(getResources(), R.drawable.wordlaptop);
        m_Start_Button = BitmapFactory.decodeResource(getResources(), R.drawable.start_button);

        //이미지 크기 받아오기
        card_w = m_Card_Backside.getWidth();
        card_h = m_Card_Backside.getHeight();
        button_w = m_Start_Button.getWidth();
        button_h = m_Start_Button.getHeight();

        //화면에 표시할 카드만큼 할당받음(2X3)
        m_Shuffle = new Card[2][3];

        // 카드 섞기
        SetCardShuffle();

        // 짝맞추기를 검사하는 스레드 실행
        _thread = new CardGameThread(this);
        _thread.start();
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        // 스페이스 바를 눌렀을 때 배경음 일시 정지/다시 재생
        if(KeyCode == KeyEvent.KEYCODE_SPACE){
            if (m_Sound_BackGround.isPlaying())
                m_Sound_BackGround.pause();
            else
                m_Sound_BackGround.start();

            // 화면을 갱신시킵니다.
            //invalidate();
        }
        return super.onKeyDown(KeyCode, event);
    }

    public void SetCardShuffle() {
        ArrayList<Double> cards = new ArrayList<Double>(Arrays.asList(1.0, 1.5, 2.0, 2.5, 3.0, 3.5));
        Collections.shuffle(cards);
        // 각각의 색을 가진 카드들을 생성
        m_Shuffle[0][0] = new Card(cards.get(0));
        m_Shuffle[0][1] = new Card(cards.get(1));
        m_Shuffle[0][2] = new Card(cards.get(2));
        m_Shuffle[1][0] = new Card(cards.get(3));
        m_Shuffle[1][1] = new Card(cards.get(4));
        m_Shuffle[1][2] = new Card(cards.get(5));
    }

    public void startGame() {
        // 모든 카드를 뒷면 상태로 만듭니다.
        m_Shuffle[0][0].m_state = Card.CARD_CLOSE;
        m_Shuffle[0][1].m_state = Card.CARD_CLOSE;
        m_Shuffle[0][2].m_state = Card.CARD_CLOSE;
        m_Shuffle[1][0].m_state = Card.CARD_CLOSE;
        m_Shuffle[1][1].m_state = Card.CARD_CLOSE;
        m_Shuffle[1][2].m_state = Card.CARD_CLOSE;

        // 화면을 갱신합니다.
        invalidate();
    }

    public void openCards() {
        // 모든 카드를 앞면 상태로 만듭니다.
        m_Shuffle[0][0].m_state = Card.CARD_SHOW;
        m_Shuffle[0][1].m_state = Card.CARD_SHOW;
        m_Shuffle[0][2].m_state = Card.CARD_SHOW;
        m_Shuffle[1][0].m_state = Card.CARD_SHOW;
        m_Shuffle[1][1].m_state = Card.CARD_SHOW;
        m_Shuffle[1][2].m_state = Card.CARD_SHOW;
    }

    @Override
    public void onDraw(Canvas canvas) {
        //모바일 화면 크기 받기
        width = canvas.getWidth();
        height = canvas.getHeight();

        m_BackGroundImage = Bitmap.createScaledBitmap(m_BackGroundImage, width, height, true);

        // 배경 이미지 그려주기
        canvas.drawBitmap(m_BackGroundImage, 0, 0, null);

        //시작 버튼, 타이머 그려주기
        if(timeLeftText == "") //시작button
            canvas.drawBitmap(m_Start_Button, 275, 150, null);
        else if(timeLeftText.equals("START!")) // 게임시작text
            canvas.drawText(timeLeftText, 200, 200, countdownText);
        else if(clear==6) { // 게임성공text
            recordText.setTextSize(50);
            recordText.setColor(Color.BLUE);
            canvas.drawText(record, 175, 100, recordText) ;
            countdownText.setColor(Color.RED);
            canvas.drawText(timeLeftText, 200, 200, countdownText);
        }
        else { // 3,2,1 timerText
            countdownText.setTextSize(100);
            countdownText.setStrokeWidth(10);
            canvas.drawText(timeLeftText, 325, 200, countdownText);
        }

        // 카드들을 그려주기 for
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                //카드 x,y 좌표 정하기
                int card_x = 75 + y * 200;
                int card_y = 250 + x * 300;
                // 카드의 앞면을 그려야 하는 경우
                if (m_Shuffle[x][y].m_state == Card.CARD_SHOW || m_Shuffle[x][y].m_state == Card.CARD_PLAYEROPEN || m_Shuffle[x][y].m_state == Card.CARD_MATCHED) {
                    // 가지고 있는 색상 값에 따라 다른 이미지 그려주기
                    if (m_Shuffle[x][y].m_Color == Card.IMG_BOOK)
                        canvas.drawBitmap(m_Card_Picture_Book, card_x, card_y, null);
                    else if (m_Shuffle[x][y].m_Color == Card.IMG_CHAIR)
                        canvas.drawBitmap(m_Card_Picture_Chair, card_x, card_y, null);
                    else if (m_Shuffle[x][y].m_Color == Card.IMG_LAPTOP)
                        canvas.drawBitmap(m_Card_Picture_Laptop, card_x, card_y, null);
                    else if (m_Shuffle[x][y].m_Color == Card.WORD_BOOK)
                        canvas.drawBitmap(m_Card_Word_Book, card_x, card_y, null);
                    else if (m_Shuffle[x][y].m_Color == Card.WORD_CHAIR)
                        canvas.drawBitmap(m_Card_Word_Chair, card_x, card_y, null);
                    else if (m_Shuffle[x][y].m_Color == Card.WORD_LAPTOP)
                        canvas.drawBitmap(m_Card_Word_Laptop, card_x, card_y, null);
                }
                // 카드의 뒷면을 그려야 하는 경우
                else
                    canvas.drawBitmap(m_Card_Backside, card_x, card_y, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int px = (int) event.getX();
        int py = (int) event.getY();
        // 게임 준비 중
        if (m_state == STATE_READY) {
            Rect box_btutton = new Rect(275, 150, 275+button_w, 150+button_h);
            if (box_btutton.contains(px, py)) {
                timerStart();
                openCards();
            }
        }
        // 게임 중
        else if (m_state == STATE_GAME) {
            // 비교하려고 두 개의 카드를 이미 뒤집은 경우
            if (m_SelectCard_1 != null && m_SelectCard_2 != null)
                return true;
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 3; y++) {
                    //카드 좌표
                    int card_x = 75 + y * 200;
                    int card_y = 250 + x * 300;
                    //각 카드의 박스 값을 생성
                    Rect box_card = new Rect(card_x, card_y, card_x+card_w, card_y+card_h);
                    if (box_card.contains(px, py)) {
                        // (x,y)에 위치한 카드가 선택되었습니다.
                        if (m_Shuffle[x][y].m_state != Card.CARD_MATCHED) {
                            // 맞춘 카드는 뒤집을 필요가 없습니다.
                            if (m_SelectCard_1 == null) { // 첫 카드를 뒤집으려는 것이라면
                                m_SelectCard_1 = m_Shuffle[x][y];
                                m_SelectCard_1.m_state = Card.CARD_PLAYEROPEN;
                            } else {// 이미 첫 번째 카드가 뒤집혀 있으니 두 번째로 뒤집으려는 거라면
                                if (m_SelectCard_1 != m_Shuffle[x][y]) {
                                    m_SelectCard_2 = m_Shuffle[x][y];
                                    m_SelectCard_2.m_state = Card.CARD_PLAYEROPEN;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 게임 종료
        else if (m_state == STATE_END) {
            // 게임 준비 상태로 변경
            m_state = STATE_READY;
        }

        // 화면을 갱신합니다.
        invalidate();
        return true;
    }

    public void checkMatch(){
        // 두 카드 중 하나라도 선택이 안 되었다면 비교할 필요가 없습니다.
        if(m_SelectCard_1 == null || m_SelectCard_2 == null)
            return;
        // 두 카드의 색상을 비교합니다.
        if((int)m_SelectCard_1.m_Color == (int)m_SelectCard_2.m_Color){
            // 두 카드의 색상이 같으면 두 카드를 맞춘 상태로 바꿉니다.
            m_SelectCard_1.m_state = Card.CARD_MATCHED;
            m_SelectCard_2.m_state = Card.CARD_MATCHED;

            // 화면을 갱신합니다.
            postInvalidate();

            // 다시 선택할 수 있게 null 값을 넣습니다.
            m_SelectCard_1 = null;
            m_SelectCard_2 = null;
            clear += 2;
            if(clear==6) {
                timeLeftText = "CLEAR!";
                now = System.currentTimeMillis();
                endTime = new Date(now);
                float duration = (endTime.getTime()-startTime.getTime())/(float)1000;
                record = "your record: " + duration;
            }
        }
        else {
            // 두 카드의 색상이 다른 경우 대기 시간을 주어 결과를 확인하게 합니다.
            try{
                Thread.sleep(500);
            }catch (InterruptedException r){
            }
            // 두 카드를 이전처럼 뒷면으로 돌려줍니다.
            m_SelectCard_1.m_state = Card.CARD_CLOSE;
            m_SelectCard_2.m_state = Card.CARD_CLOSE;

            // 화면을 갱신합니다.
            postInvalidate();

            // 다시 선택할 수 있게 null 값을 넣습니다.
            m_SelectCard_1 = null;
            m_SelectCard_2 = null;

        }
    }

    public void timerStart() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 500) {
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                updateTimer();
            }
            @Override
            public void onFinish() {
            }
        }.start();
        timerRunning = true;
    }

    public void updateTimer() {
        int seconds = (int)timeLeftInMilliseconds / 1000;
        timeLeftText = ""+seconds;
        if(timeLeftText.equals("0")) {
            timeLeftText = "START!";
            now = System.currentTimeMillis();
            startTime = new Date(now);
            m_state = STATE_GAME;
            startGame(); //게임을 시작합니다.
        }
        else
            invalidate();
    }

}
