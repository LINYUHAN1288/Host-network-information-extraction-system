package demo;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;



public class TimerTest {
	Timer timer;

    public TimerTest(){
        timer = new Timer();
        timer.schedule(new TimerTaskTest(), 1000, 2000);
    }

    public static void main(String[] args) {
        new TimerTest();
    }
    public class TimerTaskTest extends TimerTask{

        @Override
        public void run() {
            Date date = new Date(this.scheduledExecutionTime());
            System.out.println("本次执行该线程的时间为：" + date);
        }
    }
}

