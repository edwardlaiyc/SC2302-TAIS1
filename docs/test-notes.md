# Test notes

All runs use the base setting: 18 turns, 2 tables, one Waiter, Chef and Cashier, and
one arrival on turns 1, 4, 7, 10, 13 and 16. A replay file gives the same game every time.

## Stage 1

### Victory

```
./run.sh --replay runs/stage1-victory.txt        # Windows: .\run.ps1 --replay runs\stage1-victory.txt
```

| Paid | Revenue | Avg satisfaction | Unhappy departures | Result |
|---|---|---|---|---|
| 5 | $66.00 | 79.2 | 0 | **VICTORY** |

The Waiter keeps a seat → order → serve rhythm and the Chef cooks each dish the turn it
is ordered. On turn 10 the Waiter can serve C3's READY Pasta or seat the new customer C4,
not both. Serving first costs C4 one turn in the queue but stops C3 losing more.

### Defeat

```
./run.sh --replay runs/stage1-defeat.txt         # Windows: .\run.ps1 --replay runs\stage1-defeat.txt
```

| Paid | Revenue | Avg satisfaction | Unhappy departures | Result |
|---|---|---|---|---|
| 4 | $51.00 | 52.0 | 0 | **DEFEAT** |

Orders are taken late and READY dishes wait on the pass. Paid count and revenue still
meet their targets, but average satisfaction does not, so the game is lost.

### Rule checks

| Check | How | Expected |
|---|---|---|
| Worked example in the brief (§3.3) | Ignore C1 and C2; from turn 7 seat C3, then order + cook, try to serve (refused), cook, serve | C3: 92, 84, 76, 76, 76, pays $18.00 at 76 |
| Dish finished this turn | Serve on the turn the Chef finishes it | "not ready yet" — the Waiter acts before the Chef |
| Invalid choices | Type `banana`, `9`, `seat C1`, `seat C9 T1`, `cook C1` as the Waiter | A reason each time, then the same staff member chooses again |
| Ignored customer | Seat C1, take the order, then wait every turn | C1 leaves on turn 13 at 0; order cancelled; table freed; no revenue |
| Turn-16 customer | Serve C6 as fast as possible | Cannot pay before the game ends (expected by the brief) |
| Replay file too short | Replay a file with only a few lines | "The input ended before the game finished.", exit code 3 |
