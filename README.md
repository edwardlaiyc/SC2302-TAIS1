# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java.
You are the manager: each turn you decide what the Host, the Waiter and the Chef do,
and the customers' patience decides whether you win.

SC2002 Object-Oriented Design & Programming, group assignment.

- Repository: https://github.com/edwardlaiyc/SC2302-TAIS1
- Stages: **1, 2 and 3 complete** (tags `stage-1`, `stage-2`, `stage-3`)
- Video: TODO (timestamps below)

## Requirements

**JDK 25** (`java -version` must show 25 or newer). Nothing else: no build tool, no network.

## Build and run

Run these from the project folder.

| | Windows (PowerShell) | macOS / Linux |
|---|---|---|
| Build | `.\build.ps1` | `./build.sh` |
| Play | `.\run.ps1` | `./run.sh` |
| Replay a saved game | `.\run.ps1 --replay runs\stage3-victory.txt` | `./run.sh --replay runs/stage3-victory.txt` |

| Option | Effect |
|---|---|
| `--setting stage3` | Host, ComboMeal and Happy Hour, with Regular, VIP and Critic customers (default) |
| `--setting stage2` | Waiter and Chef only; Regular, VIP and Critic customers. Replays `runs/stage2-*.txt`. |
| `--setting stage1` | Waiter and Chef only; Regular customers only. Replays `runs/stage1-*.txt`. |
| `--csv FILE` | write each customer's satisfaction per turn to FILE |
| `--no-color` | plain text |

All settings use the base setting from the brief: 18 turns, 2 tables, one Cashier, and one
arrival at the start of turns 1, 4, 7, 10, 13 and 16.

## How to play

Each turn the board shows revenue, the queue, the tables, the kitchen, Happy Hour and what
each member of staff can do. Then:

1. While Happy Hour is unused, you are asked whether to start it.
2. You give the Host, then the Waiter, then the Chef one task each.

| Command | Meaning | Who |
|---|---|---|
| `1`, `2`, ... | the task with that number in the list | all |
| `seat C2 T1` | seat customer C2 at table T1 | Host, Waiter |
| `order C2` | take C2's order | Waiter |
| `serve C2` | serve C2's dish once it is READY | Waiter |
| `cook C2` | one preparation unit of C2's order | Chef |
| `wait` | do nothing this turn | all |
| `help` | show the commands | all |

**Win** after 18 turns with at least 4 paid customers, $45.00 revenue and average
satisfaction (of paid customers) of 60 or more.

### Customers

C2 is a VIP and C4 is a Critic. The others are Regular.

| Customer | Loses per waiting turn | Bill | Behaviour |
|---|---|---|---|
| Regular | 8 | full price | — |
| VIP | 5 | 10% off | — |
| Critic | 12 | full price | Orders the dish that takes longest to cook. If served later than possible, the food is cold: −10 once. |

### Menu

| Item | Price | Units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger | $21.60 | 2 |
| Salad + Pasta | $24.30 | 3 |
| Burger + Pasta | $29.70 | 3 |

The three combos are Stage 3 only. A combo costs 90% of its two parts, rounded to the nearest cent.

### Happy Hour

