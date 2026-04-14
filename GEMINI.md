# Entities
## Apartments
- Fields:
    - *AirbnbID*: Name of the apartment in Airbnb. It has to match EXACTLY. Used for the import functionality
    - *BookingID*: Same, but for booking
    - *States*:
        - **Ready** : The apartment is ready to use. No action required
        - **Occupied**: Something (an event) is using the apartment
        - **Used**: After an event, the apartment passes to this state. It needs to be cleaned to go to Ready again.
    - *Visible*: This will make the apartment not appear in , for example, the selector of the apartment when creating a new booking. Useful when your apartment is going to be out of service for a long time, or when you no longer have the apartment property. The records associated will still exists (past bookings, assignments...)
- The state cannot be modified directly. It's only affected by the events

## Workers
- Fields:
    - *Language*: Useful to generate instructions translated in their language
    - *Visible*: Same as in apartment. Useful for holidays or long sick leaves.

## Tasks
- Fields:
    - *Duration*: Aprox. minutes that will take the task. Makes easier to schedule: when assigning this task, after selecting the start date and time, the end date will be calculated adding this duration to the start date.
    - *Steps*:  List of the steps required to complete the task. Used when generating instructions for new workers.
    - *Type*:  There are 2 types of tasks:

| **Type**  | **Description**                                                | **Needed to be completed to switch the apartment back to "ready" after every event** |
| --------- | -------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| Mandatory | Regular task, like cleaning. Needs                             | YES                                                                                  |
| Extra     | Not needed every time, just when host schedule them, on demand | ONLY IF SCHEDULED                                                                    |
## Templates
Templates have the same fields than tasks. They are used to store information as  starting point, and then creating the tasks from there, only modifying the specific parts.
## Events
Events that can modify the apartment state.
- Fields:
    - *Type*: Type of the task, there are 3:

| **Type**     | **Description**                                                                                                                 | **Needs a clean apartment to start?** |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| Booking      | Normal booking for a client                                                                                                     | YES                                   |
| Personal use | Personal use for the host                                                                                                       | YES                                   |
| Mainteinance | Activity that will take the apartment and dirty it. (For example, a plumber that needs to fix the sink during a couple of days) | NO                                    |
- *Source*: Used only for Bookings. Where the booking was originated (Airbnb, Booking or None, if the booking was something managed outside the platforms)
- *States*:
  - **Pending** : Booking not started yet.
  - **In progress**: Booking currently active
  - **Finished**: Booking finished

## Assignments
An assignment is the instruction given to a worker to do a task in a specific date and time.
- Fields:
    - *States*:
        - **Pending** : Assignment not done yet
        - **Finished**: Assignment done


# Workflow
### General workflow
1. The *initial state* of the apartment when created is **READY**
2. When *one of the events* associated with the apartment *starts*, and the event state switches to **IN PROGRESS**, the apartment state passes to **OCCUPIED**
3. Once that *event finishes* (state **FINISHED**), the apartment state goes to **USED** (except if the apartment *does not have tasks associated*, in that case it goes directly to **READY** automatically)
4. To *take back the apartment state to READY*, we should:
    1. *Create assignments* for all the *Mandatory tasks*.
    2. (*Optional*) *Create assignments* for the *extra tasks* (For example, if we should check the toilet paper because it has been a long time since the last time, we assign that task)
    3. Once the *worker has done the job*, we *switch the assignments state* to **FINISHED**. Each time we *change an assignment state*, the *apartment state is recalculated*:
        1. If we *dont have* an *assignment completed* for *each mandatory task* of the apartment, then we *keep* the **USED** state.
        2. If we *have an assignment completed for each mandatory task*, then we *check* if we have any *extra task scheduled* for this booking.
            1. If *not all the scheduled extra tasks* for this booking are  *completed*, then we *keep* the state as **USED**.
            2. If *all the scheduled extra tasks* for this booking are *completed too*, then the state of the apartment *switches* to **READY**

### Considerations
- Bookings are used for regular bookings for clients
- Personal use events are used when, for example, you are lending to a friend the apartment for a weekend. It wil behave like any other booking, the only difference is that it won't show in the Booking section, but in the events section.
- Maintenance events are used for situations where the apartment will need a renovation or something that will dirty the apartment. The only difference with Bookings and Personal use evens is that the system won't alert you to clean the apartment before this event is going to happen (you dont really need to clean the apartment for a plumber to come and work)

### How modifications in entities affect the workflow?
#### Events
- Change an event to IN_PROGRESS: The apartment state passes to OCCUPIED.
- Change an event to FINISHED: The apartment state passes to USED
- Change an event to PENDING: The apartment state passes to READY
- Delete an event IN PROGRESS: The apartment passes to READY
- Delete an event FINISHED: If the event was the last one finished, the apartment state will be:
    - If the previous event was cleaned up: READY
    - If the previous event was not cleaned up (we let the client into a dirty apartment lol): USED
#### Tasks
- Change a mandatory task to be extra: If there is an apartment that had all the required tasks except this one to complete the clean up, it will be automatically passed to READY (now its not required anymore, so the apartment is good to go)
- Delete a mandatory task: Same behavior as the previous modification
#### Assignments
- Update an assignment state from FINISHED to PENDING: The apartment state will go back to USED, as the required task is no longer completed
- Delete a mandatory task assignment: Same as before.
- Delete a FINISHED extra task assignment: Nothing happens
- Delete a PENDING extra task assignment: If the apartment had all the required tasks completed, then the apartment state passes to READY automatically

### Scheduling restrictions
#### Events
- For the same apartment, you cannot create events at the time that other events or assignments are scheduled
- For the same apartment, we cannot have more than one event IN PROGRESS
- For the same apartment, we cannot have PENDING events later than FINSHED or IN PROGRESS events (if the previous ones are finished or in progress, this one that is scheduled to a sooner date should be finished too )
- For the same apartment, we cannot have FINISHED or IN PROGRESS events previous to PENDING event (viceversa than previous rule )
#### Assignments
- For the same apartment, you cannot create assignments at the time that other events or assignments are scheduled
- You cannot change or delete assignments from past events. Only allowed for the current IN PROGRESS event or if there is no event IN PROGRESS, then we can modify the assignments only for the last finished event.
- You cannot complete assignments for events that are not finished yet. (Wait until the client leaves to clean the room)
- The worker in charge of the assignment cannot have another assignment scheduled at the same time
- Cannot create more than one assignment for one task per event. (Cannot clean the room twice)
- Cannot schedule an assignment that starts before the event ends (why to clean a room that is not dirty yet?) and cannot finish after the next event start (we need to clean the rooms on time for the next client)

#### 
#### 