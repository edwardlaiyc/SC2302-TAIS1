# Restaurant Rush: Java Game Design Chat

Exported on 2026-11-01. A step-by-step build of a turn-based command-line restaurant management game in Java 25 (ECB architecture).

---

## User

I am building a turn-based restaurant management game for the command line in Java.
The project runs on Java 25 (using standard javac, no external libraries, no frameworks, no unit test frameworks).

Architectural rules:
- Entity-Control-Boundary (ECB) design:
  * rush.boundary: Terminal UI, user input parsing, ANSI colors. No game logic.
  * rush.control: Turn execution loop, arrival schedules, game configuration. No game arithmetic.
  * rush.entity: Domain data, state machines, business and pricing rules.
- Pure Object-Oriented Principles:
  * Single Responsibility: Each class has one clear job.
  * Encapsulation: Package-private or private state; immutable value objects where appropriate.
  * Polymorphism: Use abstract methods and overrides instead of `instanceof` or enum switches.
  * checked exceptions for player input errors so an invalid move does not consume a turn.
- Code style: Plain, clean, readable Java with concise comments.

Let's build my component step by step. Do not write all the code at once. Wait for my instruction before each class.

## Claude

Understood. I'll write one class at a time and wait for your instruction before each one.

How I'll work under your rules:

- **`rush.boundary`** handles terminal output, input parsing, and ANSI colors, with no game logic.
- **`rush.control`** handles the turn loop, arrival schedules, and configuration, with no game arithmetic.
- **`rush.entity`** handles domain data, state machines, and pricing and business rules. Value objects are immutable, and polymorphism replaces `instanceof` and enum switches.
- **Input errors** use checked exceptions, so an invalid move doesn't consume a turn.
- **Style** is plain Java 25 with concise comments, standard `javac`, and no external libraries.

Which class should I write first? I'd start with a small entity, such as a value object or a base abstract type, and build outward from there. You choose, though.

---

## User

In Stage 3, we add a third staff member: the `Host`.
- The Host is a `Staff` member whose only duty is seating waiting customers.
- Instead of writing new seating logic, the Host reuses `SeatTask`.
- The Host acts first in the turn, before the Waiter and Chef.
- This allows the Host to seat a customer at the start of turn T, so the Waiter can immediately take that customer's order on the same turn.

Show the `Host` class and demonstrate how placing the Host first in `GameConfig.stage3()` achieves this without altering the controller's staff loop.

## Claude

I don't have your `Staff`, `SeatTask` or `GameConfig` source in this chat, so I wrote against the shapes below. If yours differ, paste them and I'll adjust.

**Assumed shapes**
- `Staff` takes a name and an ordered list of duties in its constructor.
- `SeatTask` has a no-argument constructor, because the seating logic already lives inside it.
- `GameConfig` holds an ordered `List<Staff>`.
- The controller acts on staff in list order.

**`rush/entity/Host.java`**

```java
package rush.entity;

import java.util.List;

/** Staff member whose only duty is seating waiting customers. */
public class Host extends Staff {

    public Host(String name) {
        // Reuse the existing seating logic; the Host adds no rules of its own.
        super(name, List.of(new SeatTask()));
    }
}
```

**`rush/control/GameConfig.java` (only `stage3()` shown)**

```java
public static GameConfig stage3() {
    // Order matters: the controller acts on staff in this order each turn.
    // Host seats first, then the Waiter can take that customer's order the same turn.
    List<Staff> staff = List.of(
            new Host("Hana"),
            new Waiter("Wei"),
            new Chef("Chen"));

    return new GameConfig(staff /* , other stage 3 settings */);
}
```

**Controller loop (unchanged)**

```java
for (Staff member : config.staff()) {
    member.takeTurn(restaurant);
}
```

**Why this works**
- **Turn T:** the Host's `SeatTask` moves a waiting customer to seated. The Waiter acts next and finds that customer seated, so the order is taken on the same turn. The Chef acts last and can start any order that is already in.
- **No controller change:** the loop never names a staff type. Turn order comes entirely from the list order in `GameConfig`.
- **Ordering guarantee:** `List.of` keeps insertion order. Use a `List`, not a `Set`, and have `GameConfig` copy it with `List.copyOf`, which preserves order.

Tell me which class to write next.

