package sw.common.imagepicker.domain;

import sw.common.imagepicker.executor.Executor;
import sw.common.imagepicker.executor.MainThread;

/**
 * Created by ivancarreira on 01/04/15.
 */
abstract class ProcessImageFactory {

    public abstract ProcessImage getProcessImage();
}
