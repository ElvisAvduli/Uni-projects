/* Initial beliefs */
max_carry(3).

// Known obstacles
obstacle(2,2).
obstacle(2,1).
obstacle(4,4).
obstacle(4,5).

// Current navigation target
current_target(none, 0, 0).
navigating(false).

/* Initial goals */
!start.

/* Main control loop */
+!start : true
   <- .print("Agent started");
      !achieve_all_goals.

+!achieve_all_goals : goal_complete(true)
   <- ?total_reward(R);
      ?steps(S);
      .print("=== SUCCESS! All goals achieved! ===");
      .print("Utility: ", R, " | Steps: ", S);
      !wait_for_reset.

+!achieve_all_goals : true
   <- !select_and_execute_goal;
      !achieve_all_goals.

/* Handle environment changes - simple notification */
+environment_changed : true
   <- .print("WARNING: Environment changed! Will adapt on next goal selection...");
      -environment_changed.

/* Wait for environment reset */
+!wait_for_reset : position(1,1) & not colored(table) & not colored(chair) & not open(door)
   <- .print("Environment reset detected, starting new run...");
      -current_target(_, _, _);
      +current_target(none, 0, 0);
      -navigating(_);
      +navigating(false);
      !achieve_all_goals.

+!wait_for_reset : true
   <- .wait(100);
      !wait_for_reset.

/* Distance-aware goal selection */
+!select_and_execute_goal : not colored(table) & can_paint_table
   <- !calculate_best_goal;
      !execute_best_goal.

+!select_and_execute_goal : not colored(chair) & can_paint_chair
   <- !calculate_best_goal;
      !execute_best_goal.

+!select_and_execute_goal : not open(door) & can_open_door
   <- !open_door.

+!select_and_execute_goal : not colored(table) & not can_paint_table
   <- !collect_painting_items;
      !select_and_execute_goal.

+!select_and_execute_goal : not colored(chair) & not can_paint_chair
   <- !collect_painting_items;
      !select_and_execute_goal.

+!select_and_execute_goal : not open(door) & not can_open_door
   <- !clean_inventory_smart;
      !collect_door_items;
      !select_and_execute_goal.

+!select_and_execute_goal : goal_complete(true)
   <- .print("All goals completed!").

+!select_and_execute_goal : true
   <- .print("ERROR: No applicable plan found");
      ?position(X,Y);
      ?carrying(N);
      .print("Position: (", X, ",", Y, "), Carrying: ", N);
      .print("Attempting recovery...");
      !clean_inventory_smart;
      !select_and_execute_goal.

/* Calculate best goal based on distance and reward */
+!calculate_best_goal : position(X, Y)
   <- jia.calculate_goal_utilities(X, Y, BestGoal);
      -best_goal(_);
      +best_goal(BestGoal);
      .print("Best goal selected: ", BestGoal).

+!execute_best_goal : best_goal(table)
   <- !paint_table.

+!execute_best_goal : best_goal(chair)
   <- !paint_chair.

+!execute_best_goal : best_goal(none)
   <- .print("No executable goal available").

/* Smart inventory cleaning */
+!clean_inventory_smart : carrying_item(brush) & colored(table) & colored(chair)
   <- .print("Dropping brush (no longer needed)");
      drop(brush);
      !clean_inventory_smart.

+!clean_inventory_smart : carrying_item(color) & colored(table) & colored(chair)
   <- .print("Dropping color (no longer needed)");
      drop(color);
      !clean_inventory_smart.

+!clean_inventory_smart : carrying_item(key) & open(door)
   <- .print("Dropping key (no longer needed)");
      drop(key);
      !clean_inventory_smart.

+!clean_inventory_smart : carrying_item(code) & open(door)
   <- .print("Dropping code (no longer needed)");
      drop(code);
      !clean_inventory_smart.

+!clean_inventory_smart : carrying(3) & colored(table) & colored(chair) & 
                          (carrying_item(brush) | carrying_item(color)) & 
                          not open(door)
   <- .print("Dropping painting items to make room for door items");
      !drop_painting_items.

+!clean_inventory_smart : true.

+!drop_painting_items : carrying_item(brush)
   <- drop(brush);
      !drop_painting_items.

+!drop_painting_items : carrying_item(color)
   <- drop(color);
      !drop_painting_items.

+!drop_painting_items : true.

/* Paint table - always get fresh position */
+!paint_table : table_at(TX,TY) & carrying_item(brush) & carrying_item(color)
   <- .print("Painting table at (", TX, ",", TY, ")");
      -current_target(_, _, _);
      +current_target(table, TX, TY);
      !navigate_to(TX,TY);
      -navigating(_);
      +navigating(false);
      ?table_at(NewTX, NewTY);
      ?position(MyX, MyY);
      if (MyX == NewTX & MyY == NewTY) {
         paint(table);
      } else {
         .print("Target moved, retrying...");
         !paint_table;
      }.

+!paint_table : true
   <- !collect_painting_items.

