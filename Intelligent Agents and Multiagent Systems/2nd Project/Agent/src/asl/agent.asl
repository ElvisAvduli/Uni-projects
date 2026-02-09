/* Initial beliefs */
max_carry(3).
stuck_count(0).
deadlock_count(0).
collection_attempts(0).
failed_task(none).
fail_count(0).
last_position(0, 0).
oscillation_count(0).
blocking_mode(false).
retreat_count(0).

// Known obstacles
obstacle(2,2).
obstacle(2,1).
obstacle(4,4).
obstacle(4,5).

// Negotiation state
in_negotiation(false).
current_bid(none, 0).

/* Initial goals */
!start.

/* Main control loop */
+!start : true
   <- .my_name(Me);
      .print("Agent ", Me, " started");
      !debug_beliefs;
      !achieve_all_goals.

+!debug_beliefs : true
   <- .print("=== Debugging Beliefs ===");
      if (position(X, Y)) {
         .print("Position: (", X, ",", Y, ")");
      } else {
         .print("ERROR: No position belief!");
      };
      if (table_at(TX, TY)) {
         .print("Table at: (", TX, ",", TY, ")");
      } else {
         .print("ERROR: No table_at belief!");
      };
      .findall(I, carrying_item(I), Items);
      .print("My Belief Inventory: ", Items);
      .print("=== End Debug ===").

+!achieve_all_goals : goal_complete(true)
   <- .print("=== SUCCESS! All goals achieved! ===");
      !wait_for_reset.

+!achieve_all_goals : true
   <- !negotiate_and_execute;
      .wait(100);
      !achieve_all_goals.

+!wait_for_reset : position(X,Y) & not colored(table) & not colored(chair) & not open(door)
   <- .my_name(Me);
      .print("Agent ", Me, " - Environment reset, starting new run...");
      -current_bid(_, _);
      +current_bid(none, 0);
      -in_negotiation(_);
      +in_negotiation(false);
      -stuck_count(_);
      +stuck_count(0);
      -deadlock_count(_);
      +deadlock_count(0);
      -collection_attempts(_);
      +collection_attempts(0);
      -failed_task(_);
      +failed_task(none);
      -fail_count(_);
      +fail_count(0);
      -last_position(_, _);
      +last_position(0, 0);
      -oscillation_count(_);
      +oscillation_count(0);
      -blocking_mode(_);
      +blocking_mode(false);
      -retreat_count(_);
      +retreat_count(0);
      !achieve_all_goals.

+!wait_for_reset : true
   <- .wait(100);
      !wait_for_reset.

/* NEGOTIATION & IDLE LOGIC */
+!negotiate_and_execute : true
   <- !identify_available_tasks(Tasks);
      .length(Tasks, L);
      if (L > 0) {
         !select_best_task(Tasks, BestTask, BestUtility);
         if (BestTask \== none) {
            !negotiate_task(BestTask, BestUtility);
         } else {
            .print("No valid tasks (all skipped). Force dropping and wandering...");
            !drop_all_items;
            !wander;
         };
      } else {
         !wander;
      }.

-!negotiate_and_execute : true
   <- .print("Negotiate and execute failed - waiting");
      .wait(500).

+!identify_available_tasks([]) : colored(table) & colored(chair) & open(door).

+!identify_available_tasks(Tasks) : true
   <- .findall(table, not colored(table), TableList);
      .findall(chair, not colored(chair), ChairList);
      .findall(door, not open(door), DoorList);
      .concat(TableList, ChairList, Temp);
      .concat(Temp, DoorList, Tasks).

+!select_best_task([], none, 0).

+!select_best_task([Task], Task, Utility) : not failed_task(Task)
   <- !estimate_utility(Task, Utility).

+!select_best_task([Task], none, 0) : failed_task(Task)
   <- .print("Skipping failed task: ", Task).

+!select_best_task([Task|Rest], BestTask, BestUtility) : failed_task(Task)
   <- .print("Skipping failed task: ", Task);
      !select_best_task(Rest, BestTask, BestUtility).

+!select_best_task([Task|Rest], BestTask, BestUtility) : not failed_task(Task)
   <- !estimate_utility(Task, Util1);
      !select_best_task(Rest, Task2, Util2);
      if (Util1 > Util2 & Task2 \== none) {
         BestTask = Task;
         BestUtility = Util1;
      } elif (Task2 == none) {
         BestTask = Task;
         BestUtility = Util1;
      } else {
         BestTask = Task2;
         BestUtility = Util2;
      }.

+!estimate_utility(table, 0) : other_agent_has(brush) | other_agent_has(color).
+!estimate_utility(chair, 0) : other_agent_has(brush) | other_agent_has(color).
+!estimate_utility(door, 0)  : other_agent_has(key)   | other_agent_has(code).

