# Collection Route Optimization: Nearest Neighbor Heuristic & CVRP Guide

This document provides a comprehensive mathematical, architectural, and algorithmic explanation of the **Nearest Neighbor Route Optimization Engine** implemented in `com.waste.management.service.RouteOptimizationService`.

---

## 1. Problem Formulation: Capacitated Vehicle Routing Problem (CVRP)

The municipal waste collection problem can be modeled as a **Capacitated Vehicle Routing Problem (CVRP)**, an extension of the classical Traveling Salesperson Problem (TSP):

Let $G = (V, E)$ be a complete undirected graph where:
- Vertex $v_0 \in V$ represents the municipal **Base Depot** (start and end point of the collection trip).
- Vertices $V' = \{v_1, v_2, \dots, v_n\} \subset V$ represent active **Waste Bins** requiring collection.
- Each bin $v_i$ has a measured or estimated waste demand $q_i \ge 0$ (kg), derived from:
  $$q_i = \left( \frac{\text{fillPercentage}_i}{100} \right) \times \text{capacity}_i$$
- A collection vehicle has a maximum payload capacity $Q_{\max}$ (kg).
- Each edge $(u, v) \in E$ has a known road distance $d(u, v) \ge 0$ (km), populated from the municipal distance matrix table `location_distances`.

### Objective Function
Minimize total collection tour distance:
$$\min \sum_{k=0}^{m} d(s_k, s_{k+1})$$
subject to:
1. **Depot Cycle**: Tour starts at depot $s_0 = v_0$ and ends at depot $s_{m+1} = v_0$.
2. **Visit-Once Condition**: Each scheduled bin $v_i$ is visited exactly once: $s_k \in V'$.
3. **Vehicle Capacity Limit**: Total load accumulated does not exceed truck capacity:
   $$\sum_{k=1}^{m} q(s_k) \le Q_{\max}$$

---

## 2. The Nearest Neighbor Heuristic

Because CVRP is an **NP-hard** combinatorial optimization problem, finding the exact globally optimal route via Integer Linear Programming (ILP) requires exponential time $O(2^n \cdot n^2)$ — impractical for real-time dispatch systems.

We implement the **Nearest Neighbor Heuristic**, which runs in polynomial time $O(n^2)$:

### Algorithmic Workflow:
1. **Initialize**: Set current location $s_{\text{curr}} = v_0$ (Depot). Set cumulative distance $D = 0$, cumulative load $L = 0$, unvisited set $U = \{v_1, \dots, v_n\}$.
2. **Candidate Search**: At each step, evaluate all remaining unvisited bins $v \in U$:
   - Check capacity feasibility: $L + q(v) \le Q_{\max}$.
   - If no remaining candidate satisfies capacity, terminate the bin search.
3. **Selection Rule (Priority Biased)**: Select candidate $v^*$ that minimizes:
   $$\text{Cost}(s_{\text{curr}}, v) = d(s_{\text{curr}}, v) - \left( w_{\text{priority}}(v) \times \beta \right)$$
   where:
   - $w_{\text{priority}}$ is the priority weight (Critical = 4, High = 3, Medium = 2, Low = 1).
   - $\beta = 0.5\text{ km}$ is the priority discount factor, prioritizing high-overflow bins when distances are comparable.
4. **Tour Advancement**:
   - Add $v^*$ as the next stop in sequence.
   - Update $D \leftarrow D + d(s_{\text{curr}}, v^*)$.
   - Update $L \leftarrow L + q(v^*)$.
   - Update $s_{\text{curr}} \leftarrow v^*$.
   - Remove $v^*$ from $U$.
5. **Round Trip Return**: From the last visited bin $s_m$, travel back to $v_0$ (Depot):
   $$D \leftarrow D + d(s_m, v_0)$$
6. **Travel Time Estimation**: Total operational duration $T$ (minutes):
   $$T = \left( \frac{D}{v_{\text{truck}}} \times 60 \right) + \left( m \times t_{\text{service}} \right)$$
   where average truck speed $v_{\text{truck}} = 25\text{ km/h}$ and loading time $t_{\text{service}} = 5\text{ min/bin}$.