---

## User

Now let's implement `ComboMeal`:
- A `ComboMeal` extends `MenuItem`.
- It is created by combining two existing `MenuItem` instances (aggregation).
- Its preparation units equal the sum of both dishes.
- Its price is set to 90% of the sum of both dishes, rounded half-up to the nearest cent.
- In the menu, register: Combo 1 (Salad + Burger: $21.60, 3 units), Combo 2 (Salad + Pasta: $24.30, 3 units), and Combo 3 (Burger + Pasta: $29.70, 4 units).

Show the `ComboMeal` class and explain why using aggregation is better than hardcoding combination dishes.

## Claude

The $21.60, $24.30 and $29.70 prices and the 3, 3 and 4 units imply base dishes of **Salad $9.00 / 1 unit, Burger $15.00 / 2 units, Pasta $18.00 / 2 units**. Please check these match your existing menu. I also had to guess `MenuItem`'s API, so I assumed a constructor `MenuItem(String name, int priceCents, int prepUnits)` and accessors `name()`, `priceCents()` and `prepUnits()`. Prices are in whole cents, so there is no floating-point rounding.

**`rush/entity/ComboMeal.java`**

```java
package rush.entity;

import java.util.List;

/** A menu item built from two existing items, sold at a 10% discount. */
public class ComboMeal extends MenuItem {

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(String name, MenuItem first, MenuItem second) {
        super(name, discountedPrice(first, second), first.prepUnits() + second.prepUnits());
        this.first = first;
        this.second = second;
    }

    /** The two dishes this combo is made of. */
    public List<MenuItem> components() {
        return List.of(first, second);
    }

    // 90% of the sum, rounded half-up to the cent (integer math, no floats).
    private static int discountedPrice(MenuItem a, MenuItem b) {
        int sumCents = a.priceCents() + b.priceCents();
        return (sumCents * 9 + 5) / 10;
    }
}
```

**Menu registration** (the `Menu` class and the concrete dish constructors are my assumptions):

```java
MenuItem salad  = new MenuItem("Salad",   900, 1);
MenuItem burger = new MenuItem("Burger", 1500, 2);
MenuItem pasta  = new MenuItem("Pasta",  1800, 2);

menu.add(new ComboMeal("Combo 1", salad,  burger));  // $21.60, 3 units
menu.add(new ComboMeal("Combo 2", salad,  pasta));   // $24.30, 3 units
menu.add(new ComboMeal("Combo 3", burger, pasta));   // $29.70, 4 units
```

Checked by hand: 2400 → 2160, 2700 → 2430 and 3300 → 2970 cents. The half-up rule gives 45 → 5 on a tie, since `(45 + 5) / 10 = 5`.

**Why aggregation beats hardcoding combos**
- **One source of truth.** The combo reads its price and units from the dishes it is built from. If Burger goes to $16, `Combo 1` and `Combo 3` follow when the menu is built, with no second set of numbers to keep in sync.
- **No new class per combo.** Any pairing is just `new ComboMeal(...)`. A hardcoded `SaladBurgerCombo` would need a new class for every pairing, which breaks Single Responsibility and adds boilerplate.
- **The discount rule lives in one place.** Changing 90% to 85% means editing `discountedPrice`, not hunting through combo definitions.
- **Polymorphism.** A `ComboMeal` is a `MenuItem`, so the order, kitchen and billing code treat it like any other dish, with no `instanceof` or type switches. `components()` is there if the UI or receipt wants to list what is inside.

Because the values are computed in the constructor, a combo is a snapshot of its dishes at creation. That is fine while `MenuItem` is immutable. If dishes ever become editable, combos would need to be rebuilt.

Tell me which class to write next.

---

## User

Now let's implement Happy Hour:
- Rules:
  * Can be activated once per game by the manager at the start of any turn (after arrivals, before staff tasks).
  * Lasts for 2 active turns.
  * When active: any customer currently waiting (in queue or seated without food) immediately recovers +15 satisfaction points at the start of each active turn.
  * Any order taken during Happy Hour receives a 20% discount.
  * If a VIP customer orders during Happy Hour, apply the VIP 10% discount first, then apply the Happy Hour 20% discount, rounding half-up at each step.