+!estimate_utility(table, Utility) : position(X, Y) & table_at(TX, TY) & carrying_item(brush) & carrying_item(color)
   <- Dist = math.abs(TX - X) + math.abs(TY - Y);
      Utility = 1.0 / (Dist + 1).

+!estimate_utility(table, Utility) : position(X, Y) & table_at(TX, TY)
   <- Dist = math.abs(TX - X) + math.abs(TY - Y);
      Utility = 1.0 / (Dist + 7).

+!estimate_utility(chair, Utility) : position(X, Y) & chair_at(CX, CY) & carrying_item(brush) & carrying_item(color)
   <- Dist = math.abs(CX - X) + math.abs(CY - Y);
      Utility = 1.0 / (Dist + 1).

+!estimate_utility(chair, Utility) : position(X, Y) & chair_at(CX, CY)
   <- Dist = math.abs(CX - X) + math.abs(CY - Y);
      Utility = 1.0 / (Dist + 7).

+!estimate_utility(door, Utility) : position(X, Y) & door_at(DX, DY) & carrying_item(key) & carrying_item(code)
   <- Dist = math.abs(DX - X) + math.abs(DY - Y);
      Utility = 0.8 / (Dist + 1).

+!estimate_utility(door, Utility) : position(X, Y) & door_at(DX, DY)
   <- Dist = math.abs(DX - X) + math.abs(DY - Y);
      Utility = 0.8 / (Dist + 7).

+!estimate_utility(Task, 0.001) : true.

+!negotiate_task(none, _) : true.

+!negotiate_task(Task, MyUtility) : MyUtility > 0.0
   <- .my_name(Me);
      .print("[", Me, "] Initiating negotiation for task: ", Task, " (utility: ", MyUtility, ")");
      -in_negotiation(_);
      +in_negotiation(true);
      -current_bid(_, _);
      +current_bid(Task, MyUtility);
      .broadcast(tell, proposal(Task, Me, MyUtility));
      .wait(500);
      !evaluate_proposals(Task, MyUtility).

+!negotiate_task(Task, MyUtility) : true
   <- !execute_task(Task).

+!evaluate_proposals(Task, MyUtility) : true
   <- .my_name(Me);
      .findall(proposal(Task, Agent, Utility), proposal(Task, Agent, Utility), Proposals);
      !find_best_proposal(Proposals, MyUtility, Me, Winner);
      .print("[", Me, "] Winner for task ", Task, ": ", Winner);
      if (Winner == Me) {
         .print("[", Me, "] I won task: ", Task);
         !execute_task(Task);
      } else {
         .print("[", Me, "] Lost task ", Task, " to ", Winner);
      };
      
      .abolish(proposal(Task, _, _));
      -in_negotiation(_);
      +in_negotiation(false);
      -current_bid(_, _);
      +current_bid(none, 0).

+!find_best_proposal([], MyUtility, Me, Me).

+!find_best_proposal([proposal(Task, Agent, Utility)|Rest], MyUtility, Me, Winner) : true
   <- !find_best_proposal(Rest, MyUtility, Me, TempWinner);
      if (Utility > MyUtility) {
         Winner = Agent;
      } elif (Utility == MyUtility & Agent == agent_1) {
         Winner = agent_1;
      } else {
         Winner = TempWinner;
      }.

+proposal(Task, Agent, Utility) : in_negotiation(true) & current_bid(Task, MyUtility)
   <- .my_name(Me);
      .print("[", Me, "] Received proposal from ", Agent, " for ", Task, " (utility: ", Utility, " vs mine: ", MyUtility, ")").

+proposal(Task, Agent, Utility) : not in_negotiation(true)
   <- .print("Received late proposal, ignoring").

+!execute_task(table) : not colored(table)
   <- -stuck_count(_); +stuck_count(0);
      -deadlock_count(_); +deadlock_count(0);
      -collection_attempts(_); +collection_attempts(0);
      -oscillation_count(_); +oscillation_count(0);
      -retreat_count(_); +retreat_count(0);
      !collect_painting_items;
      !paint_table.

+!execute_task(chair) : not colored(chair)
   <- -stuck_count(_); +stuck_count(0);
      -deadlock_count(_); +deadlock_count(0);
      -collection_attempts(_); +collection_attempts(0);
      -oscillation_count(_); +oscillation_count(0);
      -retreat_count(_); +retreat_count(0);
      !collect_painting_items;
      !paint_chair.

+!execute_task(door) : not open(door)
   <- -stuck_count(_); +stuck_count(0);
      -deadlock_count(_); +deadlock_count(0);
      -collection_attempts(_); +collection_attempts(0);
      -oscillation_count(_); +oscillation_count(0);
      -retreat_count(_); +retreat_count(0);
      !clean_inventory_for_door;
      !collect_door_items;
      !open_door.

