package pl.gruszm.zephyrwork.callbacks;

import pl.gruszm.zephyrwork.enums.WorkSessionState;

public interface OnWorkSessionUpdateCallback
{
    void removeWorkSession(int workSessionId);
    void updateWorkSessionState(int workSessionId, WorkSessionState workSessionState);
    void updateNotesFromEmployee(int workSessionId, String notesFromEmployee);
    void updateNotesFromSupervisor(int workSessionId, String reason);
}
