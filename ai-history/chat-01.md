# Restaurant Rush: Stage 3 Diagrams Chat

Exported on 2026-11-12. Edward's session: project setup, the Stage 3 PlantUML class diagram, the Turn 10–15 sequence diagram, and a design quiz for the video.

---

## User

```text
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
  * Polymorphism: Use abstract methods and overrides instead of instanceof or enum switches.
  * checked exceptions for player input errors so an invalid move does not consume a turn.
- Code style: Plain, clean, readable Java with concise comments.

Let's build my component step by step. Do not write all the code at once. Wait for my instruction before each class.
```

## Claude

Sounds good. I'll build this one class at a time and wait for your go-ahead before each. Before you pick the first class, here's how I'm reading your rules, so you can correct me early.

**Package responsibilities**

- **`rush.entity`**: all game data and rules. Customers, orders, dishes, tables, the restaurant's money, pricing, and state machines (e.g., a customer moving Waiting → Seated → Ordered → Served → Paid).
- **`rush.control`**: the turn loop, scheduled arrivals, and game config (starting cash, number of turns, etc.). It orchestrates by calling entity methods and never computes prices or outcomes itself.
- **`rush.boundary`**: prints text, applies ANSI colors, reads and parses input into commands. It never decides anything.

**Design decisions I'd propose**

- **Checked exceptions:** A base `InvalidMoveException extends Exception` in `rush.entity` (the rules live there), with specific subclasses like `NoFreeTableException`. The boundary catches them, shows the message, and re-prompts, so the turn doesn't advance.
- **Polymorphism over switches:** Behavior that varies (customer types, dish types, state transitions) lives in abstract methods or interfaces, never `instanceof` or `switch` on an enum.
- **Commands:** Player actions as a `Command` interface (`execute(Restaurant) throws InvalidMoveException`). The boundary parses text into a `Command`, the control layer runs it, and the entity enforces the rules.
- **Immutability:** Value objects (e.g., `Money`, `Dish`) as final classes or records, with all state fields private or package-private.
- **Time:** Everything advances through a tick-style method on entities, driven by the control loop.

**A possible build order** (you can reorder or change it):

1. `Money` (immutable value object)
2. `InvalidMoveException` (+ a first subclass)
3. `Dish` / menu
4. `Customer` (abstract) + state machine
5. `Table`
6. `Restaurant` (aggregate root)
7. `Command` interface + concrete commands
8. `GameConfig` and arrival schedule (control)
9. `TurnLoop` (control)
10. Terminal UI, ANSI helper, input parser (boundary)
11. `Main`

A few questions would shape the early classes. Answer whichever matter to you:

- What's the core fantasy? (e.g., seat customers, take orders, cook, serve, collect payment, with patience running out)
- Is there a kitchen/chef mechanic, or are dishes served instantly?
- Do you want me to keep the root package as `rush` (e.g., `rush.Main`)?

Tell me which class to write first, or say "start with your order" and I'll begin with `Money`.

---

## User

```text
I need to generate PlantUML source code for our Stage 3 architecture.
Classes:
- Boundary: ConsoleView, CommandInput, SatisfactionLog, Colors
- Control: GameController, GameConfig, ArrivalSchedule, ManagerInput, GameListener
- Entity: Restaurant, Table, WaitingQueue, Staff, Waiter, Chef, Host, Cashier, Customer, RegularCustomer, VIPCustomer, CriticCustomer, Order, Menu, MenuItem, ComboMeal, Money, HappyHour, Objective, Task hierarchy.

Generate a clean PlantUML class diagram using skinparam monochrome/clean styles with clear composition (*--), aggregation (o--), inheritance (--|>), and dependency (..>) relationships.
```

## Claude

I've written the Stage 3 diagram to `rush_stage3.puml`. It uses a monochrome, clean style and groups the classes into `rush.boundary`, `rush.control` and `rush.entity`.

**Choices I made where your list was open-ended**

- **Task hierarchy:** You didn't name the subclasses, so I invented three: `RevenueTask`, `CustomersServedTask` and `SatisfactionTask`. Rename or replace them as needed.
- **Interfaces:** `GameListener` and `ManagerInput` are interfaces in `rush.control`. `ConsoleView` and `SatisfactionLog` implement `GameListener`, and `CommandInput` implements `ManagerInput`. These use the dotted-arrow realization (`..|>`), since `--|>` is for class inheritance. This keeps the dependency direction ECB-friendly, because control never depends on boundary.
- **Composition vs aggregation:**
  - Composition: `Restaurant` owns its `Table`s, `WaitingQueue`, `Menu` and cash `Money`. `Customer` owns its `Order`, and `Objective` owns its `Task`s.
  - Aggregation: shared or replaceable things such as `Staff`, `HappyHour`, `MenuItem` in an `Order` or `ComboMeal`, and the `Customer` in a `Table` or `WaitingQueue`.