+!execute_task(Task) : true
   <- .print("Task ", Task, " already completed or invalid").

-!execute_task(Task) : true
   <- .print("Failed to execute task ", Task, ", marking as failed");
      -collection_attempts(_); +collection_attempts(0);
      -failed_task(_);
      +failed_task(Task);
      ?fail_count(N);
      NewN = N + 1;
      -fail_count(_);
      +fail_count(NewN);
      .print("Task ", Task, " failed, fail_count = ", NewN);
      
      if (NewN >= 3) {
         .print("Multiple failures. FORCE dropping items, moving aside, and waiting...");
         !drop_all_items;
         !clear_area;
         .wait(2000);
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } else {
         !drop_all_items;
         !wander;
         .wait(1000);
      }.

+!drop_all_items : true
   <- .print("Force dropping all potential inventory to sync state.");
      !force_drop(brush);
      !force_drop(color);
      !force_drop(key);
      !force_drop(code).

+!force_drop(Item) : true
   <- drop(Item).

-!force_drop(Item) : true
   <- .print("Skipping drop of ", Item, " (not in inventory).").

+!clean_inventory_for_door : carrying(3) & colored(table) & colored(chair) & 
                              (carrying_item(brush) | carrying_item(color))
   <- .print("Cleaning painting items");
      !drop_painting_items.

+!clean_inventory_for_door : true.

+!drop_painting_items : carrying_item(brush)
   <- drop(brush); !drop_painting_items.

+!drop_painting_items : carrying_item(color)
   <- drop(color); !drop_painting_items.

+!drop_painting_items : true.

+!paint_table : colored(table)
   <- .print("Table already painted, skipping").

+!paint_table : table_at(TX, TY) & carrying_item(brush) & carrying_item(color)
   <- .print("Painting table at (", TX, ",", TY, ")");
      !navigate_to(TX, TY);
      ?position(X, Y);
      ?table_at(ActualTX, ActualTY);
      if (X == ActualTX & Y == ActualTY & not colored(table)) {
         paint(table);
         .print("Table painted successfully!");
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } elif (colored(table)) {
         .print("Table was painted while navigating");
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } else {
         .print("Not at correct position: at (", X, ",", Y, "), need (", ActualTX, ",", ActualTY, ")");
      }.

+!paint_table : true
   <- .print("Don't have painting items, collecting");
      !collect_painting_items;
      !paint_table.

-!paint_table : true
   <- .print("Failed to paint table, giving up this attempt").

+!paint_chair : colored(chair)
   <- .print("Chair already painted, skipping").

+!paint_chair : chair_at(CX, CY) & carrying_item(brush) & carrying_item(color)
   <- .print("Painting chair at (", CX, ",", CY, ")");
      !navigate_to(CX, CY);
      ?position(X, Y);
      ?chair_at(ActualCX, ActualCY);
      if (X == ActualCX & Y == ActualCY & not colored(chair)) {
         paint(chair);
         .print("Chair painted successfully!");
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } elif (colored(chair)) {
         .print("Chair was painted while navigating");
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } else {
         .print("Not at correct position: at (", X, ",", Y, "), need (", ActualCX, ",", ActualCY, ")");
      }.

+!paint_chair : true
   <- .print("Don't have painting items, collecting");
      !collect_painting_items;
      !paint_chair.

-!paint_chair : true
   <- .print("Failed to paint chair, giving up this attempt").

+!open_door : open(door)
   <- .print("Door already open, skipping").

+!open_door : door_at(DX, DY) & carrying_item(key) & carrying_item(code)
   <- .print("Opening door at (", DX, ",", DY, ")");
      !navigate_to(DX, DY);
      ?position(X, Y);
      ?door_at(ActualDX, ActualDY);
      if (X == ActualDX & Y == ActualDY & not open(door)) {
         open(door);
         .print("Door opened successfully!");
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } elif (open(door)) {
         .print("Door was opened while navigating");
         -failed_task(_);
         +failed_task(none);
         -fail_count(_);
         +fail_count(0);
      } else {
         .print("Not at correct position: at (", X, ",", Y, "), need (", ActualDX, ",", ActualDY, ")");
      }.

+!open_door : true
   <- .print("Don't have door items, collecting");
      !collect_door_items;
      !open_door.

-!open_door : true
   <- .print("Failed to open door, giving up this attempt").

+!collect_painting_items : other_agent_has(brush) & other_agent_has(color)
   <- .print("Other agent has both brush and color. I cannot complete this task.");
      .fail.

