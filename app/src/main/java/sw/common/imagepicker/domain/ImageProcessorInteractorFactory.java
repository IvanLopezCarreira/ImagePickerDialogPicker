package sw.common.imagepicker.domain;

import sw.common.imagepicker.executor.MainThreadImpl;
import sw.common.imagepicker.executor.ThreadExecutor;

/**
 * Created by ivancarreira on 01/04/15.
 */
public class ImageProcessorInteractorFactory extends ImageProcessorFactory {
    @Override
    public ImageProcessor getProcessImage() {
        return new ImageProcessorInteractor(ThreadExecutor.getInstance(), MainThreadImpl.getInstance());
    }
}