- Implementation challenge: Order prices were previously fixed by Customer alone. Now they depend on restaurant-level promotion state.
- Refactor `Restaurant.takeOrder()` so it calculates and locks the final price when the order is placed.

Show the `HappyHour` class and the updated `Restaurant.takeOrder()` logic.

## Claude

I couldn't see your `Restaurant`, `Customer` or `Order`, so these are the shapes I assumed:

- `Customer.priceFor(MenuItem)` returns cents. `VipCustomer` overrides it to apply the 10% discount, rounded half-up, so `Restaurant` never needs to know who is VIP.
- `Customer.isWaiting()` and `recoverSatisfaction(int)` exist, and the latter clamps at the maximum.
- `Order` takes its final price in the constructor and stays immutable.
- `Restaurant` has a `customers` collection.

**`rush/entity/HappyHourException.java`** (a checked exception, so a bad activation doesn't consume a turn)

```java
package rush.entity;

/** Thrown when the manager tries to start Happy Hour illegally. */
public class HappyHourException extends Exception {
    public HappyHourException(String message) {
        super(message);
    }
}
```

**`rush/entity/HappyHour.java`**

```java
package rush.entity;

/** One-shot promotion: lasts 2 turns, boosts waiting customers, discounts orders. */
public class HappyHour {

    private static final int DURATION_TURNS = 2;
    private static final int SATISFACTION_BOOST = 15;
    private static final int DISCOUNT_PERCENT = 20;

    private boolean used;        // true once activated, even after it ends
    private int turnsRemaining;

    /** Starts Happy Hour. Allowed once per game. */
    public void activate() throws HappyHourException {
        if (used) {
            throw new HappyHourException("Happy Hour has already been used this game.");
        }
        used = true;
        turnsRemaining = DURATION_TURNS;
    }

    public boolean isActive() {
        return turnsRemaining > 0;
    }

    /** Satisfaction points waiting customers recover this turn (0 when inactive). */
    public int satisfactionBoost() {
        return isActive() ? SATISFACTION_BOOST : 0;
    }

    /** Price after the Happy Hour discount, rounded half-up to the cent. */
    public int apply(int priceCents) {
        if (!isActive()) {
            return priceCents;
        }
        return (priceCents * (100 - DISCOUNT_PERCENT) + 50) / 100;
    }

    /** Counts down one active turn. */
    public void endTurn() {
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
    }
}
```

**`rush/entity/Restaurant.java`** (changed parts only)

```java
private final HappyHour happyHour = new HappyHour();

/** Manager command. Active turn 1 starts now, so waiting customers recover at once. */
public void activateHappyHour() throws HappyHourException {
    happyHour.activate();
    recoverWaitingCustomers();
}

/** Called each turn after arrivals, before the manager and staff act. */
public void startTurn() {
    recoverWaitingCustomers();
}

/** Called at the end of each turn. */
public void endTurn() {
    happyHour.endTurn();
}

private void recoverWaitingCustomers() {
    int boost = happyHour.satisfactionBoost();
    for (Customer customer : customers) {
        if (customer.isWaiting()) {
            customer.recoverSatisfaction(boost);
        }
    }
}

public Order takeOrder(Customer customer, MenuItem item) /* existing checked exceptions */ {
    // ... existing validation (customer seated, item on the menu) stays here ...

    int customerPrice = customer.priceFor(item);       // step 1: VIP 10%, if any
    int finalPrice = happyHour.apply(customerPrice);   // step 2: Happy Hour 20%

    // The price is locked here; a later change in promotion state can't alter it.
    Order order = new Order(customer, item, finalPrice);
    // ... existing bookkeeping (attach to customer, queue for the kitchen) ...
    return order;
}
```

**How the rules map**
- **Duration:** activated on turn T, it gives a boost at activation, and `endTurn` goes 2→1. On T+1 `startTurn` gives the second boost, and `endTurn` goes 1→0. On T+2 nothing applies. `startTurn` has already run on turn T, so there is no double boost.
- **Discount timing:** orders on T and T+1 get 20% off. Orders from T+2 on are full price.
- **Discount order:** `customer.priceFor` runs first and `happyHour.apply` second, which gives VIP then Happy Hour with half-up rounding at each step. A VIP Burger is 1500 → 1350 → 1080, so $10.80. A VIP Combo 2 is 2430 → 2187 → 1750, so $17.50 (1749.6 rounds up).
- **Input error:** a second activation throws `HappyHourException`. Your input parser catches it and the turn isn't consumed.

**Controller:** it needs `restaurant.startTurn()` right after arrivals and `restaurant.endTurn()` at the end of the turn. These are plain calls with no arithmetic. If those hooks already exist, only their bodies change.

Tell me which class to write next.

---

## User

Now quiz me on the code we just wrote:
1. Ask me 3 challenging questions about why we chose these specific OOP designs over alternatives.
2. Ask me about one edge case that could cause a bug if not guarded properly.
3. Review my answers and give me concise explanations that I can use in my presentation video.

## Claude

Answer in your own words, as if you were explaining it on camera. Short answers are fine. Send all four together and I'll review them.

**Q1. Inheritance plus aggregation (`ComboMeal`)**
`ComboMeal` both *extends* `MenuItem` and *holds references* to two other `MenuItem`s. Why use both, rather than a separate `Combo` class that isn't a `MenuItem`, or a `MenuItem` subclass per combo? Name one real downside of the design you chose.

**Q2. Where pricing knowledge lives**
The VIP 10% discount sits in `Customer.priceFor` (overridden by `VipCustomer`), but the Happy Hour 20% sits in `HappyHour`, applied by `Restaurant`. Why split them like this? What would go wrong in each of these alternatives?
- (a) Both discounts live in `Customer`.
- (b) Both live in `Restaurant`, using a type check for VIPs.

**Q3. Locking the price at `takeOrder()`**
We lock the final price in the `Order` when it's placed, instead of computing it when the bill is paid. Using a customer who orders on the last Happy Hour turn and is served two turns later, explain why this matters. What design principle does it protect?

**Q4. Edge case**
Suppose someone changes the controller so `restaurant.startTurn()` runs *after* the manager's command instead of before it. Which bug appears, on which turn, and how large is it? Then give one way to guard against it.

---

## User

**Q1. ComboMeal extends MenuItem and also holds two MenuItems**
A combo is a menu item, because the customer orders it from the same menu as a burger. A combo also contains items, because its price and prep time come from the pieces inside it. Extending MenuItem gives me the "is a" relationship, so the menu, the order and the kitchen treat every item the same way. Holding two MenuItems gives me the "has a" relationship, so the combo's price and prep time come from the parts instead of being hardcoded.
If the combo weren't a MenuItem, every place that handles items would need a second code path, or an `instanceof` check, which I'm avoiding. If I made one subclass per combo, I'd need a new class for every combination on the menu.
The downside is that nothing stops someone putting a combo inside a combo, so the constructor needs a guard, or the design allows nesting on purpose. Inheritance also ties ComboMeal to MenuItem, so a change to MenuItem can ripple into it.

**Q2. Why VIP lives in Customer and Happy Hour lives in HappyHour**
The VIP discount depends on who the customer is, so it belongs to the customer. `VipCustomer` overrides `priceFor`, and the restaurant just asks for a price without caring who is VIP. Happy Hour depends on when it is, and it is the same for everyone. It has its own state, a turn countdown and a once-per-game flag, so it gets its own class, which the restaurant applies.
(a) Both in Customer: Every customer would need to know the restaurant's promotion state. The once-per-game rule would be spread across many objects instead of kept in one, and the countdown would have to reach each of them.
(b) Both in Restaurant with a type check: `instanceof VipCustomer` breaks my polymorphism rule. Adding a new customer type, say a student, would mean editing Restaurant again, and Restaurant would end up knowing too much.

**Q3. Locking the price when the order is placed**
Say a customer orders on the last Happy Hour turn, and the dish is served two turns later. Happy Hour is over by then. If I calculated the price at payment, they'd order at 20% off and then be billed full price. The player would see the same order cost different amounts depending on when it was paid, which feels like a bug.
Locking the price in the `Order` when it's created means the price is quoted once and never changes. This protects two principles. One is immutability, since the Order is a value object that doesn't change after creation. The other is a single source of truth: the Order doesn't need to know what the promotion state is today, because it already holds the answer.

**Q4. Edge case: startTurn after the manager's command**
`activateHappyHour()` already boosts waiting customers the moment it runs. If `startTurn()` runs after the command, it boosts them again on the same turn. On the activation turn customers get +30 instead of +15, a 15-point overshoot. The next turn is normal, so across the promotion that's 45 instead of 30. It's easy to miss because satisfaction is clamped at the max, so the bug only shows when customers aren't near full.
The fix is to make the boost happen at most once per turn, whatever order things run in. Restaurant keeps a `boostedThisTurn` flag. `recoverWaitingCustomers()` returns early if it's set, and `endTurn()` clears it. Then the controller's call order can't double-count.

## Claude

Your answers are strong overall. The only real fix is in Q4, where your guard has a subtle trap.

### Q1: ComboMeal (very good)
You covered is-a vs has-a, the `instanceof` cost, and the class-per-combo cost. Two refinements:
- **Nesting isn't just unguarded, it also compounds the discount.** A combo inside a combo would get 10% off twice, about 19% off the inner dishes. If you guard against it without `instanceof`, add a method such as `canBeComponent()` to `MenuItem`, true by default, and have `ComboMeal` override it to false.
- **A better downside than "ripple effects" is the snapshot.** The combo copies price and units at construction, so it isn't live. That's safe only while `MenuItem` is immutable.

**Presentation line:** "A combo *is* a menu item, so the whole system treats it like any other dish. It also *has* two dishes, so its price and prep time are derived from them and never copied by hand. The cost is that it's a snapshot, so we keep dishes immutable and block nesting."

### Q2: Who owns which discount (excellent)
Your split is right: VIP depends on *who* the customer is, and Happy Hour depends on *when* it is. Add one point to (b). Putting both in `Restaurant` would also make it own the VIP percentage, a rule that belongs to the customer type.

**Presentation line:** "Who you are is the customer's business. When you order is the restaurant's. VIP uses polymorphism, so adding a new customer type never touches `Restaurant`. Happy Hour owns its own countdown and once-per-game rule, so that state lives in exactly one place."

### Q3: Locking the price (good, one wording fix)
The scenario and the immutability point are right. "Single source of truth" is slightly off, because the real principle is **no temporal coupling**. The price is a historical fact about the moment of ordering, so `Order` never depends on `Restaurant`'s state later. Rounding also runs once, so the total can't drift.

**Presentation line:** "The price is a quote, so we lock it when the order is placed. Whatever the player was promised is what they pay, even if Happy Hour ends before the food arrives. `Order` stays immutable and never has to ask the restaurant what the promotion state was."

### Q4: Double boost (correct diagnosis, but check your fix)
Your numbers are right: +30 instead of +15 on the activation turn, and 45 instead of 30 over the promotion. Your clamp observation is also a good point.

**One trap in the guard.** `startTurn()` runs first on the activation turn, while Happy Hour is still inactive. If `recoverWaitingCustomers()` sets `boostedThisTurn = true` on *every* call, that no-op call sets the flag. Activation's own boost is then skipped, and customers get *no* boost on turn 1. The flag must be set only when a boost above 0 is actually applied.

**A cleaner guard** moves the state into `HappyHour`, so call order can't matter:

```java
private boolean boostPending;

public void activate() throws HappyHourException {
    // ... existing checks ...
    turnsRemaining = DURATION_TURNS;
    boostPending = true;                  // active turn 1 is owed a boost
}

/** Returns the boost once per active turn, then 0 until the next turn. */
public int consumeBoost() {
    int boost = boostPending ? SATISFACTION_BOOST : 0;
    boostPending = false;
    return boost;
}

public void endTurn() {
    if (turnsRemaining > 0) {
        turnsRemaining--;
    }
    boostPending = isActive();            // next active turn is owed one
}
```

`Restaurant` calls `consumeBoost()` instead of `satisfactionBoost()`. Each active turn pays out exactly once, however the controller orders `startTurn()` and `activateHappyHour()`.

**Presentation line:** "A bug like this hides because satisfaction is capped, so it only shows when customers aren't near full. Instead of trusting the controller's call order, we make the boost *consumable*: each active turn creates one boost, and it can only be spent once. The rule is enforced by the entity, not by call order."

If you want to demo this in the video, a short `main` that calls the two methods in both orders and prints the satisfaction is enough, since you aren't using a test framework.

Tell me which class to write next.
