## User

I'm doing Stage 2 of our Java restaurant game. Stage 1 already works. My part is Regular, VIP and Critic customers, then the replay files. We use Java 25, plain javac and no libraries. There are boundary, control and entity packages. Game rules are supposed to stay out of the console.

I have a scratch copy of Stage 1. Can we do this a bit at a time? I don't want a whole project generated at once because I need to explain my part in the video.

## Assistant

Yes. Start with the existing `Customer` class, especially how it loses satisfaction and creates an order. We need to find the places where Stage 1 assumes every customer behaves identically.

We will keep seating, eating and payment shared. Stage 2 should change the customer's behaviour without teaching `GameController` about individual customer types.

What rules does your assignment give for each type?

## User

Regular loses 8 and pays full price. VIP gets 10% off. The AI guide says VIP loses 4 but the actual assignment says 5. Critic loses 12 and we choose some special behaviour for it.

## Assistant

Use **5** for the VIP. The assignment is the requirement; the guide's value conflicts with it.

We can make `Customer` abstract and introduce these methods:

```java
public abstract String getType();

public abstract int getSatisfactionLoss();

public Money getBill(MenuItem item) {
    return item.getPrice();
}
```

`getType()` supplies the display label. `getSatisfactionLoss()` supplies each type's waiting penalty. `getBill()` gives the ordinary full price unless a subclass changes it.

## User

The guide says to abstract the bill calculation too. Why isn't getBill abstract here?

## Assistant

It could be abstract, but then Regular and Critic would each implement the same full-price calculation. A concrete default lets both inherit that rule while the VIP overrides it.

An abstract class can contain both implemented and abstract methods. Being abstract does not require every operation to be abstract.

The useful question is which behaviour must differ. Every customer type supplies a loss and label. Only the VIP currently needs different billing.

## User

Okay. Here are the bits from my current Customer:

```java
private static final int SATISFACTION_LOSS = 8;

boolean loseSatisfaction() {
    satisfaction = Math.max(0, satisfaction - SATISFACTION_LOSS);
    return satisfaction == 0;
}

Order placeOrder(Menu menu, int turn) {
    MenuItem item = chooseItem(menu);
    order = new Order(this, item, item.getPrice(), turn);
    state = CustomerState.ORDERED;
    return order;
}
```

What actually needs replacing?

## Assistant

Change the class declaration to `public abstract class Customer`, make its constructor `protected`, and remove the fixed `SATISFACTION_LOSS` constant. Add the methods above, then replace those two sections with:

```java
boolean loseSatisfaction() {
    reduceSatisfaction(getSatisfactionLoss());
    return satisfaction == 0;
}

protected void reduceSatisfaction(int amount) {
    satisfaction = Math.max(0, satisfaction - amount);
}

Order placeOrder(Menu menu, int turn) {
    MenuItem item = chooseItem(menu);
    order = new Order(this, item, getBill(item), turn);
    state = CustomerState.ORDERED;
    return order;
}
```

Keep the remaining state transitions as they are. In particular, the price is still calculated when the order is taken and stored in the `Order`. The cashier later collects that stored price.

`reduceSatisfaction()` will also let the Critic apply its additional service penalty without exposing the satisfaction field.

## User

I was going to make satisfaction protected so the Critic can subtract from it. Is that bad?

## Assistant

It would work, but it would let subclasses assign any value directly, including a negative value. Keeping the field private and exposing a protected operation keeps the lower-bound rule in one place.

The Critic can request a reduction. It does not need permission to replace the customer's entire satisfaction value.

Here is the Regular class first:

```java
package rush.entity;

public class RegularCustomer extends Customer {

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLoss() {
        return 8;
    }
}
```

It inherits full-price billing and the existing food-selection rule.

## User

Regular makes sense. For VIP can I just do price times 0.9?

## Assistant

Keep the calculation in `Money`, using integer cents. In the existing class, add:

```java
public Money percent(int percent) {
    return new Money((cents * percent + 50) / 100);
}
```

For non-negative amounts, adding 50 before integer division rounds half up. For example, 90% of 5 cents is 4.5 cents, which becomes 5 cents. A $15 Burger is 1,500 cents; 90% is 1,350 cents, or $13.50.

This implementation is sufficient for the game's small prices. A general-purpose money class would also need to address multiplication overflow and validate its supported percentage range.

Then the VIP is:

```java
package rush.entity;

public class VIPCustomer extends Customer {

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLoss() {
        return 5;
    }

    @Override
    public Money getBill(MenuItem item) {
        return item.getPrice().percent(90);
    }
}
```

## User

I tried building and ArrivalSchedule says Customer is abstract and cannot be instantiated. There's a new Customer in arrivalsOn. Did making it abstract break Stage 1?

## Assistant

The setup still constructs the old concrete class. Replace that construction with `RegularCustomer` for the Stage 1 setting.

For Stage 2, each arrival slot needs to say which class to construct. The same six arrival turns stay in place: C2 becomes a VIP and C4 becomes a Critic.

A small registration method can accept a constructor reference:

