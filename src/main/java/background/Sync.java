package background;

import weblearning.Courses;
import weblearning.Endpoints;

import java.util.Timer;
import java.util.TimerTask;

public class Sync {
    private static final long delay = 1000 * 60 * 5;
    private static Courses courseData;

    private static class CourseSync extends TimerTask {
        @Override public void run() {
            Endpoints.getCurriculum().thenAccept(courseData -> {
                Sync.courseData = courseData;
                schedule();
            });
        }
    }
    
    private final static Timer timer = new Timer("Sync");
    public static void sync() {
        schedule();
    }
    
    private static void schedule() {
        timer.schedule(new CourseSync(), delay);
    }
}