+!collect_painting_items : true
   <- !ensure_item(brush);
      !ensure_item(color).

+!collect_door_items : other_agent_has(key) & other_agent_has(code)
   <- .print("Other agent has both key and code. I cannot complete this task.");
      .fail.

+!collect_door_items : true
   <- !ensure_item(key);
      !ensure_item(code).

+!ensure_item(Item) : carrying_item(Item).

+!ensure_item(Item) : other_agent_has(Item)
   <- .print("FAILURE: Other agent has ", Item, ". I cannot complete this task.");
      .fail.

+!ensure_item(Item) : true
   <- !get_item(Item).

+!get_item(Item) : carrying_item(Item).

+!get_item(Item) : other_agent_has(Item)
   <- .print("Other agent already has ", Item, ", cannot get it");
      .fail.

+!get_item(Item) : item_at(Item, IX, IY) & carrying(N) & N < 3
   <- .print("Getting item: ", Item, " at (", IX, ",", IY, ")");
      !navigate_to(IX, IY);
      if (not other_agent_has(Item)) {
         pickup(Item);
      } else {
         .print("Other agent got ", Item, " first");
         .fail;
      }.

+!get_item(Item) : carrying(3)
   <- .print("Inventory full");
      !drop_least_important;
      !get_item(Item).

-!get_item(Item) : true
   <- .print("Failed to get ", Item).

/* NAVIGATION WITH BI-DIRECTIONAL DEADLOCK RESOLUTION */
+!navigate_to(TX, TY) : position(TX, TY)
   <- -stuck_count(_); +stuck_count(0);
      -oscillation_count(_); +oscillation_count(0);
      -retreat_count(_); +retreat_count(0).

/* Detect oscillation */
+!navigate_to(TX, TY) : position(X, Y) & last_position(X, Y) & oscillation_count(OC) & OC >= 2
   <- .print(" OSCILLATION DETECTED! Breaking pattern...");
      .my_name(Me);
      -oscillation_count(_);
      +oscillation_count(0);
      
      if (Me == agent_1) {
         .print("[", Me, "] Oscillation: Moving to safe zone and waiting");
         !move_to_safe_zone;
         .wait(1500);
      } else {
         .print("[", Me, "] Oscillation: Clearing area actively");
         !clear_area;
         .wait(800);
      };
      
      -stuck_count(_);
      +stuck_count(0);
      !navigate_to(TX, TY).

/* Too many deadlocks - give up on this goal */
+!navigate_to(TX, TY) : deadlock_count(DC) & DC >= 5
   <- .print("Too many deadlocks, giving up on this goal");
      -stuck_count(_);
      +stuck_count(0);
      -deadlock_count(_); 
      +deadlock_count(0);
      -oscillation_count(_);
      +oscillation_count(0);
      -retreat_count(_);
      +retreat_count(0);
      .my_name(Me);
      .random(R);
      WaitTime = 1000 + (R * 1500);
      !clear_area;
      .wait(WaitTime);
      .fail.

+!navigate_to(TX, TY) : stuck_count(SC) & SC >= 3
   <- .print(" DEADLOCK DETECTED! Stuck count: ", SC);
      ?deadlock_count(DC);
      NewDC = DC + 1;
      -deadlock_count(_);
      +deadlock_count(NewDC);
      -stuck_count(_);
      +stuck_count(0);
      
      /* Determine who is blocking */
      ?position(MyX, MyY);
      ?other_agent_at(OtherX, OtherY);
      
      /* Check if other agent is blocking my path */
      if (jia.FindPath(MyX, MyY, TX, TY, MyPath) & .length(MyPath, L) & L > 0) {
         .nth(0, MyPath, MyNextDir);
         !get_next_pos(MyX, MyY, MyNextDir, MyNextX, MyNextY);
         
         /* If other agent is at my next position, they are blocking */
         if (MyNextX == OtherX & MyNextY == OtherY) {
            .print(" Other agent is BLOCKING my path!");
            !request_blocking_agent_move_aside(OtherX, OtherY);
         } else {
            /* I might be blocking them, try to yield */
            .my_name(Me);
            if (Me == agent_1) {
               .print("[", Me, "] Not clearly blocking, yielding with wait");
               !retreat_steps(2);
               .wait(1000);
            } else {
               .print("[", Me, "] Not clearly blocking, clearing area");
               !clear_area_steps(2);
               .wait(600);
            };
         };
      };
      
      !navigate_to(TX, TY).

