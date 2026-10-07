# World Cup 2026 Simulator

A Java simulator for the 48-team 2026 World Cup (USA · Canada · Mexico). It plays the full tournament — 12 groups, the new Round of 32, and the knockouts through to the final — using a Poisson expected-goals model driven by team strength, form, squad availability, head-to-head history and a North America venue factor.

It runs two ways:

- **GUI** — a Swing desktop app that plays one tournament live, with animated group tables, a knockout bracket and a match-by-match analysis log.
- **Monte Carlo** — a headless mode that simulates thousands of tournaments and reports each team's probability of reaching every stage.

No dependencies beyond the JDK.

## Quick start

Requires **Java 17+**.

```bash
javac -d out src/*.java

# GUI
java -cp out Main

# Monte Carlo: 10,000 tournaments, seed 42 (both optional)
java -cp out Main --montecarlo 10000 42
```

## Sample output

10,000 simulated tournaments (seed 42, data as of October 2026):

| # | Team | Win group | Reach QF | Reach SF | Reach final | Champion |
|---|---|---|---|---|---|---|
| 1 | Argentina | 76.3% | 57.2% | 39.7% | 26.7% | **17.3%** |
| 2 | France | 64.9% | 53.7% | 36.5% | 24.4% | **14.8%** |
| 3 | Brazil | 57.9% | 49.1% | 31.5% | 19.0% | **10.7%** |
| 4 | England | 69.3% | 46.3% | 28.9% | 17.4% | **9.4%** |
| 5 | Spain | 65.6% | 45.0% | 27.4% | 15.5% | **8.0%** |
| 6 | Portugal | 58.9% | 42.9% | 25.7% | 14.5% | **7.9%** |
| 7 | Germany | 67.0% | 41.3% | 23.8% | 12.7% | **6.8%** |
| 8 | Netherlands | 43.8% | 31.8% | 16.9% | 8.0% | **3.5%** |

Spain sit fifth largely because the squad data has Rodri and Carvajal out and Pedri doubtful.

## How a match is simulated

Each side's goals are drawn from a Poisson distribution with expected goals λ:

```
λ(A vs B) = 1.25                                   base rate
          + (strength_A − strength_B) / 100 × 2.5   quality gap — the dominant term
          + form_A × 0.20 − form_B × 0.10           recent team form
          + H2H(A, B)                               historical edge, ±0.20
          + NA_factor_A                             familiarity with North American conditions
          + squad_A                                 star power, injuries, squad form (±0.25)
λ = max(λ, 0.3)
```

The **squad modifier** adds up to +0.04 per fit, in-form star player (capped at +0.16), subtracts 0.09 per injured star and 0.04 per doubtful one (capped at −0.22), and adds average squad form × 0.08.

**Knockout ties** go to extra time (λ × 0.33), then penalties: each kick converts with probability `0.72 + (strength − 65) × 0.002`, minus the opposing goalkeeper's save bonus, with sudden death after five.

**Group standings** follow FIFA's 2026 order: points, then head-to-head points / goal difference / goals among tied teams, then overall goal difference, goals scored, and FIFA ranking (fair play is not modelled). The top two in each group plus the eight best third-placed teams reach the Round of 32, where the draw avoids rematches of group-stage fixtures.

## Data sources

| Data | Source | File |
|---|---|---|
| Group draw | football-data.org API if `FD_API_KEY` is set, otherwise the official 5 Dec 2025 draw | `FootballApiProvider`, `StaticGroupStageProvider` |
| Team strength, FIFA ranking, form, venue factor | Hand-curated | `Tournament.buildTeams()` |
| Squads (club, age, fitness, market value, star flag) | Transfermarkt / FotMob, May 2026 | `SquadData` |
| Live player form | FotMob, overlaid at startup when available | `FotMobSquadProvider` |
| Head-to-head edges | WC and major-tournament history | `HeadToHead` |

**Live form.** FotMob player ratings are mapped onto the static form scale with `form = (rating − 5.75) / 3.5`. That formula was fitted by matching the mean and spread of all 706 rated players against the hand-curated form values, so live and static data can be mixed fairly. Coverage is partial: FotMob currently has ratings for 22 of 48 squads, and teams without them keep their static form. If FotMob can't be reached, everything falls back to static data.

**Live group draw (optional).** Get a free key at [football-data.org](https://www.football-data.org/client/register), then either `export FD_API_KEY=your_key` or put `FD_API_KEY=your_key` in a `config.properties` file in the project root (it's git-ignored).

## Project structure

```
src/
  Main.java                 entry point: GUI, or --montecarlo
  Tournament.java           team ratings, group → R32 → final flow
  Group.java                round robin and FIFA tiebreakers
  Match.java                Poisson λ model, extra time, penalties, outcome probabilities
  Team.java, Squad.java, Player.java
  HeadToHead.java           historical matchup edges
  MonteCarlo.java           batch simulation and stage-reach report
  SimulatorWindow.java      Swing GUI
  *Provider.java, SquadData.java   data sources and fallbacks
test/
  InvariantCheck.java       2,000-tournament structural checks
```

Run the checks:

```bash
javac -d out src/*.java test/*.java && java -cp out InvariantCheck
```

## Limitations

- Model weights are hand-tuned, not fitted to historical results. Backtesting against the real 2026 tournament is the obvious next step.
- The Round of 32 bracket is a constrained random draw rather than FIFA's fixed pairing table.
- FotMob has no official API; its endpoints change without notice and may break the live overlay (the simulator then falls back to static data).

## Disclaimer

This is an independent fan project. It is not affiliated with or endorsed by FIFA, FotMob or football-data.org.

## License

[MIT](LICENSE)
