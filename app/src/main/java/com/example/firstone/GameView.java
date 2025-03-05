package com.example.firstone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private Paddle paddle;
    private Ball ball;
    private Brick[] bricks;
    private int numBricks = 30;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        paddle = new Paddle(getWidth() / 2, getHeight() - 100, 200, 20);
        ball = new Ball(getWidth() / 2, getHeight() - 150, 20);
        bricks = new Brick[numBricks];

        int brickWidth = getWidth() / 6;
        int brickHeight = 50;
        int padding = 10;
        int index = 0;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 6; col++) {
                bricks[index++] = new Brick(col * (brickWidth + padding), row * (brickHeight + padding), brickWidth, brickHeight);
            }
        }
        gameThread.setRunning(true);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            paddle.setX((int) event.getX());
        }
        return true;
    }

    public void update() {
        ball.update();
        ball.checkCollisionWithPaddle(paddle);
        for (Brick brick : bricks) {
            if (brick.isVisible() && ball.checkCollisionWithBrick(brick)) {
                brick.setVisible(false);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            paddle.draw(canvas);
            ball.draw(canvas);
            for (Brick brick : bricks) {
                if (brick.isVisible()) {
                    brick.draw(canvas);
                }
            }
        }
    }
}

class GameThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean running;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gameView.update();
                    gameView.draw(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}

class Paddle {
    public int x, y, width, height;
    private Paint paint;

    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        paint = new Paint();
        paint.setColor(Color.WHITE);
    }

    public void setX(int x) {
        this.x = x - width / 2;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}

class Ball {
    private int x, y, radius, dx = 15, dy = -15;
    private Paint paint;

    public Ball(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    public void update() {
        x += dx;
        y += dy;
        if (x <= 0 || x >= 1080 - radius) dx = -dx;
        if (y <= 0) dy = -dy;
    }

    public void checkCollisionWithPaddle(Paddle paddle) {
        if (y + radius >= paddle.y && x >= paddle.x && x <= paddle.x + paddle.width) {
            dy = -dy;
        }
    }

    public boolean checkCollisionWithBrick(Brick brick) {
        if (x >= brick.getX() && x <= brick.getX() + brick.getWidth() && y >= brick.getY() && y <= brick.getY() + brick.getHeight()) {
            dy = -dy;
            return true;
        }
        return false;
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }
}

class Brick {
    private int x, y, width, height;
    private boolean visible;
    private Paint paint;

    public Brick(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
        paint = new Paint();
        paint.setColor(Color.BLUE);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public void draw(Canvas canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint);
    }
}
