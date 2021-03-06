package io.cattle.platform.engine.handler;

import io.cattle.platform.engine.process.ProcessInstance;
import io.cattle.platform.engine.process.ProcessState;
import io.cattle.platform.util.type.Named;

public interface ProcessLogic extends Named {

    String[] getProcessNames();

    HandlerResult handle(ProcessState state, ProcessInstance process);

}