```java
public ArrivalSchedule add(
        int turn,
        BiFunction<Integer, Integer, Customer> customerType) {
    turns.add(turn);
    customerTypes.add(customerType);
    return this;
}
```

Then setup can contain `.add(4, VIPCustomer::new)`. At arrival time, call the stored function with the customer's ID and arrival turn.

Keep an `allRegular()` schedule for Stage 1. Make `GameConfig.stage1()` select that schedule and `stage2()` select the mixed schedule. This changes registration, rather than the turn controller's rules.

## User

BiFunction is new to me. Is VIPCustomer::new creating the VIP immediately?

## Assistant

No. It supplies a function that can create a VIP later.

`BiFunction<Integer, Integer, Customer>` means: accept two integers and return a `Customer`. In this case, the integers are the ID and arrival turn.

These two expressions mean the same thing here:

```java
VIPCustomer::new
```

```java
(id, arrivalTurn) -> new VIPCustomer(id, arrivalTurn)
```

When `arrivalsOn(4)` calls that function, it creates C2. Merely registering the function does not admit a customer early.

## User

Right. Let's do Critic now. I want it to order the longest dish and complain if food is cold. Would checking that it was ready on an earlier turn be enough?

## Assistant

Not quite. Every dish must already have been READY on an earlier turn, because the Waiter acts before the Chef. We need to distinguish the first legal serving turn from an avoidable delay.

If a dish becomes READY on turn 12:

| Served on | Meaning |
|---|---|
| 12 | Too early; serving must be refused |
| 13 | First legal serving turn; hot |
| 14 or later | Delayed service; cold |

The cold-food condition is therefore:

```java
turn > getOrder().getReadyTurn() + 1
```

Expose the existing `readyTurn` through a getter on `Order`. Do not add a separate clock or count real elapsed time.

## User

I wrote >= readyTurn + 1, so mine would penalise turn 13 as well. I'll change that. Where should the penalty go? The guide mentions ServeTask but I don't want an instanceof check there.

## Assistant

Override `startEating()` in `CriticCustomer`. `ServeTask` already calls that operation on the customer, so Java selects the Critic's implementation automatically.

Here is the class:

```java
package rush.entity;

public class CriticCustomer extends Customer {

    private static final int COLD_FOOD_PENALTY = 10;
    private boolean gotColdFood;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLoss() {
        return 12;
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        MenuItem best = menu.get(0);
        for (MenuItem item : menu.getItems()) {
            int units = item.getPreparationUnits();
            if (units > best.getPreparationUnits()
                    || (units == best.getPreparationUnits()
                    && item.getPrice().compareTo(best.getPrice()) > 0)) {
                best = item;
            }
        }
        return best;
    }

    @Override
    void startEating(int turn) {
        super.startEating(turn);
        if (turn > getOrder().getReadyTurn() + 1) {
            gotColdFood = true;
            reduceSatisfaction(COLD_FOOD_PENALTY);
        }
    }

    @Override
    public String getReaction() {
        return gotColdFood ? "the food is cold, -10" : "";
    }
}
```

Add a default `getReaction()` returning an empty string to `Customer`. `ServeTask.getResult()` can append the returned text when it is non-empty. Its validation and execution rules stay unchanged.

## User

Why super.startEating first? And does the boolean stop the penalty from being applied twice?

## Assistant

The parent method performs the shared transition to EATING and records the served turn. The override keeps that behaviour and adds the Critic's reaction.

The boolean records the reaction for display. It does **not** itself prevent a second deduction. The existing serving rules provide that protection: after an order becomes SERVED, another `ServeTask` fails validation because the order is no longer READY.

This assumes service continues to go through the validated task flow. If we later introduced another way to invoke serving, we would need to preserve that guarantee.

Also, `chooseItem()` assumes a non-empty menu, as the existing game does. On equal preparation units, it selects the higher-priced dish; if both values tie, it keeps the earlier item.

## User

I named the reaction method differently at first so @Override gave an error. Fixed that. The code builds now, and I've added the type label to the console.

For testing, should I run the old files with the new mixed customers? The defeat one seems to get out of step when it reaches C4.

## Assistant

There are two separate checks:

1. Run the old replay files with `--setting stage1`. That checks whether Regular-only behaviour still works.
2. Use dedicated Stage 2 replays with `--setting stage2`. Those must account for the new behaviour.

In Stage 1, C4 follows the rotation and chooses Salad, which needs one preparation unit. In Stage 2, C4 is a Critic and chooses Pasta, which needs two. Reusing the old commands with mixed customers can attempt to serve C4 before the second unit is cooked. The refusal then changes which subsequent input gets consumed at each prompt.

Don't change the Chef's rate to make the old script fit. Adjust the Stage 2 decisions.

## User

Can you help me trace just the Critic section? I'm losing track of when the Waiter can do something useful while Pasta cooks.

## Assistant

For the victory sequence:

| Turn | Waiter | Chef | C4's position |
|---|---|---|---|
| 10 | Serve C3 | Wait | C4 arrives and waits |
| 11 | Seat C4 | Wait | Seated |
| 12 | Take C4's order | Cook C4 | Pasta 1/2 |
| 13 | Seat C5 | Cook C4 | Pasta becomes READY |
| 14 | Serve C4 | Wait | First legal serving turn; no cold penalty |
| 15 | Take C5's order | Cook C5 | C4 becomes READY_TO_PAY |
| 16 | Serve C5 | Wait | Cashier collects C4's payment |

On turn 13, the Waiter cannot serve the Pasta yet. Seating C5 uses that turn productively while the Chef finishes C4's dish.

For a defeat sequence, take C4's order on turn 13, cook its Pasta on turns 14 and 15, then leave it waiting on turn 16. Serving it on turn 17 causes the cold-food penalty. It cannot pay before turn 19, after the game has ended.

That last point matters: a cold-food message alone does not prove that the penalty reduced the final paid-customer average. In this defeat replay, C4 never pays within the 18 turns.

## User

I ran these with JDK 25 using sh run.sh because my run.sh isn't executable:

```sh
sh run.sh --setting stage1 --replay runs/stage1-defeat.txt --no-color
sh run.sh --setting stage1 --replay runs/stage1-victory.txt --no-color
sh run.sh --setting stage2 --replay runs/stage2-defeat.txt --no-color
sh run.sh --setting stage2 --replay runs/stage2-victory.txt --no-color
```

I got:

```text
stage1 defeat:  4 paid, $51.00, average 52.0
stage1 victory: 5 paid, $66.00, average 79.2
stage2 defeat:  3 paid, $40.50, average 56.0
stage2 victory: 5 paid, $73.50, average 75.6
```

Those match the notes. Does that mean Stage 2 is fully tested?

## Assistant

They are good regression and end-to-end evidence, but they cover only those four decision sequences.

Before calling the component checked, cover its boundaries as well:

- READY on turn 12, served on turn 13: no cold penalty.
- READY on turn 12, served on turn 14: a 10-point penalty.
- Attempt to serve the same order again: refused, with no second penalty.
- Two equally long dishes: the more expensive one is selected.
- A half-cent discount result: rounds half up.

The final screenshots should show paid count, revenue and average satisfaction. A filename containing `victory` is not itself proof that the game ended in VICTORY.

## User

Let's do the quiz from the guide. Ask the design questions first, then the edge case. Don't give the answers immediately.

## Assistant

1. Why is overriding customer methods preferable here to adding a customer-type switch in `GameController`?
2. Why did we leave `getBill()` implemented in the abstract parent rather than make it abstract?
3. Why does the Critic override `startEating()` instead of putting its penalty calculation inside `ServeTask`?
4. Edge case: a Critic's Pasta becomes READY on turn 12. It is served on turn 13. Should the Critic lose the cold-food penalty, the normal waiting penalty, both, or neither on turn 13? Explain the order of events.

## User

1. A switch would need another case whenever we add a customer. With subclasses the controller calls the same method and the object chooses what happens.

2. Regular and Critic both pay full price so they can share that implementation. Only VIP needs to replace it.

3. The reaction belongs to the Critic. ServeTask should just perform serving and shouldn't have to know all the customer types.

4. No cold penalty because 13 is the first turn it can be served. I think it still loses 12 at the end of turn 13 because it was waiting at the start of that turn?

## Assistant

Your first three answers capture the design accurately. For question 1, mention that adding a type still requires setup registration; the benefit is keeping the controller's existing rules unchanged.

For question 4, the answer is **neither**. The cold-food comparison is false because `13 > 12 + 1` is false. Serving then changes the customer to EATING. At the end of the turn, satisfaction decreases only for customers still waiting for service. The check uses the customer's current state, rather than its state at the beginning of the turn.

A sentence you could use in the video is: “Serving on the first available turn avoids the cold-food penalty, and the customer stops losing waiting satisfaction as soon as eating starts.”

## User

So if I serve on turn 14 instead, it loses 12 at the end of turn 13 and then another 10 when served on turn 14, but not another 12 after that?

## Assistant

Correct, assuming it still has enough satisfaction to remain in the restaurant. Compared with serving on turn 13, that delay costs 22 points: one additional waiting turn and the cold-food penalty. Once served, it is EATING and no longer receives the normal waiting deduction.

Keep “assuming it remains” in mind. A customer that reaches zero while waiting leaves, so a later serving attempt must be refused.

## User

That makes sense now. For the README I'll explain the new subclasses, Money.percent, Order.getReadyTurn, the schedule and display changes. I won't say the whole system needed no edits, just that the controller and staff didn't need changes for Stage 2.

How should the chat be saved for the AI-history requirement?

## Assistant

For a real submission, export the actual complete conversation rather than writing a replacement account of it. Save it as `ai-history/chat-session.md` on your `ai-history/liuyuan` branch. Include the questions, corrections and imperfect answers, since those show how the interaction developed.

Before committing, review the export and the file list. The scratch exercise code stays separate from the game repository unless your team explicitly requests it. Your reflection should distinguish what you wrote, what the AI supplied, and what you personally verified.