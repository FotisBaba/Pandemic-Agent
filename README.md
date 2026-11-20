# Pandemic Agent (Monte Carlo Tree Search)

A team project for Multi-Agent Systems (COMP512) at the Technical University of Crete, 2020–21. It's an AI agent that plays the cooperative board game **Pandemic** with three other agents.

## How the agent works

- **Monte Carlo Tree Search (UCT).** Each turn, the agent builds a search tree from the current board. It selects nodes with UCT, expands them, rolls them out and backpropagates the result. It stops after a fixed number of iterations (`MonteCarloTreeSearch.java`, `mctsNode.java`).
- **Legal moves.** The agent generates the legal moves for each action type the game allows.
- **Rollout scoring.** Each simulated result is scored against the starting board, rewarding diseases cured and research stations built.
- **Cooperation.** Agents send each other move suggestions and take them into account when choosing their own move (`suggestionsWithSignalling` in `Client.java`).

## Project layout

```
client/   agent: Client.java, MonteCarloTreeSearch.java, mctsNode.java
server/   game server and board model
all_v4.4.jar   compiled build used by the scripts
```

## Run

You need Java. Start the server, then four clients, each in its own terminal:

```bash
./makeServer.sh
./makeClient.sh   # run this four times
```

The server waits until four players have connected, then starts the game.