---

## 3. Step-by-Step Worked Example (Matching Unit Test)

Consider a Base Depot $D_0$ and three candidate bins $B_1, B_2, B_3$ with vehicle capacity $Q_{\max} = 2000\text{ kg}$:

### Distance Matrix:
- $d(D_0, B_1) = 1.0\text{ km}$
- $d(D_0, B_2) = 3.0\text{ km}$
- $d(D_0, B_3) = 5.0\text{ km}$
- $d(B_1, B_2) = 1.5\text{ km}$
- $d(B_1, B_3) = 4.0\text{ km}$
- $d(B_2, B_3) = 1.2\text{ km}$
- Each bin demand: $q_1 = 80\text{ kg}, q_2 = 80\text{ kg}, q_3 = 80\text{ kg}$ (all High priority, weight = 3).

### Step-by-Step Execution:
- **Stop 0**: Start at Depot $D_0$. Load = $0\text{ kg}$, Dist = $0.0\text{ km}$.
- **Stop 1**: From $D_0$, distances are $B_1: 1.0$, $B_2: 3.0$, $B_3: 5.0$.
  - Minimum is $B_1$ ($1.0\text{ km}$).
  - Next stop: **$B_1$**. Cumulative Dist = $1.0\text{ km}$, Load = $80\text{ kg}$.
- **Stop 2**: From $B_1$, unvisited are $B_2: 1.5\text{ km}$, $B_3: 4.0\text{ km}$.
  - Minimum is $B_2$ ($1.5\text{ km}$).
  - Next stop: **$B_2$**. Cumulative Dist = $1.0 + 1.5 = 2.5\text{ km}$, Load = $160\text{ kg}$.
- **Stop 3**: From $B_2$, only $B_3$ remains ($1.2\text{ km}$).
  - Next stop: **$B_3$**. Cumulative Dist = $2.5 + 1.2 = 3.7\text{ km}$, Load = $240\text{ kg}$.
- **Stop 4 (Return Leg)**: From $B_3$ back to Depot $D_0$ ($5.0\text{ km}$).
  - Total tour distance = $3.7 + 5.0 = \mathbf{8.70\text{ km}}$.
  - Total load collected = $\mathbf{240.0\text{ kg}}$.

---

## 4. Complexity Analysis

| Metric | Nearest Neighbor Heuristic | Exact Branch-and-Bound / ILP |
|---|---|---|
| **Time Complexity** | $\mathcal{O}(n^2)$ | $\mathcal{O}(2^n \cdot n^2)$ (Exponential) |
| **Space Complexity** | $\mathcal{O}(n)$ | $\mathcal{O}(2^n)$ |
| **Scalability** | Instantaneous for municipal zones ($n \le 100$) | Unusable beyond $n \approx 20$ |
| **Optimality** | Greedy heuristic (near-optimal) | Globally optimal |

---

## 5. Viva Defense Talking Points

1. **Why Nearest Neighbor instead of Dijkstra or A\*?**
   - Dijkstra and A\* find the **shortest path between two points** (Point A to Point B).
   - Our challenge is a **Tour Problem** (visit $N$ distinct points and return to start), which is CVRP/TSP. Nearest Neighbor constructs a closed tour visiting all candidate bins.
2. **Is Nearest Neighbor globally optimal?**
   - No. It is a greedy heuristic that makes locally optimal choices at each step. In certain edge cases ("sub-tour traps"), the final return leg to the depot can be long.
   - However, for municipal waste collection with capacity limits, its $O(n^2)$ execution speed, deterministic predictability, and priority tie-breaking make it standard in municipal dispatch tools.
3. **How does the system prevent missing distance bugs?**
   - The engine validates distance availability via [`lookupDistance()`](file:///d:/Hackathons/Java%208%20Capstone%20Project/src/main/java/com/waste/management/service/RouteOptimizationService.java). If an unmapped road segment is encountered, it raises a explicit `ValidationException` naming the exact unmapped locations rather than hallucinating arbitrary numbers.