/* Paint chair - always get fresh position */
+!paint_chair : chair_at(CX,CY) & carrying_item(brush) & carrying_item(color)
   <- .print("Painting chair at (", CX, ",", CY, ")");
      -current_target(_, _, _);
      +current_target(chair, CX, CY);
      !navigate_to(CX,CY);
      -navigating(_);
      +navigating(false);
      ?chair_at(NewCX, NewCY);
      ?position(MyX, MyY);
      if (MyX == NewCX & MyY == NewCY) {
         paint(chair);
      } else {
         .print("Target moved, retrying...");
         !paint_chair;
      }.

+!paint_chair : true
   <- !collect_painting_items.

/* Open door - always get fresh position */
+!open_door : door_at(DX,DY) & carrying_item(key) & carrying_item(code)
   <- .print("Opening door at (", DX, ",", DY, ")");
      -current_target(_, _, _);
      +current_target(door, DX, DY);
      !navigate_to(DX,DY);
      -navigating(_);
      +navigating(false);
      ?door_at(NewDX, NewDY);
      ?position(MyX, MyY);
      if (MyX == NewDX & MyY == NewDY) {
         open(door);
      } else {
         .print("Target moved, retrying...");
         !open_door;
      }.

+!open_door : true
   <- !collect_door_items.

/* Collect items for painting */
+!collect_painting_items : not carrying_item(brush)
   <- !get_item(brush);
      !collect_painting_items.

+!collect_painting_items : not carrying_item(color)
   <- !get_item(color);
      !collect_painting_items.

+!collect_painting_items : carrying_item(brush) & carrying_item(color).

/* Collect items for door */
+!collect_door_items : carrying(3) & (carrying_item(brush) | carrying_item(color)) & 
                       colored(table) & colored(chair)
   <- .print("Inventory full, cleaning painting items first");
      !drop_painting_items;
      !collect_door_items.

+!collect_door_items : not carrying_item(key)
   <- !get_item(key);
      !collect_door_items.

+!collect_door_items : not carrying_item(code)
   <- !get_item(code);
      !collect_door_items.

+!collect_door_items : carrying_item(key) & carrying_item(code).

/* Get specific item */
+!get_item(Item) : carrying_item(Item).

+!get_item(Item) : item_at(Item,IX,IY) & carrying(N) & N < 3
   <- !navigate_to(IX,IY);
      pickup(Item).

+!get_item(Item) : carrying(3)
   <- .print("Inventory full, dropping least important item");
      !drop_least_important_item;
      !get_item(Item).

+!get_item(Item) : true
   <- .print("Cannot get item ", Item).

/* Drop least important item */
+!drop_least_important_item : carrying_item(brush) & colored(table) & colored(chair)
   <- drop(brush).

+!drop_least_important_item : carrying_item(color) & colored(table) & colored(chair)
   <- drop(color).

+!drop_least_important_item : carrying_item(key) & open(door)
   <- drop(key).

+!drop_least_important_item : carrying_item(code) & open(door)
   <- drop(code).

+!drop_least_important_item : not colored(table) & not colored(chair) & 
                              (carrying_item(key) | carrying_item(code))
   <- .print("Need painting, dropping door item");
      !drop_door_item.

+!drop_least_important_item : colored(table) & colored(chair) & not open(door) &
                              (carrying_item(brush) | carrying_item(color))
   <- .print("Need door items, dropping painting item");
      !drop_painting_items.

+!drop_least_important_item : carrying_item(Item)
   <- .print("Dropping arbitrary item: ", Item);
      drop(Item).

+!drop_least_important_item : true.

+!drop_door_item : carrying_item(key)
   <- drop(key).

+!drop_door_item : carrying_item(code)
   <- drop(code).

+!drop_door_item : true.

/* Navigate using A* pathfinding */
+!navigate_to(TX,TY) : position(TX,TY).

+!navigate_to(TX,TY) : position(X,Y)
   <- -navigating(_);
      +navigating(true);
      jia.get_astar_path(X,Y,TX,TY,Path);
      !process_path(Path, TX, TY).

/* Process path */
+!process_path([], TX, TY) : true
   <- .print("ERROR: No path found to (", TX, ",", TY, ")");
      ?position(X,Y);
      .print("Current position: (", X, ",", Y, ")");
      -navigating(_);
      +navigating(false);
      !handle_no_path(TX, TY).

+!process_path(Path, TX, TY) : not Path == []
   <- !follow_path(Path);
      -navigating(_);
      +navigating(false).

/* Handle no path found */
+!handle_no_path(TX, TY) : true
   <- .print("Attempting alternative goal selection...");
      -current_target(_, _, _);
      +current_target(none, 0, 0).

/* Follow path - simple implementation */
+!follow_path([]).

+!follow_path([Direction|Rest])
   <- move(Direction);
      !follow_path(Rest).

/* Helper rules */
can_paint_table :- 
   carrying_item(brush) & carrying_item(color) & not colored(table).

can_paint_chair :- 
   carrying_item(brush) & carrying_item(color) & not colored(chair).

can_open_door :- 
   carrying_item(key) & carrying_item(code) & not open(door).