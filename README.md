# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java.
You are the manager: each turn you decide what the Waiter and the Chef do, and the
customers' patience decides whether you win.

SC2002 Object-Oriented Design & Programming, group assignment.

- Repository: https://github.com/edwardlaiyc/SC2302-TAIS1
- Current stage: **Stage 1** (tag `stage-1`)

## Requirements

**JDK 25** (`java -version` must show 25 or newer). Nothing else: no build tool, no network.

## Build and run

Run these from the project folder.

| | Windows (PowerShell) | macOS / Linux |
|---|---|---|
| Build | `.\build.ps1` | `./build.sh` |
| Play | `.\run.ps1` | `./run.sh` |
| Replay a saved game | `.\run.ps1 --replay runs\stage1-victory.txt` | `./run.sh --replay runs/stage1-victory.txt` |

Other options: `--csv FILE` writes each customer's satisfaction per turn; `--no-color` gives plain text.

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

## Testing

See [`docs/test-notes.md`](docs/test-notes.md): the victory and defeat runs and the rule checks.