/* Request blocking agent to move aside with verification */
+!request_blocking_agent_move_aside(BlockerX, BlockerY) : true
   <- .my_name(Me);
      .print("[", Me, "] Broadcasting: MOVE_ASIDE request to agent at (", BlockerX, ",", BlockerY, ")");
      
      /* Store pre-move position */
      ?other_agent_at(PreX, PreY);
      
      .broadcast(tell, please_move_aside(Me, BlockerX, BlockerY));
      .wait(1000); 
      
      /* Verify if blocking agent actually moved */
      ?other_agent_at(PostX, PostY);
      if (PostX == PreX & PostY == PreY) {
         .print("[", Me, "] Blocking agent couldn't move or is stuck. I will retreat instead.");
         !retreat_and_wait_for_clearance;
      } else {
         .print("[", Me, "] Blocking agent moved! Continuing...");
      }.

/* Prevent simultaneous blocking mode */
+please_move_aside(RequesterAgent, MyExpectedX, MyExpectedY) : 
   blocking_mode(true)
   <- .my_name(Me);
      .print("[", Me, "] Already in blocking mode - ignoring duplicate request");
      .abolish(please_move_aside(RequesterAgent, _, _)).

/* Handle move-aside request (when I am the blocking agent) */
+please_move_aside(RequesterAgent, MyExpectedX, MyExpectedY) : 
   position(MyX, MyY) & MyX == MyExpectedX & MyY == MyExpectedY & not blocking_mode(true)
   <- .my_name(Me);
      .print("[", Me, "]  Received MOVE_ASIDE request from ", RequesterAgent);
      .print("[", Me, "] I am the blocking agent. Moving aside (3 steps)...");
      
      -blocking_mode(_);
      +blocking_mode(true);
      
      /* Try to move aside 3 steps */
      !move_aside_steps(3);
      
      -blocking_mode(_);
      +blocking_mode(false);
      
      .print("[", Me, "] Finished moving aside. Requester can pass now.");
      .abolish(please_move_aside(RequesterAgent, _, _)).

+please_move_aside(RequesterAgent, X, Y) : true
   <- .print("Received move-aside request but I'm not at blocking position. Ignoring.");
      .abolish(please_move_aside(RequesterAgent, _, _)).

/* Move aside with perpendicular priority */
+!move_aside_steps(0) : true
   <- .print(" Completed move-aside steps").

+!move_aside_steps(N) : N > 0 & position(X, Y) & other_agent_at(OX, OY)
   <- .print(" Move-aside step ", (4 - N), "/3");
      
      /* Calculate away direction */
      DX = X - OX;
      DY = Y - OY;
      
      /* Check if we have perpendicular space first */
      !has_perpendicular_space(X, Y, DX, DY, HasSpace);
      
      if (HasSpace) {
         /* Perpendicular move (better for corridors) */
         !try_perpendicular_move(DX, DY);
      } else {
         /* Fall back to away movement */
         !move_away_one_step(DX, DY);
      };
      
      .wait(250); 
      !move_aside_steps(N - 1).

-!move_aside_steps(N) : N > 0
   <- .print(" Failed to complete all move-aside steps");
      !move_aside_steps(0).

/* Check if perpendicular space exists */
+!has_perpendicular_space(X, Y, DX, DY, true) : DX == 0 & 
   ((X < 5 & not other_agent_at(X+1, Y) & not obstacle(X+1, Y)) |
    (X > 1 & not other_agent_at(X-1, Y) & not obstacle(X-1, Y))).
    
+!has_perpendicular_space(X, Y, DX, DY, true) : DY == 0 &
   ((Y < 5 & not other_agent_at(X, Y+1) & not obstacle(X, Y+1)) |
    (Y > 1 & not other_agent_at(X, Y-1) & not obstacle(X, Y-1))).
    
+!has_perpendicular_space(_, _, _, _, false).

/* Try perpendicular movement */
+!try_perpendicular_move(DX, DY) : DX == 0 & position(X, Y)
   <- /* Same column - move horizontally */
      if (X < 5 & not other_agent_at(X+1, Y) & not obstacle(X+1, Y)) {
         move(right);
      } elif (X > 1 & not other_agent_at(X-1, Y) & not obstacle(X-1, Y)) {
         move(left);
      } else {
         .fail;
      }.

+!try_perpendicular_move(DX, DY) : DY == 0 & position(X, Y)
   <- /* Same row - move vertically */
      if (Y < 5 & not other_agent_at(X, Y+1) & not obstacle(X, Y+1)) {
         move(up);
      } elif (Y > 1 & not other_agent_at(X, Y-1) & not obstacle(X, Y-1)) {
         move(down);
      } else {
         .fail;
      }.

-!try_perpendicular_move(DX, DY) : true
   <- /* Perpendicular failed, try diagonal */
      !move_away_one_step(DX, DY).

