public class Clock {
    int over_time = 0;
    public boolean timeOfProcess(int time, int timeOfProcess) {
        return time != timeOfProcess;
    }

    public boolean returnToQueue(int time) {
        return over_time == time;
    }
}
