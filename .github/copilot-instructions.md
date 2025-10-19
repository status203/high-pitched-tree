<!-- .github/copilot-instructions.md - guidance for AI coding agents -->
# Project notes for AI coding assistants

This repo generates and analyses simple 2D "trees" represented as nested maps. The guidance below highlights the key architecture, conventions, and typical developer workflows so an AI agent can be productive quickly.

## Big picture
- Core domain: a tree is a nested map structure where each branch is a map with keys :start, :end, :rel-angle, :abs-angle, :length, and :children. The trunk is depth 1.
- Growth algorithm: `trees.tree/grow` constructs a zipper-based tree using pluggable algorithm functions (angle, length, and child policies). See `test/trees/tree_test.clj` for common usage.
- Zipper usage: The code uses `clojure.zip` and a custom `trees.util/tree-zipper` for efficient traversal and mutation.

## Key files and what to look for
- `src/trees/tree.clj` — grow algorithm, branch insertion, finalisation, and angle/length conversion helpers.
- `src/trees/util.clj` — zipper helpers, depth and bounds calculations, deterministic utilities (e.g. `stochastic-round`).
- `src/trees/algo/` — small, composable algorithm modules. Each module implements one algorithmic role (for example: produce a branch-angle, produce a branch-length, or decide whether to add a child). Keep algorithms focused, pure, and compatible with `trees.algo.combine/with` and `by-depth` for composition.
- `test/` — contains concise examples of how algorithm pieces are composed and how `grow` is invoked; use tests as executable documentation.

## Data shapes / contracts

Branch map example:

```clojure
{:start    [x y]
 :end      [x y]
 :rel-angle number ; relative clockwise degrees
 :abs-angle number ; absolute clockwise degrees-from-West
 :length   number
 :children list}
```

Algorithm functions (expected shapes):

```clojure
; branch-angle  : [zipper-loc] -> degrees (clockwise)
; branch-length : [zipper-loc] -> length (number)
; add-child?    : [zipper-loc] -> boolean
```

## Project-specific conventions
- Zipper locs: many helper fns expect a zipper loc (not raw nodes). Use `trees.util/tree-zipper` to create one.
- Children lists are stored such that the most recently inserted child is first (created via z/insert-child). Tests and helpers account for this ordering.
- Angles are stored as clockwise degrees-from-West in :abs-angle. `trees.tree/convert-angle` converts to radians for trig.
- Depth semantics: trunk depth = 1. The parent of the trunk is nil and considered depth 0. See `trees.util/depth` and usages in `combine/by-depth`.

## Common workflows (build, test, debug)
- Tests are standard clojure.test under `test/`. Run with your usual deps tool (this repo uses deps.edn):
  - To run all tests locally (local deps tool):
    clojure -M:test
  - To run a specific test file, e.g. `tree_test.clj`:
    clojure -M:test:test/trees/tree_test.clj

  Note: If your environment uses `lein` or a different alias, prefer the repo's `deps.edn` conventions.

## Patterns to follow when editing or adding code
- Prefer small, composable pure functions under `src/trees/algo/*`. Tests often compose these with `trees.algo.combine/with` or `by-depth`.
- Use zippers for tree manipulation; helpers in `trees.util` should be reused (e.g. `depth`, `has-children?`, `tree-zipper`).
- When adding functions that operate on a branch, accept a zipper loc and return plain values (angles, lengths, booleans). This keeps them composable with `combine/with` and `by-depth`.

## Examples (copyable snippets from repo)

Grow a basic tree (from tests):

```clojure
(def base-opts
  {:branch-angle  (trees.tree/with-vertical-trunk
                    ;; algorithms live in `src/trees/algo/` and should return
                    ;; values of the expected contract for the role
                    (trees.algo.angle/regularly-spaced 90 2))
   :branch-length (trees.algo.length/scale 100 0.7)
   :add-child?    (trees.algo.combine/with :and
                   (trees.algo.children/count<= 2)
                   (trees.algo.children/depth<= 3))})

(trees.tree/grow base-opts)
```

Test commands (repo uses `deps.edn`):

```bash
clojure -M:test
# run a single test file:
clojure -M:test:test/trees/tree_test.clj
```

## Tests & examples as doc-source
- Tests are small and authoritative. If unsure how an API behaves, open `test/trees/*_test.clj` for concrete examples of expected inputs/outputs.

## Integration & external dependencies
- This is a single-module Clojure library (no external HTTP services). It uses Clojure stdlib (`clojure.zip`) and is driven by `deps.edn`.

## When making PRs
- Keep changes small and well-tested. Add unit tests in `test/` mirroring existing test styles.
- Preserve the public function shapes (branch-angle, branch-length, add-child?) to maintain composability.

---
## Dev examples & Quil rendering

The `dev/` folder contains utilities and presets for exploring trees and rendering them with Quil.

- `dev/examples.clj` — behavior-named presets for `trees.tree/grow` (e.g. `binary-symmetric`, `radial-fan`, `lopsided-spiral`). These return option maps suitable to pass directly to `trees.tree/grow`.
- `dev/draw.clj` — a small Quil-aware renderer. The central helper is `draw-tree` which:
  - computes model bounds via `trees.util/bounds`,
  - chooses scaling and placement (options: `:scale` -> `:none|:to-fit|:to-view|:contain|:cover`),
  - draws lines for each branch end/start pair.

Quick REPL preview (example): start a REPL in the project root and evaluate the following to open a Quil window:

```clojure
(require '[quil.core :as q]
         '[dev.draw :as draw]
         '[dev.examples :as examples]
         '[trees.tree :as tree])

(def t (atom (tree/grow (examples/binary-symmetric))))

(q/defsketch tree-demo
  :title "tree-demo"
  :size [800 600]
  :setup (fn [] (q/frame-rate 30))
  :draw (fn []
          (draw/draw-tree @t {:width 800 :height 600
                              :scale :contain
                              :padding 20
                              :bg 0xFF})))
```

Notes:
- The sketch above uses `draw/draw-tree` to render a static tree stored in `t`. Swap the preset (for example, `(examples/radial-fan)`) or regenerate `t` to see different behaviours.
- The repo already requires Quil in `dev/draw.clj`, so start a plain REPL with `clojure` (or your preferred tool). If Quil isn't available in your environment, ensure the `deps.edn` includes Quil under the aliases you use to start the REPL.

If anything here is unclear or you'd like additional examples (rendering, CLI usage, or visual output under `dev/`), tell me which area to expand and I'll iterate.