/* Retreat and wait with abort condition */
+!retreat_and_wait_for_clearance : true
   <- .my_name(Me);
      .print("[", Me, "] 🔙 RETREATING - blocking agent is stuck");
      
      ?retreat_count(RC);
      if (RC >= 3) { 
         .print("[", Me, "] Too many retreats - full abort and spatial separation");
         !move_to_safe_zone;
         .wait(2000);
         -retreat_count(_);
         +retreat_count(0);
         .fail;  /* Give up on current goal */
      } else {
         !retreat_steps(4); 
         
         .print("[", Me, "]  Waiting for blocking agent to clear area...");
         .wait(1500);
         
         .print("[", Me, "]  Resuming navigation after retreat.");
      }.

/* Retreat N steps backwards */
+!retreat_steps(0) : true
   <- .print(" Retreat complete").

+!retreat_steps(N) : N > 0
   <- .print(" Retreat step ", (5 - N), "/4");
      ?retreat_count(RC);
      -retreat_count(_);
      +retreat_count(RC + 1);
      
      ?position(X, Y);
      ?last_position(LX, LY);
      
      /* Move away from previous position */
      BackDX = LX - X;
      BackDY = LY - Y;
      
      if (BackDX \== 0 | BackDY \== 0) {
         !move_away_one_step(BackDX, BackDY);
      } else { 
         !wander;
      };
      
      .wait(250);  
      !retreat_steps(N - 1).

-!retreat_steps(N) : N > 0
   <- .print(" Failed retreat step");
      !retreat_steps(N - 1).

/* Clear area with limited steps */
+!clear_area_steps(0) : true
   <- .print(" Clear area complete").

+!clear_area_steps(N) : N > 0 & position(X, Y) & other_agent_at(OX, OY)
   <- .print(" Clear area step ", (3 - N), "/2");
      DX = X - OX;
      DY = Y - OY;
      !move_away_one_step(DX, DY);
      .wait(200);
      !clear_area_steps(N - 1).

-!clear_area_steps(N) : N > 0
   <- !clear_area_steps(N - 1).

/* Move away one step (helper) */
+!move_away_one_step(DX, DY) : DX > 0 & position(X, Y) & not other_agent_at(X+1, Y) & not obstacle(X+1, Y) & X < 5
   <- move(right).

+!move_away_one_step(DX, DY) : DX < 0 & position(X, Y) & not other_agent_at(X-1, Y) & not obstacle(X-1, Y) & X > 1
   <- move(left).

+!move_away_one_step(DX, DY) : DY > 0 & position(X, Y) & not other_agent_at(X, Y+1) & not obstacle(X, Y+1) & Y < 5
   <- move(up).

+!move_away_one_step(DX, DY) : DY < 0 & position(X, Y) & not other_agent_at(X, Y-1) & not obstacle(X, Y-1) & Y > 1
   <- move(down).

+!move_away_one_step(DX, DY) : position(X, Y) & not other_agent_at(X, Y+1) & not obstacle(X, Y+1) & Y < 5
   <- move(up).

+!move_away_one_step(DX, DY) : position(X, Y) & not other_agent_at(X, Y-1) & not obstacle(X, Y-1) & Y > 1
   <- move(down).

+!move_away_one_step(DX, DY) : position(X, Y) & not other_agent_at(X+1, Y) & not obstacle(X+1, Y) & X < 5
   <- move(right).

+!move_away_one_step(DX, DY) : position(X, Y) & not other_agent_at(X-1, Y) & not obstacle(X-1, Y) & X > 1
   <- move(left).

+!move_away_one_step(DX, DY) : true
   <- .wait(200).

/* Get next position given direction (helper) */
+!get_next_pos(X, Y, "up", X, NY) : true <- NY = Y + 1.
+!get_next_pos(X, Y, "down", X, NY) : true <- NY = Y - 1.
+!get_next_pos(X, Y, "left", NX, Y) : true <- NX = X - 1.
+!get_next_pos(X, Y, "right", NX, Y) : true <- NX = X + 1.
+!get_next_pos(X, Y, _, X, Y) : true.

/* NORMAL NAVIGATION (when not in deadlock) */
+!navigate_to(TX, TY) : position(X, Y)
   <- /* Track position for oscillation detection */
      ?last_position(LX, LY);
      if (X == LX & Y == LY) {
         ?oscillation_count(OC);
         -oscillation_count(_);
         +oscillation_count(OC + 1);
      } else {
         -oscillation_count(_);
         +oscillation_count(0);
      };
      -last_position(_, _);
      +last_position(X, Y);
      
      /* Find path and move */
      jia.FindPath(X, Y, TX, TY, Path);
      .length(Path, L);
      if (L > 0) {
         .nth(0, Path, Dir);
         !try_move(Dir);
      };
      !navigate_to(TX, TY).

