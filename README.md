# Restaurant Rush

Restaurant Rush is a turn-based command-line restaurant management game written in Java.
You play as the manager: each turn you assign tasks to the Host, Waiter, and Chef, balancing customer patience to meet win targets.

SC2002 Object-Oriented Design and Programming, group assignment.

- Repository: https://github.com/edwardlaiyc/SC2302-TAIS1
- Release stages: **Stages 1, 2, and 3 complete** (git tags `stage-1`, `stage-2`, `stage-3`)
- Video walkthrough: https://drive.google.com/file/d/1ZZbvbxNQxGUYGpd4BAcGnjEaqOI-SG44/view?usp=sharing (timestamps below)

## Requirements

- **JDK 25** (`java -version` must report 25 or newer).
- Zero external build tools, third-party libraries, or internet connectivity required.

## Build and Run

Run these commands from the project root folder.

| Action | Windows (PowerShell) | macOS / Linux (Bash) |
|---|---|---|
| Compile | `.\build.ps1` | `./build.sh` |
| Start game | `.\run.ps1` | `./run.sh` |
| Execute replay | `.\run.ps1 --replay runs\stage3-victory.txt` | `./run.sh --replay runs/stage3-victory.txt` |

| Option | Description |
|---|---|
| `--setting stage3` | Runs Stage 3: Host, ComboMeal dishes, Happy Hour, VIP, Critic, and Regular customers (default) |
| `--setting stage2` | Runs Stage 2: Waiter, Chef, VIP, Critic, and Regular customers (replays `runs/stage2-*.txt`) |
| `--setting stage1` | Runs Stage 1: Waiter, Chef, and Regular customers only (replays `runs/stage1-*.txt`) |
| `--csv <file>` | Writes turn-by-turn customer satisfaction scores to `<file>` in CSV format |
| `--no-color` | Disables ANSI terminal color styling |

All presets use the base assignment parameters: 18 turns, 2 dining tables, 1 automated Cashier, and fixed customer arrivals at the start of turns 1, 4, 7, 10, 13, and 16.

## Gameplay and Rules

At the start of each turn, the board displays current revenue, the queue, table occupancies, kitchen cooking progress, Happy Hour status, and available staff actions.

1. If Happy Hour is unused, you are prompted whether to activate it.
2. You assign one task each to the Host, Waiter, and Chef in that order.

### Commands

| Command | Action | Allowed staff |
|---|---|---|
| `1`, `2`, ... | Selects the task with that number from the menu | All staff |
| `seat C2 T1` | Seats customer C2 at table T1 | Host, Waiter |
| `order C2` | Takes an order from customer C2 | Waiter |
| `serve C2` | Delivers dish to customer C2 once the order is READY | Waiter |
| `cook C2` | Cooks 1 preparation unit of customer C2's order | Chef |
| `wait` | Staff skips action for the current turn | All staff |
| `help` | Displays available command syntax | All staff |

### Win Conditions

The game evaluates win/loss criteria after turn 18 completes. To achieve **VICTORY**, you must satisfy all three targets:
- At least 4 paid customers.
- At least $45.00 in total revenue.
- Average satisfaction of paid customers of 60.0 or higher.

### Customer Types

In Stages 2 and 3, customer C2 is a VIP and customer C4 is a Critic. All other diners are Regular customers.

| Customer type | Satisfaction decay / waiting turn | Bill calculation | Custom behavior |
|---|---|---|---|
| Regular | -8 | 100% menu price | Standard dining cycle |
| VIP | -5 | 10% discount (90% of menu price) | Reduced patience decay rate |
| Critic | -12 | 100% menu price | Always orders dish with highest cooking units (Pasta / Combo 3). If food sits READY for 1+ turns before serving, incurs a one-time cold food penalty of -10 satisfaction. |

### Menu and Combos

| Item | Price | Cooking units | Availability |
|---|---|---|---|
| Salad | $9.00 | 1 | Stages 1, 2, 3 |
| Burger | $15.00 | 1 | Stages 1, 2, 3 |
| Pasta | $18.00 | 2 | Stages 1, 2, 3 |
| Salad + Burger (Combo 1) | $21.60 | 2 | Stage 3 only |
| Salad + Pasta (Combo 2) | $24.30 | 3 | Stage 3 only |
| Burger + Pasta (Combo 3) | $29.70 | 3 | Stage 3 only |

Combo meals are composed by aggregating two standalone menu dishes. A combo meal price is calculated as 90% of the sum of both dishes, rounded half-up to the nearest cent.

### Happy Hour Promotion

- Can be activated once per game by the player at the start of any turn (after arrivals, before staff tasks).
- Lasts for 2 active turns.
- While active, all new orders receive a 20% discount. For a VIP customer, discounts stack sequentially with half-up rounding (e.g. Burger: $15.00 x 0.9 = $13.50; $13.50 x 0.8 = $10.80).
- Order prices remain locked at their discounted value even after Happy Hour ends.
- At the start of each active turn, every customer currently waiting for a table or food recovers +15 satisfaction points (capped at 100).