Once per game, at the start of a turn. It lasts that turn and the next. Orders taken while it
is on cost 20% less (after the VIP discount: a VIP's Burger is $15.00 × 0.9 × 0.8 = $10.80),
and the price stays locked after it ends. At the start of each active turn, every customer
still waiting for a table or food gets 15 satisfaction back (up to 100).

## Rules as implemented

Where the brief leaves a point open, this is how the game reads it.

| Point | Reading |
|---|---|
| Board vs arrivals | The start-of-turn board prints first. Then the arrival is announced, and the prompts list the tasks again, including the new customer. |
| Order of staff | Host, then Waiter, then Chef. Each task is done before the next person is asked, so the Waiter can take an order from a customer the Host just seated, and the Chef can cook it. |
| Eating and paying | Payment is checked before eating each turn. A customer served on turn *t* is READY_TO_PAY on *t*+1 and pays on *t*+2 at the earliest. |
| Food choice | Customer *n* (arrival order) orders item (*n* − 1) mod (menu size). Stages 1–2: Salad, Burger, Pasta, ... Stage 3: the rotation runs over all six items, combos included. |
| Critic's choice | The item with the most preparation units; on a tie, the higher price. In Stage 3: Burger + Pasta. |
| Cold food | A READY dish can be served the next turn at the earliest. If the Critic is served any later, the Critic loses 10 once. |
| Happy Hour timing | Asked after the arrival, before anyone acts. Recovery applies at once on the first active turn and again on the second. |
| Rounding | Every percentage rounds half up to the nearest cent, step by step: VIP first, then Happy Hour. |
| Average satisfaction | Compared as totals (sum ≥ 60 × paid), so 59.75 never counts as 60. Shown to one decimal; "N/A" until someone pays. |
| End-of-turn results | Printed after every turn and again at the end. |
| Leaving with a READY dish | The order is cancelled: it never reached the table. |
| Cashier order | Earliest READY_TO_PAY turn first. |
| Replay file too short | The game stops with a message and exit code 3. |
| Turn-16 customer | Cannot pay within 18 turns, so at most five customers can pay. The brief intends this. |
| VIP and Critic in the base run | They replace the 2nd and 4th Regular arrivals. There are still six arrivals on the same turns. |

## Design

| Package | Holds | Main classes |
|---|---|---|
| `rush.boundary` | screen and keyboard/file input | `ConsoleView`, `CommandInput`, `SatisfactionLog` |
| `rush.control` | the turn sequence and game setup | `GameController`, `GameConfig`, `ArrivalSchedule` |
| `rush.entity` | the restaurant and every game rule | `Restaurant`, `Customer`, `Order`, `Staff`, `Task`, `HappyHour`, `Money` |

- The screen classes hold no rules. They turn typed text into `Task` objects and print the board.
- Each task (`SeatTask`, `TakeOrderTask`, `ServeTask`, `CookTask`, `WaitTask`) checks its own
  rule. The same check lists the allowed tasks and refuses an invalid one with a reason.
- `Staff` is abstract; `Host`, `Waiter` and `Chef` differ only in their duties and the tasks they try.
- `Customer` is abstract. `RegularCustomer`, `VIPCustomer` and `CriticCustomer` each give their own
  loss per turn, bill, food choice and reaction. No code checks a customer's type.
- `ComboMeal` is a `MenuItem` made of two `MenuItem`s, so ordering, cooking and paying treat it like any dish.
- `GameController` talks to the screen only through `ManagerInput` and `GameListener`.

Diagrams (PlantUML source and SVG) are in [`docs/uml/`](docs/uml/):

| Diagram | File |
|---|---|
| Class overview | `class-overview.svg` |
| Class views (floor, customers, orders and menu, staff, tasks, control, boundary) | `class-1-floor.svg` … `class-7-boundary.svg` |
| Sequence diagram, in five parts | `seq-1-arrival.svg` … `seq-5-payment.svg` |
| Stage changes | `evolution-1-2.svg`, `evolution-2-3.svg` |

To re-render: `java -jar plantuml.jar -tsvg docs/uml/*.puml` (needs Graphviz and the Inter font).

## Testing

There are no unit tests. Each replay in `runs/` plays one full game and gives the same result every time.

## Video

| # | Segment | Time |
|---|---|---|
| 1 | The game launching from the submitted project | TODO |
| 2 | Manager choices, including a decision where staff are constrained | TODO |
| 3 | A dish going READY → SERVED → payment | TODO |
| 4 | A VIP or Critic behaviour | TODO |
| 5 | A Stage 3 feature | TODO |
| 6 | One victory and one defeat | TODO |
| 7 | The stage tag comparisons and the diagrams | TODO |

## Stage changes

### Stage 1 → Stage 2

Compare: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-1...stage-2

**New files**

| File | Why |
|---|---|
| `entity/RegularCustomer.java` | Regular behaviour (loses 8) moves out of `Customer` into its own type. |
| `entity/VIPCustomer.java` | Loses 5 per turn; overrides the bill for 10% off. |
| `entity/CriticCustomer.java` | Loses 12 per turn; overrides the food choice and reacts to cold food. |
| `runs/stage2-victory.txt`, `runs/stage2-defeat.txt` | Reproducible runs with a VIP and a Critic. |

**Changed files**

| File | Change | Responsibility that forced it |
|---|---|---|
| `entity/Customer.java` | Now `abstract`. The fixed loss of 8 became `getSatisfactionLoss()`; the order price comes from `getBill()`; added `getType()`, `getReaction()` and `reduceSatisfaction()`. | Satisfaction loss and billing are customer rules, and they now differ by customer type. |
| `entity/Money.java` | Added `percent()` with half-up rounding. | The VIP discount needs a percentage of a price in whole cents. |
| `entity/Order.java` | Added `getReadyTurn()`. | The Critic needs to know when the dish became READY to judge if it is cold. |
| `entity/ServeTask.java` | The result line shows the customer's reaction. | Showing the Critic's reaction. The serving rule is unchanged. |
| `control/ArrivalSchedule.java` | Each arrival names its customer type: `add(4, VIPCustomer::new)`. | Setup: which type arrives in each slot. |
| `control/GameConfig.java` | `standard()` became `stage1()` (all Regular) and `stage2()` (with VIP and Critic). | Setup. Keeps the Stage 1 setting available for regression checks. |
| `boundary/ConsoleView.java` | Shows each customer's type. | Display only. |
| `Main.java` | Added `--setting stage1/stage2`. | Choosing a setting. |

**Deleted:** none.

**Unchanged:** `GameController`, `Restaurant`, `Staff`, `Waiter`, `Chef`, `Cashier` and the rule in every task.
The Stage 1 replays give the same results on Stage 2 code with `--setting stage1`.

### Stage 2 → Stage 3

Compare: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-2...stage-3

**New files**

| File | Why |
|---|---|
| `entity/Host.java` | A new role: a `Staff` whose only duty is seating. |
| `entity/ComboMeal.java` | A `MenuItem` built from two items: units add up, price is 90%. |
| `entity/HappyHour.java` | The promotion: when it runs, the discount and the recovery. |
| `runs/stage3-victory.txt`, `runs/stage3-defeat.txt` | Reproducible runs with the Stage 3 features. |

**Changed files**

| File | Change | Responsibility that forced it |
|---|---|---|
| `entity/Restaurant.java` | Owns a `HappyHour`; new `takeOrder()`, `startHappyHour()` and `recoverWaitingCustomers()`. The constructor takes whether Happy Hour is offered. | The price of an order now depends on a restaurant-wide promotion, so the restaurant must fix the price when the order is taken. |
| `entity/Customer.java` | `placeOrder()` receives the item and the locked price instead of working out the price; added `recover()`. | The customer still chooses the dish and gives the bill, but no longer has the full price picture. |
| `entity/TakeOrderTask.java` | Calls `restaurant.takeOrder()`. | Follows from the change above. The rule that checks the task is unchanged. |
| `entity/Menu.java` | Added `withCombos()`. | Setup: the Stage 3 menu. |
| `control/GameController.java` | One new step at the start of a turn: offer Happy Hour, then apply the recovery. | Happy Hour is a new step in the turn sequence. The six steps are otherwise untouched. |
| `control/ManagerInput.java` | Added `wantsHappyHour()`. | The manager now makes one more kind of decision. |
| `control/GameListener.java` | Added `happyHourStarted()` and `customersRecovered()`. | New events to show. Both have empty defaults, so existing listeners did not change. |
| `control/GameConfig.java` | Holds the staff list and whether Happy Hour is offered; added `stage3()`. | Setup: registers the Host (first in the list) and the combo menu. |
| `boundary/CommandInput.java` | Asks the Happy Hour question. | Input for the new decision. |
| `boundary/ConsoleView.java` | Shows Happy Hour status and recovery; wider labels. | Display only. |
| `Main.java` | Added `--setting stage3`, now the default. | Choosing a setting. |

**Deleted:** none.

**Unchanged:** `Staff`, `Waiter`, `Chef`, `Cashier`, `Order`, every customer type, and the rules in
`SeatTask`, `ServeTask` and `CookTask`. `Host` reuses `SeatTask` as it is.
The Stage 1 and Stage 2 replays give the same results on Stage 3 code with `--setting stage1` and `--setting stage2`.