- **Polymorphism:** `Customer` and `Task` have abstract methods (for example `maxPatience()` and `isDone()`) instead of type checks. `ComboMeal` overrides `price()`. `HappyHour` applies the discount to `Money`.
- **Not drawn:** `Command`, `InvalidMoveException`, `CustomerState` and `OrderStatus` weren't in your list, so they appear only as method or field types. If you want them as classes, say so and I'll add them.

To render it, paste the contents into plantuml.com or use the VS Code PlantUML extension. If the layout feels cramped, change `left to right direction` to top-to-bottom by deleting that line.

When you're ready to continue the Java build, tell me which class to write first.

> 📎 **Attachment:** `rush_stage3.puml` (PUML)

---

## User

```text
Next, generate a PlantUML sequence diagram for Turn 10 to Turn 13 of our game:
- Turn 10: C4 (Critic) arrives. Manager triggers Happy Hour (+15 recovery). Host seats C4. Waiter takes order (Burger + Pasta combo, price locked at $23.76).
- Turn 11-12: Chef cooks 1 unit each turn. Order status becomes READY on Turn 12.
- Turn 13: Manager decision point: Waiter serves C4 immediately (avoiding the cold food penalty).
- Turn 14-15: Eating, Cashier collection, departure.

Show the interactions between Manager, GameController, Host, Waiter, Chef, Restaurant, Customer, and Cashier.
```

## Claude

I've written the sequence diagram to `rush_turns_10_15.puml`. It uses the same monochrome style as the class diagram and has numbered messages with a section divider for each turn.

**How I read the scenario**

- **Turn 10:** The `GameController` creates C4 from the arrival schedule and hands it to the `Restaurant`. The Manager's Happy Hour command goes through the controller to `Restaurant.startHappyHour()`, which applies the +15 recovery to the customer. The `Host` then seats C4, and the `Waiter` takes the order. The combo price of $23.76 is locked when the `Order` is created.
- **Turns 11 and 12:** The `Chef` cooks one unit per turn. I treated the Burger + Pasta combo as two units, so the order moves to `READY` at the end of Turn 12.
- **Turn 13:** An `alt` block shows the decision. The chosen branch has the `Waiter` serve immediately with no penalty. The other branch is greyed as "not taken" and notes the cold food penalty.
- **Turns 14 and 15:** C4 eats on Turn 14. On Turn 15 the `Cashier` bills C4, and the `Restaurant` adds the locked $23.76 to its cash, releases the table and ends the customer's lifecycle.

**Assumptions to check**

- **Recovery:** I assumed the +15 recovery applies to patience. If it's satisfaction, change the `recoverPatience(15)` call.
- **Eating time:** I gave eating one full turn (Turn 14), with the customer ready to pay on Turn 15.
- **Orchestration:** The controller only calls `work(restaurant)` and `tick()`. All pricing, state changes and cash arithmetic happen inside `Restaurant`, `Order` and `Customer`, which follows your ECB rules.
- **Tips:** I left out any tip or critic review score, since you didn't give numbers. I can add them if you want.

Whenever you're ready to go back to the Java build, tell me which class to write first.

> 📎 **Attachment:** `rush_turns_10_15.puml` (PUML)

---

## User

```text
Now quiz me on the code/diagrams we just wrote:
1. Ask me 3 challenging questions about why we chose these specific OOP designs over alternatives.
2. Ask me about one edge case that could cause a bug if not guarded properly.
3. Review my answers and give me concise explanations that I can use in my presentation video.
```

## Claude

Here's your quiz. Answer in a few sentences each, and I'll review them all together once you reply.

**Q1: Polymorphism vs. type checks.**
`Customer` is abstract, with `RegularCustomer`, `VIPCustomer` and `CriticCustomer` overriding methods like `maxPatience()` and `tipRate()`. A simpler design would be one `Customer` class with a `CustomerType` enum and a `switch` in the logic. Why is our approach better for this game, and what is the one real cost of it?

**Q2: Interfaces in control, implemented by boundary.**
`GameListener` and `ManagerInput` live in `rush.control`, but `ConsoleView`, `SatisfactionLog` and `CommandInput` live in `rush.boundary` and implement them. Why not let `GameController` call `ConsoleView` directly? Name the principle and one concrete benefit for this project.

**Q3: Locked price and immutable Money.**
In the Turn 10 to 15 scenario, the combo price of $23.76 is locked when the `Order` is created, and `Money` is an immutable value object. What bug would appear if the `Cashier` recomputed the price from `Menu` and `HappyHour` at payment time? And why does immutability make the locked price safer?