-!navigate_to(TX, TY) : true
   <- ?stuck_count(SC);
      -stuck_count(_);
      +stuck_count(SC + 1);
      .wait(150);
      !navigate_to(TX, TY).

/* MOVEMENT WITH COLLISION DETECTION */
+!try_move(up) : position(X, Y) & other_agent_at(X, Y+1)
   <- ?stuck_count(SC);
      -stuck_count(_);
      +stuck_count(SC + 1);
      .wait(150);
      .fail.

+!try_move(up) : position(X, Y) & not obstacle(X, Y+1) & Y < 5
   <- move(up); -stuck_count(_); +stuck_count(0).

+!try_move(down) : position(X, Y) & other_agent_at(X, Y-1)
   <- ?stuck_count(SC);
      -stuck_count(_);
      +stuck_count(SC + 1);
      .wait(150);
      .fail.

+!try_move(down) : position(X, Y) & not obstacle(X, Y-1) & Y > 1
   <- move(down); -stuck_count(_); +stuck_count(0).

+!try_move(left) : position(X, Y) & other_agent_at(X-1, Y)
   <- ?stuck_count(SC);
      -stuck_count(_);
      +stuck_count(SC + 1);
      .wait(150);
      .fail.

+!try_move(left) : position(X, Y) & not obstacle(X-1, Y) & X > 1
   <- move(left); -stuck_count(_); +stuck_count(0).

+!try_move(right) : position(X, Y) & other_agent_at(X+1, Y)
   <- ?stuck_count(SC);
      -stuck_count(_);
      +stuck_count(SC + 1);
      .wait(150);
      .fail.

+!try_move(right) : position(X, Y) & not obstacle(X+1, Y) & X < 5
   <- move(right); -stuck_count(_); +stuck_count(0).

+!try_move(Dir) : true
   <- .fail.

/* FALLBACK SPATIAL SEPARATION STRATEGIES */
+!clear_area : position(X, Y) & other_agent_at(OX, OY)
   <- .my_name(Me);
      .print("[", Me, "] Clearing area - moving away from other agent at (", OX, ",", OY, ")");
      
      DX = X - OX;
      DY = Y - OY;
      
      /* If on same row/column, force perpendicular movement */
      if (Y == OY) {
         .print("[", Me, "] Same row detected! Moving vertically first");
         !escape_horizontal_deadlock(Me);
      } elif (X == OX) {
         .print("[", Me, "] Same column detected! Moving horizontally first");
         !escape_vertical_deadlock(Me);
      } else {
         !move_away_one_step(DX, DY);
         .wait(200);
         !move_away_one_step(DX, DY);
         .wait(200);
         !move_away_one_step(DX, DY);
      }.

+!clear_area : true
   <- .print("No other agent detected, wandering");
      !wander.

+!escape_horizontal_deadlock(agent_1) : position(X, Y) & Y < 5 & not other_agent_at(X, Y+1) & not obstacle(X, Y+1)
   <- .print("[agent_1] Escaping horizontal deadlock by moving UP");
      move(up);
      .wait(200);
      if (not other_agent_at(X+1, Y+1) & not obstacle(X+1, Y+1) & X < 5) {
         move(right);
      } elif (not other_agent_at(X-1, Y+1) & not obstacle(X-1, Y+1) & X > 1) {
         move(left);
      }.

+!escape_horizontal_deadlock(agent_1) : position(X, Y) & Y > 1 & not other_agent_at(X, Y-1) & not obstacle(X, Y-1)
   <- .print("[agent_1] Escaping horizontal deadlock by moving DOWN");
      move(down);
      .wait(200);
      if (not other_agent_at(X+1, Y-1) & not obstacle(X+1, Y-1) & X < 5) {
         move(right);
      } elif (not other_agent_at(X-1, Y-1) & not obstacle(X-1, Y-1) & X > 1) {
         move(left);
      }.

+!escape_horizontal_deadlock(agent_2) : position(X, Y) & Y > 1 & not other_agent_at(X, Y-1) & not obstacle(X, Y-1)
   <- .print("[agent_2] Escaping horizontal deadlock by moving DOWN");
      move(down);
      .wait(200);
      if (not other_agent_at(X+1, Y-1) & not obstacle(X+1, Y-1) & X < 5) {
         move(right);
      } elif (not other_agent_at(X-1, Y-1) & not obstacle(X-1, Y-1) & X > 1) {
         move(left);
      }.

+!escape_horizontal_deadlock(agent_2) : position(X, Y) & Y < 5 & not other_agent_at(X, Y+1) & not obstacle(X, Y+1)
   <- .print("[agent_2] Escaping horizontal deadlock by moving UP");
      move(up);
      .wait(200);
      if (not other_agent_at(X+1, Y+1) & not obstacle(X+1, Y+1) & X < 5) {
         move(right);
      } elif (not other_agent_at(X-1, Y+1) & not obstacle(X-1, Y+1) & X > 1) {
         move(left);
      }.

