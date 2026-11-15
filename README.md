# Restaurant Rush

A turn-based command-line restaurant game in Java. You are the manager. Each turn, you give one task to each staff member. After 18 turns, the game checks three targets and declares victory or defeat.

This is the SC2302 group assignment of lab group TAIS1, group 6.

- Repository: https://github.com/edwardlaiyc/SC2302-TAIS1
- Stages:
  - [`stage-1`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/stage-1)
  - [`stage-2`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/stage-2)
  - [`stage-3`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/stage-3)
- Video: https://drive.google.com/file/d/1KIPWOP982iiDgpsPoJVe1ARzF-wHCuHa/view?usp=sharing
- Report: the PDF in the submission. It covers the rules, the design, the diagrams and the evidence. The diagram sources are in [`docs/uml/`](docs/uml/).

## Build and run

The game needs **JDK 25** and nothing else: no other tools, no libraries and no network access. Run every command from the project root.

| Action | Windows (PowerShell) | macOS / Linux |
|---|---|---|
| Compile | `.\build.ps1` | `./build.sh` |
| Play | `.\run.ps1` | `./run.sh` |
| Replay a run | `.\run.ps1 --replay runs\stage3-victory.txt` | `./run.sh --replay runs/stage3-victory.txt` |

`--setting stage1`, `stage2` or `stage3` picks the stage. `stage3` is the default. Every stage uses the base setting: 18 turns, 2 tables, and arrivals on turns 1, 4, 7, 10, 13 and 16. Type `help` in the game to list the commands.

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
- `entity/RegularCustomer.java`: the Stage 1 customer rules. It loses 8 per waiting turn and pays the full price.
- `entity/VIPCustomer.java`: loses 5 per waiting turn and gets 10% off the bill.
- `entity/CriticCustomer.java`: loses 12 per waiting turn and orders the dish with the most cooking units. It loses 10 once if its food is cold.
- `runs/stage2-victory.txt`, `runs/stage2-defeat.txt`: Stage 2 runs with a VIP and a Critic.

**Changed files**

| File | Change | Reason |
|---|---|---|
| `entity/Customer.java` | Becomes abstract. Adds `getSatisfactionLoss()` and `getBill()`. | Loss and bill now differ by customer type, so each subclass supplies them. |
| `entity/Money.java` | Adds `percent()`. | The VIP discount needs a percentage with half-up rounding. |
| `entity/Order.java` | Adds `getReadyTurn()`. | The Critic needs to know how long the dish sat READY. |
| `entity/ServeTask.java` | Prints the customer's reaction. | The player sees the Critic's cold-food reaction. The serve rule did not change. |
| `control/ArrivalSchedule.java`, `control/GameConfig.java` | Each arrival names its customer type. Adds the `stage1()` and `stage2()` presets. | These classes own the setup, including who arrives when. |
| `boundary/ConsoleView.java`, `Main.java` | Shows the customer type. Adds the `--setting` flag. | These classes own the display and the start-up options. |

**Deleted files:** none.

**Unchanged:** `GameController`, `Restaurant`, `Staff`, `Waiter`, `Chef`, `Cashier` and the task rules did not change. The Stage 1 runs give the same results on Stage 2 code (`--setting stage1`).

## Stage 2 → Stage 3

Compare: https://github.com/edwardlaiyc/SC2302-TAIS1/compare/stage-2...stage-3

**New files**
- `entity/Host.java`: a staff member who can only seat customers. It reuses `SeatTask`.
- `entity/ComboMeal.java`: a `MenuItem` made of two dishes. Its units are the sum of both, and its price is 90% of the sum.
- `entity/HappyHour.java`: the manager can start it once, and it lasts 2 turns. It takes 20% off new orders and gives waiting customers +15 each active turn.
- `runs/stage3-victory.txt`, `runs/stage3-defeat.txt`: Stage 3 runs.

**Changed files**

| File | Change | Reason |
|---|---|---|
| `entity/Restaurant.java` | Owns `HappyHour`. Adds `takeOrder()`, `startHappyHour()` and `recoverWaitingCustomers()`. | Happy Hour applies to the whole restaurant, so the restaurant fixes the price: VIP discount first, then Happy Hour. |
| `entity/Customer.java` | `placeOrder()` takes the fixed price. Adds `recover()`. | The restaurant now fixes the price. Recovery changes the customer's own satisfaction. |
| `entity/TakeOrderTask.java` | Calls `Restaurant.takeOrder()`. | The restaurant now fixes the price. The task's check did not change. |
| `entity/Menu.java` | Adds `withCombos()`. | The menu owns its dishes. |
| `control/GameController.java` | Adds the Happy Hour step at the start of a turn. | Happy Hour adds a step to the turn. |
| `control/ManagerInput.java`, `control/GameListener.java` | Add `wantsHappyHour()`, `happyHourStarted()` and `customersRecovered()`. | They carry the new decision and the new events. The listener methods have empty defaults, so existing listeners still compile. |
| `control/GameConfig.java` | Adds the `stage3()` preset, with the Host first in the staff list. | This class owns the setup. |
| `boundary/CommandInput.java`, `boundary/ConsoleView.java`, `Main.java` | Adds the Happy Hour question and status line. Makes `--setting stage3` the default. | These classes own input and display. |

**Deleted files:** none.

**Unchanged:** `Staff`, `Waiter`, `Chef`, `Cashier`, `Order`, all customer types and the checks in `SeatTask`, `ServeTask` and `CookTask` did not change. The Stage 1 and Stage 2 runs give the same results on Stage 3 code.

## Video timestamps

| # | Segment | Time |
|---|---|---|
| 1 | The game launching from the submitted project | 00:04 |
| 2 | Manager choices, including a decision where staff are constrained | 00:51 |
| 3 | A dish going READY → SERVED → payment | 02:28 |
| 4 | VIP and Critic behaviour | 04:28 |
| 5 | Stage 3 features: Host, ComboMeal, Happy Hour | 06:22 |
| 6 | One victory and one defeat | 08:29 |
| 7 | Stage tag comparisons and the diagrams | 09:27 |

## AI history

Each member's AI chats are on their own branch, in `ai-history/`.

| Member | Branch |
|---|---|
| Wazir | [`ai-history/wazir`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/wazir/ai-history) |
| Cui Tao | [`ai-history/cuitao`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/cuitao/ai-history) |
| Liu Yuan | [`ai-history/liuyuan`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/liuyuan/ai-history) |
| Farsat | [`ai-history/farsat`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/farsat/ai-history) |
| Edward | [`ai-history/edward`](https://github.com/edwardlaiyc/SC2302-TAIS1/tree/ai-history/edward/ai-history) |