**Q4: Edge case.**
Suppose C4's patience hits zero during `Restaurant.tick()` on Turn 12, right as the `Chef` finishes cooking and the order becomes `READY`. C4 leaves and the table is released. On Turn 13 the Manager still types `serve C4`, and the `Chef` and `Cashier` still hold references to that order.

- What could go wrong?
- Which class should own the guard?
- How would the checked-exception design (`InvalidMoveException`) fit into your fix?

Take your time. When you answer, tell me whether the explanations should sound like a short spoken script or a written summary, and I'll match that style in the review.

---

## User

**Q1.** the open/closed principle, so adding a new type would just mean writing a new class for it and not touching any of the existing ones. a cost of this would be: more classes, and could be harder to find out stuff like "what a VIP does?"

**Q2.** dependency inversion principle: high-level policy depends on abstractions, and low-level details depend on those same abstractions. a benefit would be: can swap or fake the UI without touching game logic

**Q3.** bug would be the Cashier recomputes a different amount and charges the customer something other than what was promised. immutability ensures that no other class can accidentally alter the Money value.

**Q4.**

what could go wrong: the Manager serves a customer who is gone, so the food is "delivered" to an empty or reassigned table. The Cashier charges a customer who left (or charges the new customer now seated at that table). Stale references in Chef/Cashier cause a null pointer, double-release of the table, or revenue counted for a lost customer.

Restaurant should own the guard

Because it is a checked exception, the compiler forces the caller (GameController) to handle it. The controller catches it and reports it through GameListener as a friendly message, and the game continues without crashing and without charging anyone. Chef and Cashier should also check the order state before acting, so stale references fail safely too.

## Claude

Your answers are solid overall. Q2 and Q4 are strongest. Q1 and Q3 each need one sharper point. I defaulted to a spoken-script style for the video lines, and I can convert them to written form if you prefer.

**Q1: Polymorphism (strong, with one refinement)**

Open/Closed is the right principle. The cost you named is real but minor, because "what does a VIP do?" is answered by opening `VIPCustomer`. The deeper cost is that adding a new behavior (say `complaintStyle()`) means editing every subclass. The enum and switch alternative also scatters the same type checks across many classes, which your architecture rules forbid.

> "Each customer type is its own class, so adding a new type means adding a class, not editing existing ones. The trade-off is more classes, and a new behavior has to be implemented in every subclass. We accept that because it keeps each customer's rules in one place and avoids type checks spread through the code."

**Q2: Dependency Inversion (strong)**

Correct, and your benefit is the right one. Two things to add. The interfaces are owned by `rush.control`, which is the inversion: boundary depends on control, never the reverse. And one `GameController` can notify several listeners, so `ConsoleView` and `SatisfactionLog` both subscribe without the controller knowing either exists. A scripted `ManagerInput` is also useful, since you have no test framework: it lets you replay a game headlessly.

> "The controller depends only on GameListener and ManagerInput, interfaces that live in the control package. The console classes implement them, so the dependency points inward. We can add a new listener, or replace the keyboard with scripted input for testing, without touching game logic."

**Q3: Locked price and Money (good, but two ideas are blended)**

Your bug is correct. Be concrete: if Happy Hour ends before Turn 15, recomputing at payment would charge full price after promising the discounted one. But the lock and the immutability do different jobs. The `Order` stores the price once in a `final` field, and that is what locks it. Immutability guarantees the stored value can never change, even through shared references, and `plus()` or `minus()` always return new objects, so cash arithmetic cannot corrupt a stored price.

> "The price is captured when the order is created and stored in the Order, so a later menu change or the end of Happy Hour can't alter what the customer was promised. Money is immutable, so that stored value can never be modified by any other class."

**Q4: Edge case (very good, with one correction)**

You found the real bugs: serving a ghost customer, charging the wrong person, stale references, and a double table release. Your choice of `Restaurant` as the guard owner is right, since it's the aggregate root.

The correction is the checked exception. It fits the Manager's mistake: `serve C4` after C4 left is a player input error, so it throws `InvalidMoveException`, the turn isn't consumed, and the player is re-prompted. The `Chef` or `Cashier` holding a stale order is different. That is an internal bug, not a player error, so it shouldn't be a checked exception. Prevent it instead by having `Restaurant` cancel the order and clear staff references when a customer leaves. The order's state machine should then reject invalid transitions. As a safety net, an unchecked `IllegalStateException` is appropriate here.

> "When a customer leaves, the restaurant, as the aggregate root, cancels their order and releases the table exactly once. If the player then tries to serve that customer, it throws a checked InvalidMoveException, so no turn is lost. Internal inconsistencies are prevented by the state machine, not left to player-facing exceptions."

Next, I can write the Java for one of these pieces (for example `Money`, `InvalidMoveException`, or the `Order` state machine). Which would you like to start with?
