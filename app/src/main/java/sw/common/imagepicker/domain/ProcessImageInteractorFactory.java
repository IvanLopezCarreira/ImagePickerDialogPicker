package sw.common.imagepicker.domain;

import sw.common.imagepicker.executor.Executor;
import sw.common.imagepicker.executor.MainThread;
import sw.common.imagepicker.executor.MainThreadImpl;
import sw.common.imagepicker.executor.ThreadExecutor;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class ProcessImageInteractorFactory extends ProcessImageFactory{
    @Override
    public ProcessImage getProcessImage() {
        return new ProcessImageInteractor(ThreadExecutor.getInstance(), MainThreadImpl.getInstance());
    }
}
