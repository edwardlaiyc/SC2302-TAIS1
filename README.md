# Restaurant Rush

A turn-based command-line restaurant game in Java. You are the manager: each turn you give one task to each staff member, and after 18 turns the game checks three targets for victory or defeat.

SC2002 group assignment, lab group TAIS1, group 6.

- Repository: https://github.com/edwardlaiyc/SC2302-TAIS1
- Stages: [`stage-1`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/stage-1) · [`stage-2`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/stage-2) · [`stage-3`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/stage-3)
- Video: https://drive.google.com/file/d/1ZZbvbxNQxGUYGpd4BAcGnjEaqOI-SG44/view?usp=sharing
- Report (rules, design, diagrams, evidence): the PDF in the submission. Diagram sources: [`docs/uml/`](docs/uml/)

## Build and run

Requires **JDK 25**. No other tools, libraries or network access. Run from the project root.

| Action | Windows (PowerShell) | macOS / Linux |
|---|---|---|
| Compile | `.\build.ps1` | `./build.sh` |
| Play | `.\run.ps1` | `./run.sh` |
| Replay a run | `.\run.ps1 --replay runs\stage3-victory.txt` | `./run.sh --replay runs/stage3-victory.txt` |

`--setting stage1`, `stage2` or `stage3` (default) picks the stage. All use the base setting: 18 turns, 2 tables, arrivals on turns 1, 4, 7, 10, 13 and 16. Type `help` in the game for the commands.

## Replay runs

| Run | Command | Result | Paid | Revenue | Avg satisfaction |
|---|---|---|---|---|---|
| Stage 1 victory | `--setting stage1 --replay runs/stage1-victory.txt` | VICTORY | 5 | $66.00 | 79.2 |
| Stage 1 defeat | `--setting stage1 --replay runs/stage1-defeat.txt` | DEFEAT | 4 | $51.00 | 52.0 |
| Stage 2 victory | `--setting stage2 --replay runs/stage2-victory.txt` | VICTORY | 5 | $73.50 | 75.6 |
| Stage 2 defeat | `--setting stage2 --replay runs/stage2-defeat.txt` | DEFEAT | 3 | $40.50 | 56.0 |
| Stage 3 victory | `--replay runs/stage3-victory.txt` | VICTORY | 4 | $64.26 | 86.8 |
| Stage 3 defeat | `--replay runs/stage3-defeat.txt` | DEFEAT | 4 | $70.20 | 55.8 |

## Stage 1 → Stage 2

Compare: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-1...stage-2

**New files**
- `entity/RegularCustomer.java`: the Stage 1 customer rules (−8 per waiting turn, full price).
- `entity/VIPCustomer.java`: −5 per waiting turn, 10% off the bill.
- `entity/CriticCustomer.java`: −12 per waiting turn, orders the dish with the most cooking units, one-time −10 for cold food.
- `runs/stage2-victory.txt`, `runs/stage2-defeat.txt`: Stage 2 runs with a VIP and a Critic.

**Changed files**

| File | Change | Reason |
|---|---|---|
| `entity/Customer.java` | Becomes abstract; adds `getSatisfactionLoss()` and `getBill()` | Loss and bill now differ by customer type, so each subclass supplies them. |
| `entity/Money.java` | Adds `percent()` | The VIP discount needs a percentage with half-up rounding. |
| `entity/Order.java` | Adds `getReadyTurn()` | The Critic needs to know how long the dish sat READY. |
| `entity/ServeTask.java` | Prints the customer's reaction | Shows the Critic's cold-food reaction. The serve rule is unchanged. |
| `control/ArrivalSchedule.java`, `control/GameConfig.java` | Each arrival names its customer type; `stage1()`, `stage2()` presets | Who arrives when is setup, which these classes own. |
| `boundary/ConsoleView.java`, `Main.java` | Shows the customer type; `--setting` flag | Display and start-up options. |

**Deleted files:** none.

**Still works:** `GameController`, `Restaurant`, `Staff`, `Waiter`, `Chef`, `Cashier` and the task rules are unchanged. The Stage 1 runs give the same results on Stage 2 code (`--setting stage1`).

## Stage 2 → Stage 3

Compare: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-2...stage-3

**New files**
- `entity/Host.java`: a staff member who can only seat customers; reuses `SeatTask`.
- `entity/ComboMeal.java`: a `MenuItem` made of two dishes; units are the sum, price is 90% of the sum.
- `entity/HappyHour.java`: usable once, 2 turns, 20% off new orders, +15 to waiting customers each active turn.
- `runs/stage3-victory.txt`, `runs/stage3-defeat.txt`: Stage 3 runs.

**Changed files**

| File | Change | Reason |
|---|---|---|
| `entity/Restaurant.java` | Owns `HappyHour`; adds `takeOrder()`, `startHappyHour()`, `recoverWaitingCustomers()` | Happy Hour is restaurant-wide, so the restaurant fixes the price: VIP discount first, then Happy Hour. |
| `entity/Customer.java` | `placeOrder()` takes the fixed price; adds `recover()` | Follows from the restaurant fixing the price; recovery is a change to the customer's own satisfaction. |
| `entity/TakeOrderTask.java` | Calls `Restaurant.takeOrder()` | Same reason. The task's check is unchanged. |
| `entity/Menu.java` | Adds `withCombos()` | The menu owns its dishes. |
| `control/GameController.java` | Adds the Happy Hour step at turn start | Happy Hour adds a step to the turn. |
| `control/ManagerInput.java`, `control/GameListener.java` | Add `wantsHappyHour()`, `happyHourStarted()`, `customersRecovered()` | The new decision and events. Listener methods have empty defaults, so existing listeners still compile. |
| `control/GameConfig.java` | `stage3()` preset, Host first in the staff list | Setup. |
| `boundary/CommandInput.java`, `boundary/ConsoleView.java`, `Main.java` | Happy Hour question and status; `--setting stage3` is the default | Input and display. |

**Deleted files:** none.

**Still works:** `Staff`, `Waiter`, `Chef`, `Cashier`, `Order`, all customer types and the checks in `SeatTask`, `ServeTask` and `CookTask` are unchanged. The Stage 1 and Stage 2 runs give the same results on Stage 3 code.

## Video timestamps

| # | Segment | Time |
|---|---|---|
| 1 | The game launching from the submitted project | 00:00 |
| 2 | Manager choices, including a decision where staff are constrained | 00:49 |
| 3 | A dish going READY → SERVED → payment | 02:24 |
| 4 | VIP and Critic behaviour | 04:24 |
| 5 | Stage 3 features: Host, ComboMeal, Happy Hour | 06:18 |
| 6 | One victory and one defeat | 08:28 |
| 7 | Stage tag comparisons and the diagrams | 09:24 |

## AI history

Each member's AI chats are on their own branch, in `ai-history/`.

| Member | Branch |
|---|---|
| Wazir | [`ai-history/wazir`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/wazir/ai-history) |
| Cui Tao | [`ai-history/cuitao`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/cuitao/ai-history) |
| Liu Yuan | [`ai-history/liuyuan`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/liuyuan/ai-history) |
| Farsat | [`ai-history/farsat`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/farsat/ai-history) |
| Edward | [`ai-history/edward`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/edward/ai-history) |
