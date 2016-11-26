# A multi-threaded Store

## Model

A store is modeled with the concept of multiple threads of logic taking and adding items from an inventory,
enqueuing and dequeuing shoppers in various states of executing a "shopping run", and reporting on the results of the shopping run.

## Programmatic Flow

### Store
 1. Store loads inventory and prepares to open ___(thread 0)___
 2. The Store creates a pool of Registers and starts them running in a thread pool. They spin in a tight loop until they receive Shoppers. __(thread 0 --> Thread i..z)__
 3. Once the Store closes:
    1. No more Shoppers are allowed to enter the _start_ phase.
    2. All known Shoppers in the Store are allowed to finish shopping and exit.
    3. Register threads are told to stop.
 
### Shopper
 1. Shoppers are spawned and initialized with a list of items (by name) and desired quantities __(thread 0 --> thread 1..n)__
    * Each shopper (in no assumed order of execution) independently executes the steps of shopping (in this order), in its own thread of execution ___(Thread1..n)___
       1. _start phase:_ to start shopping: a call to the Store begins this phase
          * If the store is open, we expect to proceed
          * If the Store is closed, we may __wait__ (the desired behavior is included with our message to the store asking to start this phase)
             * If we wait, Store puts Shopper into a thread-safe queue, preserving order of arrival into line (which may not exactly be order of execution of the _start_ phase)
             * the Store may have a limited capacity queue of waiting Shoppers; in that case, the _try_ step fails fast.
          * If our initial _try_ fails, (either because we did not choose to wait, or were denied permission to do so), the Shopper thread terminates
       2. _shop_ phase:
          * Initiated by Store when a Shopper may proceed shopping (the Shopper thread must not proceed until then)
          * The Store will add Shoppers to a known Shopper collection before signalling this phase.
 
 3. The Store remains closed. possibly queuing up Shopper as it stays in this state. ___(thread 0)___
 4. Once the Store opens:
    1. the "open" flag is atomically flipped -- at that point, no more Shoppers should ever possibly enter the wait queue
    2. The queue of waiting Shoppers is drained to a "known Shoppers" collection.
    3. Each Shopper has its _shop_ phase intiated __(thread 0)__
    
 5. Now each Shopper thread executes the steps of the _shop_ phase. __(thread 1..n)__
    1. Try to add items from the shopping list to the Cart by querying the Store
    2. Decrement items on the shopping list
    3. Once all items are added to the Cart, enter _checkout_ phase
    
 6. _checkout_ phase
    * The Shopper tries to enter a Register line through a call back to the Store. This is presumed to always succeed.
    * which Register the Shopper gets added to is not defined
    * Once added to a Register line, the Register executes the checkout steps.
    * Once the checkout of a Shopper is done, the Register signals this to the Shopper and they exit the Store
    
 7. _exit_ phase
  * tell Store we're exiting
  * terminate execution
  
  ### Register
  
  1. Started up by Store
  2. Spins in a loop until stopped:
   * __if__ is a Shopper in line
      1. deque Shopper
      2. remove Items from their Cart
         * Lookup the Item through the Store
         * Add the Item to the Reciept
         * attach the Receipt to the Shopper
         * signal _exit_ to the Shopper
  
    
 