## Implemented Interpretations of Ambiguities

Where the project specification leaves edge cases open, the game implements these exact rules:

| Rule or edge case | Implementation details |
|---|---|
| Turn board vs customer arrivals | The board prints first to show initial turn state. The arrival is then announced, and the task prompt reprints available moves including the newcomer. |
| Staff action order | Host acts first, followed by Waiter, then Chef. Each action resolves immediately so the Waiter can take an order from a diner just seated by the Host. |
| Eating and payment timing | Payment collection executes before eating updates. A customer served on turn T becomes READY_TO_PAY on turn T+1 and can pay on turn T+2 at the earliest. |
| Dish selection | Customer N orders item (N - 1) mod (menu size). Stages 1 and 2 rotate across the 3 single dishes; Stage 3 rotates across all 6 menu items including combos. |
| Critic dish preference | The Critic orders the item requiring the most cooking units; ties are broken by choosing the higher-priced item (Burger + Pasta in Stage 3). |
| Cold food definition | A dish marked READY can be served on the next turn at the earliest. If served on turn T+2 or later, it has sat on the counter for 1+ turns and triggers the -10 penalty. |
| Happy Hour timing | Prompted after arrivals, before staff actions. The +15 satisfaction recovery applies immediately on turn 1 and again on turn 2. |
| Percentage rounding | All calculations use integer cents with half-up rounding after each discrete percentage calculation step. |
| Scoring precision | Average satisfaction is evaluated as integer totals (`sum >= 60 * paidCount`) to eliminate floating-point rounding errors. Displayed to one decimal place. |
| Abandoned orders | If a customer's satisfaction drops to 0 while their dish is READY, the customer leaves unhappy and the order is cancelled with zero revenue. |
| Cashier priority | The customer who reached READY_TO_PAY status earliest is charged first. |
| Replay EOF handling | When a replay file ends before turn 18, the game exits cleanly with status code 3. |
| Turn 16 arrival | A customer arriving on turn 16 cannot complete dining before turn 18 finishes. Thus, at most 5 customers can pay in an 18-turn game. |

## Software Architecture and Design

The application follows the Entity-Control-Boundary (ECB) pattern:

| Package | Role | Core classes |
|---|---|---|
| `rush.boundary` | Terminal display, keyboard/file input, ANSI formatting | `ConsoleView`, `CommandInput`, `SatisfactionLog`, `Colors` |
| `rush.control` | Turn sequence orchestration, schedule loading, game configuration | `GameController`, `GameConfig`, `ArrivalSchedule`, `ManagerInput`, `GameListener` |
| `rush.entity` | Domain models, pricing arithmetic, state machines, business rules | `Restaurant`, `Customer`, `Table`, `Order`, `Staff`, `Task`, `HappyHour`, `Money` |

Key OOP principles implemented:
- **Clean separation of concerns:** Boundary classes contain zero game logic; they parse text into `Task` objects and format terminal output.
- **Command pattern:** Each action (`SeatTask`, `TakeOrderTask`, `ServeTask`, `CookTask`, `WaitTask`) is an executable object that validates its own preconditions via `check()` and throws checked `InvalidTaskException`.
- **Template Method pattern:** `Staff.perform()` is declared `final`, executing standard validation and turn logging while delegating execution to the task.
- **Polymorphism without type checking:** `Customer` is an abstract base class. Subclasses (`RegularCustomer`, `VIPCustomer`, `CriticCustomer`) override behavior polymorphically without `instanceof` checks.
- **Decoupled controller:** `GameController` interacts with the terminal solely through `ManagerInput` and `GameListener` interfaces.

Diagrams (PlantUML source files and SVGs) are available in [`docs/uml/`](docs/uml/):
- Full class diagram: `class-overview.svg`
- Domain class views: `class-1-floor.svg` through `class-7-boundary.svg`
- Turn sequence diagram (5 parts): `seq-1-arrival.svg` through `seq-5-payment.svg`
- Stage evolution diffs: `evolution-1-2.svg`, `evolution-2-3.svg`

## Functional Testing and Verification

The project relies on deterministic replay scripts in `runs/` that execute complete games and verify all functional requirements:

| Replay script | Command | Result | Paid | Revenue | Avg Satisfaction |
|---|---|---|---|---|---|
| Stage 1 victory | `--setting stage1 --replay runs/stage1-victory.txt` | VICTORY | 5 | $66.00 | 79.2 |
| Stage 1 defeat | `--setting stage1 --replay runs/stage1-defeat.txt` | DEFEAT | 4 | $51.00 | 52.0 |
| Stage 2 victory | `--setting stage2 --replay runs/stage2-victory.txt` | VICTORY | 5 | $73.50 | 75.6 |
| Stage 2 defeat | `--setting stage2 --replay runs/stage2-defeat.txt` | DEFEAT | 3 | $40.50 | 56.0 |
| Stage 3 victory | `--replay runs/stage3-victory.txt` | VICTORY | 4 | $64.26 | 86.8 |
| Stage 3 defeat | `--replay runs/stage3-defeat.txt` | DEFEAT | 4 | $70.20 | 55.8 |