+!escape_horizontal_deadlock(Me) : true
   <- .print("[", Me, "] Cannot escape horizontally, wandering");
      !wander.

+!escape_vertical_deadlock(agent_1) : position(X, Y) & X < 5 & not other_agent_at(X+1, Y) & not obstacle(X+1, Y)
   <- .print("[agent_1] Escaping vertical deadlock by moving RIGHT");
      move(right);
      .wait(200);
      if (not other_agent_at(X+1, Y+1) & not obstacle(X+1, Y+1) & Y < 5) {
         move(up);
      } elif (not other_agent_at(X+1, Y-1) & not obstacle(X+1, Y-1) & Y > 1) {
         move(down);
      }.

+!escape_vertical_deadlock(agent_1) : position(X, Y) & X > 1 & not other_agent_at(X-1, Y) & not obstacle(X-1, Y)
   <- .print("[agent_1] Escaping vertical deadlock by moving LEFT");
      move(left);
      .wait(200);
      if (not other_agent_at(X-1, Y+1) & not obstacle(X-1, Y+1) & Y < 5) {
         move(up);
      } elif (not other_agent_at(X-1, Y-1) & not obstacle(X-1, Y-1) & Y > 1) {
         move(down);
      }.

+!escape_vertical_deadlock(agent_2) : position(X, Y) & X > 1 & not other_agent_at(X-1, Y) & not obstacle(X-1, Y)
   <- .print("[agent_2] Escaping vertical deadlock by moving LEFT");
      move(left);
      .wait(200);
      if (not other_agent_at(X-1, Y+1) & not obstacle(X-1, Y+1) & Y < 5) {
         move(up);
      } elif (not other_agent_at(X-1, Y-1) & not obstacle(X-1, Y-1) & Y > 1) {
         move(down);
      }.

+!escape_vertical_deadlock(agent_2) : position(X, Y) & X < 5 & not other_agent_at(X+1, Y) & not obstacle(X+1, Y)
   <- .print("[agent_2] Escaping vertical deadlock by moving RIGHT");
      move(right);
      .wait(200);
      if (not other_agent_at(X+1, Y+1) & not obstacle(X+1, Y+1) & Y < 5) {
         move(up);
      } elif (not other_agent_at(X+1, Y-1) & not obstacle(X+1, Y-1) & Y > 1) {
         move(down);
      }.

+!escape_vertical_deadlock(Me) : true
   <- .print("[", Me, "] Cannot escape vertically, wandering");
      !wander.

+!move_to_safe_zone : position(X, Y) & X <= 2 & Y <= 2
   <- .print("Moving to zone: top-right");
      !navigate_to(5, 5).

+!move_to_safe_zone : position(X, Y) & X >= 4 & Y >= 4
   <- .print("Moving to zone: bottom-left");
      !navigate_to(1, 1).

+!move_to_safe_zone : position(X, Y) & X <= 2
   <- .print("Moving to zone: right side");
      !navigate_to(5, Y).

+!move_to_safe_zone : position(X, Y) & X >= 4
   <- .print("Moving to zone: left side");
      !navigate_to(1, Y).

+!move_to_safe_zone : true
   <- !wander.

-!move_to_safe_zone : true
   <- !wander.

+!wander : position(X, Y)
   <- .random(R);
      if (R < 0.25 & not other_agent_at(X+1, Y) & not obstacle(X+1, Y) & X < 5) { 
         move(right); 
      } elif (R < 0.5 & not other_agent_at(X-1, Y) & not obstacle(X-1, Y) & X > 1) { 
         move(left); 
      } elif (R < 0.75 & not other_agent_at(X, Y-1) & not obstacle(X, Y-1) & Y > 1) { 
         move(down); 
      } elif (not other_agent_at(X, Y+1) & not obstacle(X, Y+1) & Y < 5) { 
         move(up); 
      } else { 
         .wait(200); 
      }.

+!wander : true
   <- .wait(200).

-!wander : true
   <- .print("Wander failed - all directions blocked, waiting");
      .wait(300).

/* UTILITY FUNCTIONS */
+!drop_least_important : carrying_item(brush) & colored(table) & colored(chair)
   <- drop(brush).

+!drop_least_important : carrying_item(color) & colored(table) & colored(chair)
   <- drop(color).

+!drop_least_important : carrying_item(key) & open(door)
   <- drop(key).

+!drop_least_important : carrying_item(code) & open(door)
   <- drop(code).

+!drop_least_important : carrying_item(Item)
   <- drop(Item).

+!drop_least_important : true.