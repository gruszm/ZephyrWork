package pl.gruszm.zephyrwork.callbacks;

import pl.gruszm.zephyrwork.enums.WorkSessionState;

public interface OnWorkSessionUpdateCallback
{
    void removeWorkSession(int workSessionId);
    void updateWorkSessionState(int workSessionId, WorkSessionState workSessionState);
}