## Video Presentation

The demonstration video is available on Google Drive: [Restaurant Rush Walkthrough Video](https://drive.google.com/file/d/1ZZbvbxNQxGUYGpd4BAcGnjEaqOI-SG44/view?usp=sharing).

| # | Segment description | Timestamp |
|---|---|---|
| 1 | Launching the game from the submitted project | 00:00 |
| 2 | Manager decision-making under staff constraints | 00:49 |
| 3 | Order lifecycle: READY to SERVED to payment collection | 02:24 |
| 4 | VIP and Critic customer behavior in action | 04:24 |
| 5 | Stage 3 features: Host, ComboMeal, and Happy Hour | 06:18 |
| 6 | Victory and defeat gameplay outcomes | 08:28 |
| 7 | Stage tag comparisons and architecture diagrams | 09:24 |

## Individual AI History

Each member's AI chat history is kept on its own branch, in the `ai-history/` folder.

| Member | Branch |
|---|---|
| Cui Tao | [`ai-history/cuitao`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/cuitao/ai-history) |
| Wazir | To be added |
| Liu Yuan | [`ai-history/liuyuan`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/liuyuan/ai-history) |
| Farsat | [`ai-history/farsat`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/farsat/ai-history) |
| Edward | [`ai-history/edward`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/edward/ai-history) |

## Stage-by-Stage Evolution

### Stage 1 to Stage 2

Git comparison: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-1...stage-2

**New files:**
- `entity/RegularCustomer.java`: Standard customer behavior extracted from `Customer`.
- `entity/VIPCustomer.java`: Implements -5 decay per turn and 10% bill discount.
- `entity/CriticCustomer.java`: Implements -12 decay per turn, Pasta preference, and cold food penalty.
- `runs/stage2-victory.txt`, `runs/stage2-defeat.txt`: Deterministic replay tests with VIP and Critic diners.

**Modified files:**
- `entity/Customer.java`: Converted to abstract class. Replaced fixed decay with `getSatisfactionLoss()` and bill calculation with `getBill()`. Added `getType()`, `getReaction()`, and `reduceSatisfaction()`.
- `entity/Money.java`: Added `percent(int percentage)` helper with half-up rounding.
- `entity/Order.java`: Added `getReadyTurn()` to track kitchen completion turn.
- `entity/ServeTask.java`: Updated output message to display customer reactions.
- `control/ArrivalSchedule.java`: Supports polymorphic customer factory constructors per arrival slot.
- `control/GameConfig.java`: Added `stage1()` and `stage2()` presets.
- `boundary/ConsoleView.java`: Displays customer type in terminal output.
- `Main.java`: Added `--setting stage1` and `--setting stage2` command-line flags.

**Unchanged:** `GameController`, `Restaurant`, `Staff`, `Waiter`, `Chef`, `Cashier`, and all underlying task validation rules. Stage 1 replays execute with identical results on Stage 2 code.

### Stage 2 to Stage 3

Git comparison: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-2...stage-3

**New files:**
- `entity/Host.java`: New staff role dedicated exclusively to seating customers.
- `entity/ComboMeal.java`: Subclasses `MenuItem` by aggregating two existing dishes at 90% combined price.
- `entity/HappyHour.java`: Manages promotional duration, discounts, and customer patience recovery.
- `runs/stage3-victory.txt`, `runs/stage3-defeat.txt`: Replay benchmarks exercising Stage 3 features.

**Modified files:**
- `entity/Restaurant.java`: Owns `HappyHour` instance; added `takeOrder()`, `startHappyHour()`, and `recoverWaitingCustomers()`. Locks order price when taken.
- `entity/Customer.java`: `placeOrder()` receives the locked bill amount; added `recover(int points)`.
- `entity/TakeOrderTask.java`: Delegates pricing directly to `Restaurant.takeOrder()`.
- `entity/Menu.java`: Added `withCombos()` registration helper.
- `control/GameController.java`: Added turn-start phase for Happy Hour prompting and satisfaction recovery.
- `control/ManagerInput.java`: Added `wantsHappyHour()` interface method.
- `control/GameListener.java`: Added `happyHourStarted()` and `customersRecovered()` event hooks.
- `control/GameConfig.java`: Added `stage3()` preset with Host registered first in the staff list.
- `boundary/CommandInput.java`: Prompts manager for Happy Hour activation.
- `boundary/ConsoleView.java`: Displays Happy Hour banner and recovery status.
- `Main.java`: Configured `--setting stage3` as default execution mode.

**Unchanged:** `Staff`, `Waiter`, `Chef`, `Cashier`, `Order`, all customer classes, and the validation logic in `SeatTask`, `ServeTask`, and `CookTask`. The `Host` reuses `SeatTask` directly. Stage 1 and Stage 2 replay scripts continue to produce identical results on Stage 3 code.
