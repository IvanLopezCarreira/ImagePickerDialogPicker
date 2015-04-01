package sw.common.imagepicker.executor;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class MainThreadImpl implements MainThread {
    private Handler handler;

    private static MainThreadImpl MAIN_THREAD_SINGLETON = null;

    public static MainThreadImpl getInstance() {
        if (MAIN_THREAD_SINGLETON == null) {
            createInstance();
        }

        return MAIN_THREAD_SINGLETON;
    }

    private synchronized static void createInstance() {
        if (MAIN_THREAD_SINGLETON == null) {
                MAIN_THREAD_SINGLETON = new MainThreadImpl();
        }
    }

    private MainThreadImpl() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void post(Runnable runnable) {
        this.handler.post(runnable);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
