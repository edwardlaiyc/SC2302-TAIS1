# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java.
You are the manager: each turn you decide what the Waiter and the Chef do, and the
customers' patience decides whether you win.

SC2002 Object-Oriented Design & Programming, group assignment.

- Repository: https://github.com/edwardlaiyc/SC2302-TAIS1
- Current stage: **Stage 2** (tags `stage-1`, `stage-2`)

## Requirements

**JDK 25** (`java -version` must show 25 or newer). Nothing else: no build tool, no network.

## Build and run

Run these from the project folder.

| | Windows (PowerShell) | macOS / Linux |
|---|---|---|
| Build | `.\build.ps1` | `./build.sh` |
| Play | `.\run.ps1` | `./run.sh` |
| Replay a saved game | `.\run.ps1 --replay runs\stage2-victory.txt` | `./run.sh --replay runs/stage2-victory.txt` |

Other options:

| Option | Effect |
|---|---|
| `--setting stage1` | Regular customers only, exactly as in Stage 1. Use it to replay `runs/stage1-*.txt`. |
| `--setting stage2` | Regular, VIP and Critic customers (default) |
| `--csv FILE` | write each customer's satisfaction per turn to FILE |
| `--no-color` | plain text |

## How to play

Each turn the board shows revenue, the queue, the tables, the kitchen and what each
member of staff can do. You give the Waiter one task, then the Chef one task.

| Command | Meaning | Who |
|---|---|---|
| `1`, `2`, ... | the task with that number in the list | both |
| `seat C2 T1` | seat customer C2 at table T1 | Waiter |
| `order C2` | take C2's order | Waiter |
| `serve C2` | serve C2's dish once it is READY | Waiter |
| `cook C2` | one preparation unit of C2's order | Chef |
| `wait` | do nothing this turn | both |
| `help` | show the commands | both |

Customers arrive on turns 1, 4, 7, 10, 13 and 16. In Stage 2, C2 is a VIP and C4 is a Critic.

| Customer | Loses per waiting turn | Bill | Behaviour |
|---|---|---|---|
| Regular | 8 | full price | — |
| VIP | 5 | 10% off | — |
| Critic | 12 | full price | Orders the dish that takes longest to cook. If served later than possible, the food is cold: −10 once. |

**Win** after 18 turns with at least 4 paid customers, $45.00 revenue and average
satisfaction (of paid customers) of 60 or more.

## Rules as implemented

Where the brief leaves a point open, this is how the game reads it.

| Point | Reading |
|---|---|
| Board vs arrivals | The start-of-turn board prints first. Then the arrival is announced, and the Waiter's prompt lists the tasks again, including the new customer. |
| Waiter before Chef | The Waiter's task is asked for and done before the Chef's, so the Chef can cook an order the Waiter just took. |
| Eating and paying | Payment is checked before eating each turn. A customer served on turn *t* is READY_TO_PAY on *t*+1 and pays on *t*+2 at the earliest. |
| Food choice | Customer *n* (arrival order) orders item (*n* − 1) mod 3: Salad, Burger, Pasta, Salad, ... |
| Average satisfaction | Compared as totals (sum ≥ 60 × paid), so 59.75 never counts as 60. Shown to one decimal; "N/A" until someone pays. |
| End-of-turn results | Printed after every turn and again at the end. |
| Leaving with a READY dish | The order is cancelled: it never reached the table. |
| Cashier order | Earliest READY_TO_PAY turn first. |
| Replay file too short | The game stops with a message and exit code 3. |
| Turn-16 customer | Cannot pay within 18 turns, so at most five customers can pay. The brief intends this. |
| VIP discount | 10% off, rounded half up to the nearest cent (Burger $15.00 → $13.50). |
| Critic's choice | The item with the most preparation units; on a tie, the higher price. |
| Cold food | A READY dish can be served the next turn at the earliest. If the Critic is served any later, the Critic loses 10 once. |
| VIP and Critic in the base run | They replace the 2nd and 4th Regular arrivals. There are still six arrivals on the same turns. |

## Design

| Package | Holds | Main classes |
|---|---|---|
| `rush.boundary` | screen and keyboard/file input | `ConsoleView`, `CommandInput`, `SatisfactionLog` |
| `rush.control` | the turn sequence and game setup | `GameController`, `GameConfig`, `ArrivalSchedule` |
| `rush.entity` | the restaurant and every game rule | `Restaurant`, `Customer`, `Order`, `Staff`, `Task`, `Money` |

- The screen classes hold no rules. They turn typed text into `Task` objects and print the board.
- Each task (`SeatTask`, `TakeOrderTask`, `ServeTask`, `CookTask`, `WaitTask`) checks its own
  rule. The same check lists the allowed tasks and refuses an invalid one with a reason.
- `GameController` talks to the screen only through `ManagerInput` and `GameListener`.
- `Customer` is abstract. `RegularCustomer`, `VIPCustomer` and `CriticCustomer` each give their own
  loss per turn, bill, food choice and reaction. No code checks a customer's type.

## Testing

There are no unit tests. Each replay in `runs/` plays one full game and gives the same result every time.
See [`docs/test-notes.md`](docs/test-notes.md): the victory and defeat runs and the rule checks.

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
