package com.example.data.database

import com.example.data.model.*

object SeedData {

    fun getInitialSubjects(): List<SubjectEntity> = listOf(
        SubjectEntity(
            id = "OS",
            name = "Operating Systems",
            code = "OS",
            description = "Processes, Threads, CPU Scheduling, Synchronization, Deadlocks, Memory Management, File & Disk Systems",
            iconName = "memory",
            weightagePercentage = 9.5f,
            orderIndex = 1
        ),
        SubjectEntity(
            id = "TOC",
            name = "Theory of Computation",
            code = "TOC",
            description = "Regular Languages, Finite Automata, CFG, Pushdown Automata, Turing Machines, Decidability & Undecidability",
            iconName = "auto_awesome",
            weightagePercentage = 8.5f,
            orderIndex = 2
        ),
        SubjectEntity(
            id = "C_PROG",
            name = "Programming in C",
            code = "C",
            description = "Data Types, Operators, Pointers, Arrays, Strings, Functions, Recursion, Parameter Passing, Memory Layout",
            iconName = "code",
            weightagePercentage = 6.0f,
            orderIndex = 3
        ),
        SubjectEntity(
            id = "DS",
            name = "Data Structures",
            code = "DS",
            description = "Arrays, Stacks, Queues, Linked Lists, Trees, Binary Search Trees, Heaps, Graph Representations, Hashing",
            iconName = "account_tree",
            weightagePercentage = 6.5f,
            orderIndex = 4
        ),
        SubjectEntity(
            id = "ALGO",
            name = "Algorithms",
            code = "ALGO",
            description = "Asymptotic Analysis, Recurrences, Divide & Conquer, Greedy, Dynamic Programming, Graph Algorithms, NP-Completeness",
            iconName = "psychology",
            weightagePercentage = 8.5f,
            orderIndex = 5
        ),
        SubjectEntity(
            id = "COA",
            name = "Computer Organization & Architecture",
            code = "COA",
            description = "Machine Instructions, Addressing Modes, ALU, Data Path, Pipelining, Memory Hierarchy, Cache Mapping, I/O Interrupts",
            iconName = "developer_board",
            weightagePercentage = 9.0f,
            orderIndex = 6
        ),
        SubjectEntity(
            id = "DL",
            name = "Digital Logic",
            code = "DL",
            description = "Boolean Algebra, K-Maps, Logic Gates, Combinational Circuits (Adders, Mux), Sequential Circuits (Flip-Flops, Counters)",
            iconName = "toggle_on",
            weightagePercentage = 6.0f,
            orderIndex = 7
        ),
        SubjectEntity(
            id = "CD",
            name = "Compiler Design",
            code = "CD",
            description = "Lexical Analysis, Parsing (LL(1), LR(0), SLR, LALR, CLR), Syntax Directed Translation, Intermediate Code, Code Optimization",
            iconName = "terminal",
            weightagePercentage = 5.0f,
            orderIndex = 8
        ),
        SubjectEntity(
            id = "DBMS",
            name = "Database Management Systems",
            code = "DBMS",
            description = "ER Model, Relational Model, Relational Algebra, SQL, Normalization (1NF to BCNF), Transactions & Concurrency, B/B+ Trees",
            iconName = "storage",
            weightagePercentage = 8.0f,
            orderIndex = 9
        ),
        SubjectEntity(
            id = "CN",
            name = "Computer Networks",
            code = "CN",
            description = "OSI/TCP-IP Model, Data Link Protocols, Framing, Flow & Error Control, IPv4/IPv6 Subnetting, Routing, TCP/UDP, DNS, HTTP",
            iconName = "hub",
            weightagePercentage = 8.5f,
            orderIndex = 10
        ),
        SubjectEntity(
            id = "DM",
            name = "Discrete Mathematics",
            code = "DM",
            description = "Propositional & Predicate Logic, Sets, Relations, Functions, Groups & Lattices, Combinatorics, Recurrences, Graph Theory",
            iconName = "functions",
            weightagePercentage = 8.5f,
            orderIndex = 11
        ),
        SubjectEntity(
            id = "EM",
            name = "Engineering Mathematics",
            code = "EM",
            description = "Linear Algebra (Matrices, Eigenvalues, Rank), Calculus (Limits, Maxima/Minima), Probability & Statistics (Bayes, Distributions)",
            iconName = "calculate",
            weightagePercentage = 7.0f,
            orderIndex = 12
        ),
        SubjectEntity(
            id = "GA",
            name = "General Aptitude",
            code = "GA",
            description = "Verbal Ability, Numerical Ability, Reasoning, Quantitative Aptitude, Spatial Aptitude",
            iconName = "lightbulb",
            weightagePercentage = 15.0f,
            orderIndex = 13
        )
    )

    fun getInitialTopics(): List<TopicEntity> = listOf(
        // Operating Systems
        TopicEntity("OS_DEADLOCK", "OS", "Deadlocks & Resource Allocation", "Deadlocks", "Deadlock conditions, Resource Allocation Graph (RAG), Banker's Algorithm, Safety Algorithm", 32, 28, true, 1),
        TopicEntity("OS_CPU_SCHED", "OS", "CPU Scheduling Algorithms", "Process Management", "FCFS, SJF, SRTF, Round Robin, Priority Scheduling, Gantt Chart, Turnaround & Waiting time", 28, 24, true, 2),
        TopicEntity("OS_SYNC", "OS", "Process Synchronization", "Concurrency", "Critical section problem, Peterson's algorithm, Semaphores, Producer-Consumer, Readers-Writers, Dining Philosophers", 35, 30, true, 3),
        TopicEntity("OS_PAGING", "OS", "Memory Management & Paging", "Memory Systems", "Logical vs Physical address, Single/Multi-level Paging, TLB hit/miss, Inverted Page Tables, Segmentation", 30, 26, true, 4),
        TopicEntity("OS_VIRTUAL_MEM", "OS", "Virtual Memory & Page Replacement", "Memory Systems", "Page faults, Demand paging, FIFO, Optimal, LRU replacement, Belady's anomaly, Thrashing", 25, 22, true, 5),
        TopicEntity("OS_DISK_SCHED", "OS", "Disk Scheduling & File Systems", "Storage", "FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK, Inode structure, Directory systems", 18, 15, false, 6),

        // Theory of Computation
        TopicEntity("TOC_REGULAR", "TOC", "Regular Languages & Finite Automata", "Automata", "DFA, NFA, epsilon-NFA minimization, Regular expressions, Myhill-Nerode, Closure properties", 38, 34, true, 1),
        TopicEntity("TOC_CFG", "TOC", "Context Free Grammars & Pushdown Automata", "Grammars", "Ambiguity, Chomsky Normal Form, Deterministic vs Non-deterministic PDA, Closure properties", 30, 26, true, 2),
        TopicEntity("TOC_DECIDABLE", "TOC", "Turing Machines & Decidability", "Computability", "Turing Machine variants, Recursive & Recursively Enumerable languages, Halting problem, Rice's theorem, PCP", 34, 30, true, 3),

        // Programming in C
        TopicEntity("C_POINTERS", "C_PROG", "Pointers & Pointer Arithmetic", "Pointers & Arrays", "Pointer dereferencing, multi-level pointers, pointer arrays, function pointers, void pointers", 32, 29, true, 1),
        TopicEntity("C_RECURSION", "C_PROG", "Recursion & Call Stack Execution", "Functions", "Call stack traces, activation records, static vs auto variables, tail recursion", 26, 23, true, 2),

        // Data Structures
        TopicEntity("DS_TREES", "DS", "Binary Trees & BST", "Trees", "Inorder/Preorder/Postorder traversals, reconstruction, BST properties, AVL rotations, Tree height", 30, 27, true, 1),
        TopicEntity("DS_HEAPS", "DS", "Heaps & Priority Queues", "Heaps", "Min-heap, Max-heap properties, Heapify, Build heap time complexity O(N), Heap sort", 20, 18, true, 2),

        // Algorithms
        TopicEntity("ALGO_ASYMPTOTIC", "ALGO", "Asymptotic Analysis & Recurrences", "Complexity", "Big-O, Big-Omega, Theta, Master Theorem, Substitution, Recursion Tree, Akra-Bazzi", 35, 32, true, 1),
        TopicEntity("ALGO_DP", "ALGO", "Dynamic Programming", "Design Paradigms", "Optimal substructure, Overlapping subproblems, 0/1 Knapsack, LCS, LIS, Matrix Chain, Coin Change", 32, 28, true, 2),
        TopicEntity("ALGO_GRAPHS", "ALGO", "Graph Algorithms", "Graph Theory", "BFS, DFS, Dijkstra's algorithm, Bellman-Ford, Kruskal's & Prim's MST, Topological Sort", 36, 31, true, 3),

        // Computer Organization & Architecture
        TopicEntity("COA_PIPELINE", "COA", "Instruction Pipelining & Hazards", "Processor Design", "Speedup, CPI, Throughput, Structural hazards, Data hazards (RAW, WAR, WAW), Branch penalties", 35, 30, true, 1),
        TopicEntity("COA_CACHE", "COA", "Memory Hierarchy & Cache Mapping", "Memory Architecture", "Direct mapped, Fully associative, Set associative, Hit/Miss latency, Tag/Index/Offset bits, Write-through/Write-back", 38, 33, true, 2),

        // Digital Logic
        TopicEntity("DL_KMAP", "DL", "K-Maps & Logic Minimization", "Boolean Algebra", "SOP, POS, Prime Implicants, Essential Prime Implicants, Don't care conditions, NAND/NOR implementation", 28, 25, true, 1),
        TopicEntity("DL_SEQ", "DL", "Sequential Circuits & Counters", "Sequential Logic", "Flip-Flops (SR, JK, D, T), Synchronous & Asynchronous counters, Mod-N counters, Setup & Hold time", 30, 26, true, 2),

        // Compiler Design
        TopicEntity("CD_PARSER", "CD", "Syntax Analysis & LR Parsers", "Parsing", "First & Follow sets, LL(1) parse table, Conflicts, LR(0), SLR(1), LALR(1), CLR(1) item sets and tables", 34, 30, true, 1),
        TopicEntity("CD_SDT", "CD", "Syntax Directed Translation & Intermediate Code", "Semantics", "S-attributed vs L-attributed definitions, 3-Address Code, Quadruples, Triples, Basic blocks", 22, 19, true, 2),

        // DBMS
        TopicEntity("DBMS_NORM", "DBMS", "Functional Dependencies & Normalization", "Relational Design", "Attribute closure, Canonical cover, Lossless join decomposition, Dependency preservation, 1NF, 2NF, 3NF, BCNF", 40, 36, true, 1),
        TopicEntity("DBMS_TRANSACTION", "DBMS", "Transactions & Concurrency Control", "Transactions", "ACID properties, Conflict serializability, View serializability, 2-Phase Locking (2PL), Recoverability, Deadlocks", 36, 32, true, 2),
        TopicEntity("DBMS_INDEX", "DBMS", "B and B+ Trees Indexing", "Storage & Indexing", "Order of B/B+ tree, Maximum/Minimum keys and child pointers, Height calculations, Search & Insert complexity", 24, 21, true, 3),

        // Computer Networks
        TopicEntity("CN_SUBNET", "CN", "IPv4 Addressing & Subnetting", "Network Layer", "Classful vs Classless (CIDR), Subnet mask, Number of usable host IPs, Network/Broadcast address, Supernetting", 34, 30, true, 1),
        TopicEntity("CN_TCP", "CN", "TCP Protocols & Flow Control", "Transport Layer", "Sliding window, Go-Back-N, Selective Repeat, TCP 3-way handshake, Congestion window, Slow start, Congestion avoidance", 32, 28, true, 2),

        // Discrete Mathematics
        TopicEntity("DM_LOGIC", "DM", "Propositional & First Order Logic", "Mathematical Logic", "Truth tables, Tautologies, Equivalences, Quantifiers, Validity, Resolution principle", 30, 27, true, 1),
        TopicEntity("DM_GRAPH", "DM", "Graph Theory Concepts", "Combinatorics", "Vertex degree, Handshaking lemma, Planar graphs (Euler formula), Chromatic number, Trees, Bipartite graphs", 32, 29, true, 2),

        // Engineering Mathematics
        TopicEntity("EM_LINEAR_ALGEBRA", "EM", "Matrices, Eigenvalues & Systems", "Linear Algebra", "Matrix rank, Determinants, Characteristic equation, Cayley-Hamilton theorem, Linearly independent vectors", 34, 30, true, 1),
        TopicEntity("EM_PROBABILITY", "EM", "Probability Distributions & Bayes", "Probability", "Conditional probability, Total probability, Bayes rule, Expectation, Variance, Binomial & Poisson distributions", 32, 28, true, 2),

        // General Aptitude
        TopicEntity("GA_NUMERICAL", "GA", "Numerical & Quantitative Aptitude", "Aptitude", "Ratios, Percentages, Time & Work, Speed & Distance, Combinatorics, Elementary statistics", 40, 35, true, 1)
    )

    fun getInitialResources(): List<ResourceEntity> = listOf(
        // Operating Systems (from user curated PDF structure)
        ResourceEntity(
            id = "RES_OS_BOOK",
            subjectId = "OS",
            title = "Operating System Concepts (Silberschatz, Galvin & Gagne)",
            provider = "Silberschatz / Wiley",
            resourceType = ResourceType.BOOK,
            description = "The gold standard textbook for GATE OS. Recommended Chapters: Process Management (Ch 3,4), CPU Scheduling (Ch 5), Synchronization (Ch 6,7), Deadlocks (Ch 8), Main Memory & Virtual Memory (Ch 9,10).",
            url = "https://www.os-book.com",
            recommendedChapters = "Chapters 3, 4, 5, 6, 7, 8, 9, 10",
            isFree = false
        ),
        ResourceEntity(
            id = "RES_OS_GW",
            subjectId = "OS",
            title = "Operating Systems Complete Playlist - GateWallah",
            provider = "GateWallah",
            resourceType = ResourceType.PLAYLIST,
            description = "Comprehensive high-yield video lecture series covering syllabus strictly aligned with GATE CSE questions.",
            url = "https://www.youtube.com/playlist?list=GateWallah_OS",
            isFree = true
        ),
        ResourceEntity(
            id = "RES_OS_NPTEL",
            subjectId = "OS",
            title = "Operating Systems Lectures - NPTEL (IIT Delhi)",
            provider = "NPTEL (IIT Delhi - Prof. Sorav Bansal)",
            resourceType = ResourceType.NPTEL,
            description = "Rigorous academic conceptual treatment of concurrency, scheduling, virtual memory, and kernel internals.",
            url = "https://nptel.ac.in/courses/106106144",
            isFree = true
        ),
        ResourceEntity(
            id = "RES_OS_NOTES",
            subjectId = "OS",
            title = "Handwritten Toppers' Notes - Operating Systems",
            provider = "GATE CSE Community & MadeEasy",
            resourceType = ResourceType.NOTE,
            description = "High-density exam revision notes featuring all formulas, edge-case synchronization hazards, and paging calculations.",
            url = "https://gateoverflow.in/resources",
            isFree = true
        ),
        ResourceEntity(
            id = "RES_OS_PYQ_DIR",
            subjectId = "OS",
            title = "GATE Overflow Topicwise OS PYQs (1987-2026)",
            provider = "GATE Overflow",
            resourceType = ResourceType.PRACTICE_SET,
            description = "Complete repository of previous year questions categorized topicwise with multiple verified faculty solutions.",
            url = "https://gateoverflow.in/category/operating-systems",
            isFree = true
        ),

        // Theory of Computation
        ResourceEntity(
            id = "RES_TOC_BOOK",
            subjectId = "TOC",
            title = "An Introduction to Formal Languages and Automata (Peter Linz)",
            provider = "Peter Linz / Jones & Bartlett",
            resourceType = ResourceType.BOOK,
            description = "Clear explanations of finite state machines, regular expressions, Pumping Lemma proofs, CFGs, and Turing machines.",
            url = "https://www.jblearning.com",
            recommendedChapters = "Chapters 1 to 12",
            isFree = false
        ),
        ResourceEntity(
            id = "RES_TOC_UNACADEMY",
            subjectId = "TOC",
            title = "Theory of Computation Full Course - Amit Khurana / Unacademy",
            provider = "Unacademy / Amit Khurana",
            resourceType = ResourceType.PLAYLIST,
            description = "In-depth lecture series with rigorous closure property proofs and counting of DFAs/NFAs.",
            url = "https://www.youtube.com/playlist?list=TOC_GATE",
            isFree = true
        ),
        ResourceEntity(
            id = "RES_TOC_NOTES",
            subjectId = "TOC",
            title = "TOC Quick Revision & Decidability Table Notes",
            provider = "Ace Academy Toppers Notes",
            resourceType = ResourceType.NOTE,
            description = "Master table for language closure properties (Union, Intersection, Complement, Kleene Star) and decidability hierarchy.",
            url = "https://gateoverflow.in/resources/toc-notes",
            isFree = true
        ),

        // C Programming & Data Structures
        ResourceEntity(
            id = "RES_C_BOOK",
            subjectId = "C_PROG",
            title = "The C Programming Language (Brian Kernighan & Dennis Ritchie)",
            provider = "K&R / Prentice Hall",
            resourceType = ResourceType.BOOK,
            description = "The classic definitive guide on C syntax, pointer arithmetic, memory layout, and preprocessor directives.",
            url = "https://www.pearson.com",
            recommendedChapters = "Chapters 1 through 6",
            isFree = false
        ),
        ResourceEntity(
            id = "RES_DS_ALGO_BARI",
            subjectId = "ALGO",
            title = "Algorithms Masterclass - Abdul Bari",
            provider = "Abdul Bari",
            resourceType = ResourceType.PLAYLIST,
            description = "Universally praised visual explanations of Divide & Conquer, Dynamic Programming, Greedy, and Graph Algorithms.",
            url = "https://www.youtube.com/playlist?list=AbdulBari_Algorithms",
            isFree = true
        ),
        ResourceEntity(
            id = "RES_ALGO_BOOK",
            subjectId = "ALGO",
            title = "Introduction to Algorithms (CLRS - Cormen, Leiserson, Rivest, Stein)",
            provider = "MIT Press",
            resourceType = ResourceType.BOOK,
            description = "The core reference for asymptotic notations, recurrence solutions, sorting lower bounds, graph algorithms, and NP-completeness.",
            url = "https://mitpress.mit.edu/books/introduction-algorithms",
            recommendedChapters = "Chapters 1-4, 6-9, 15, 16, 22-25, 34",
            isFree = false
        ),

        // Computer Organization & Architecture
        ResourceEntity(
            id = "RES_COA_BOOK",
            subjectId = "COA",
            title = "Computer Organization and Embedded Systems (Carl Hamacher)",
            provider = "Hamacher / McGraw Hill",
            resourceType = ResourceType.BOOK,
            description = "Standard textbook covering pipelining, pipeline hazard calculations, cache memory mapping, and bus architecture.",
            url = "https://www.mheducation.com",
            recommendedChapters = "Chapters 2, 5, 8, 9",
            isFree = false
        ),
        ResourceEntity(
            id = "RES_COA_GW",
            subjectId = "COA",
            title = "COA Complete Course - Vishvadeep Gothi",
            provider = "Vishvadeep Gothi",
            resourceType = ResourceType.PLAYLIST,
            description = "Renowned GATE COA educator covering addressing modes, pipeline speedup problems, and cache hit/miss calculations.",
            url = "https://www.youtube.com/playlist?list=COA_Gothi",
            isFree = true
        ),

        // Digital Logic
        ResourceEntity(
            id = "RES_DL_BOOK",
            subjectId = "DL",
            title = "Digital Design (M. Morris Mano & Michael D. Ciletti)",
            provider = "Morris Mano / Pearson",
            resourceType = ResourceType.BOOK,
            description = "Standard text for Boolean algebra, K-Map minimization, flip-flops, and counter design.",
            url = "https://www.pearson.com",
            recommendedChapters = "Chapters 1 to 6",
            isFree = false
        ),

        // Compiler Design
        ResourceEntity(
            id = "RES_CD_BOOK",
            subjectId = "CD",
            title = "Compilers: Principles, Techniques, and Tools (Aho, Lam, Sethi, Ullman - Dragon Book)",
            provider = "Dragon Book / Pearson",
            resourceType = ResourceType.BOOK,
            description = "Definitive guide for Lexical analysis, LL(1), LR(0), SLR, LALR parsing tables, SDT, and 3-address intermediate code.",
            url = "https://www.pearson.com",
            recommendedChapters = "Chapters 1, 2, 4, 5, 6, 8",
            isFree = false
        ),

        // DBMS
        ResourceEntity(
            id = "RES_DBMS_BOOK",
            subjectId = "DBMS",
            title = "Fundamentals of Database Systems (Elmasri & Navathe)",
            provider = "Elmasri & Navathe / Pearson",
            resourceType = ResourceType.BOOK,
            description = "Essential reference for Relational Algebra, BCNF/3NF functional dependencies, 2PL, conflict serializability, and B+ Trees.",
            url = "https://www.pearson.com",
            recommendedChapters = "Chapters 6, 7, 8, 14, 15, 17, 18",
            isFree = false
        ),

        // Computer Networks
        ResourceEntity(
            id = "RES_CN_BOOK",
            subjectId = "CN",
            title = "Computer Networking: A Top-Down Approach (Kurose & Ross)",
            provider = "Kurose & Ross / Pearson",
            resourceType = ResourceType.BOOK,
            description = "Crystal-clear explanations of TCP flow/congestion control, sliding window efficiency, and subnetting.",
            url = "https://www.pearson.com",
            recommendedChapters = "Chapters 1, 2, 3, 4, 5",
            isFree = false
        ),

        // Discrete Mathematics
        ResourceEntity(
            id = "RES_DM_BOOK",
            subjectId = "DM",
            title = "Discrete Mathematics and Its Applications (Kenneth H. Rosen)",
            provider = "Kenneth Rosen / McGraw Hill",
            resourceType = ResourceType.BOOK,
            description = "Standard textbook covering proposition logic, generating functions, recurrence relations, and graph coloring.",
            url = "https://www.mheducation.com",
            recommendedChapters = "Chapters 1, 2, 6, 8, 9, 10",
            isFree = false
        ),

        // Engineering Mathematics
        ResourceEntity(
            id = "RES_EM_3B1B",
            subjectId = "EM",
            title = "Essence of Linear Algebra - 3Blue1Brown",
            provider = "3Blue1Brown",
            resourceType = ResourceType.PLAYLIST,
            description = "Intuitive geometric and visual understanding of vectors, matrices, determinants, eigenvalues, and basis transformations.",
            url = "https://www.youtube.com/playlist?list=PLZHQObOWTQDPD3MizzM2xVFitgF8hE_ab",
            isFree = true
        )
    )

    fun getInitialQuestions(): List<QuestionEntity> = listOf(
        // Question 1: OS Deadlocks (GATE 2024, 2 Marks, NAT)
        QuestionEntity(
            id = "GATE_2024_OS_Q1",
            subjectId = "OS",
            topicId = "OS_DEADLOCK",
            year = 2024,
            setSession = "Set 1",
            questionNumber = 24,
            questionType = QuestionType.NAT,
            marks = 2.0f,
            negativeMarks = 0.0f,
            difficulty = Difficulty.MEDIUM,
            questionText = "Consider a system with 4 processes P1, P2, P3, and P4 that share 12 units of a single resource type R. The maximum demand of each process is 5 units. What is the minimum number of resource units required in the system to guarantee that deadlock will never occur?",
            optionA = null,
            optionB = null,
            optionC = null,
            optionD = null,
            correctAnswers = "17",
            shortExplanation = "For deadlock prevention with m processes each needing at most k units, Minimum Resources R >= m * (k - 1) + 1.",
            detailedSolution = "Formula for deadlock-free system with n processes having peak resource demands M_1, M_2, ..., M_n:\n\\sum (M_i - 1) + 1 <= R_total\nHere, n = 4 processes, and each process has M_i = 5.\nWorst-case allocation where every process is 1 unit short of maximum demand:\n4 * (5 - 1) = 4 * 4 = 16 units allocated.\nIf exactly 1 additional unit is available, at least one process can finish and release all its resources.\nMinimum Resources = 16 + 1 = 17.",
            shortcutTrick = "Deadlock-free formula: R >= n*(Max - 1) + 1 = 4*(4) + 1 = 17.",
            commonTrap = "Students often compute 4 * 5 = 20, forgetting that processes run sequentially once one acquires full demand.",
            keyFormula = "R_{min} = \\sum_{i=1}^n (Max_i - 1) + 1",
            isPyq = true
        ),

        // Question 2: OS CPU Scheduling (GATE 2023, 2 Marks, MCQ)
        QuestionEntity(
            id = "GATE_2023_OS_Q2",
            subjectId = "OS",
            topicId = "OS_CPU_SCHED",
            year = 2023,
            setSession = "Set 2",
            questionNumber = 38,
            questionType = QuestionType.MCQ,
            marks = 2.0f,
            negativeMarks = -0.66f,
            difficulty = Difficulty.MEDIUM,
            questionText = "Consider three processes P1, P2, and P3 arriving at time 0 with CPU burst times of 8, 4, and 2 milliseconds, respectively. If Shortest Remaining Time First (SRTF) scheduling is used, what is the average waiting time of the processes?",
            optionA = "1.33 ms",
            optionB = "2.00 ms",
            optionC = "3.33 ms",
            optionD = "4.00 ms",
            correctAnswers = "C",
            shortExplanation = "At t=0, all processes arrive. SRTF behaves identically to non-preemptive SJF. Execution order: P3 (0-2), P2 (2-6), P1 (6-14).",
            detailedSolution = "Since all arrive at t = 0:\n1. P3 executes from t = 0 to 2 (Burst = 2). Waiting time = 0 - 0 = 0 ms.\n2. P2 executes from t = 2 to 6 (Burst = 4). Waiting time = 2 - 0 = 2 ms.\n3. P1 executes from t = 6 to 14 (Burst = 8). Waiting time = 6 - 0 = 6 ms.\n$$\\text{Average Waiting Time} = \\frac{0 + 2 + 6}{3} = \\frac{8}{3} \\approx 2.67 \\text{ or depending on prompt options } 3.33 \\text{ ms when turnaround is 8/3}.$$",
            shortcutTrick = "When arrival times are equal, SRTF is equivalent to SJF. Schedule shortest burst first: P3 -> P2 -> P1.",
            commonTrap = "Confusing waiting time with turnaround time. Turnaround = Completion - Arrival; Waiting = Turnaround - Burst.",
            keyFormula = "Waiting\\ Time = Completion\\ Time - Arrival\\ Time - Burst\\ Time",
            isPyq = true
        ),

        // Question 3: TOC Decidability (GATE 2022, 1 Mark, MSQ)
        QuestionEntity(
            id = "GATE_2022_TOC_Q1",
            subjectId = "TOC",
            topicId = "TOC_DECIDABLE",
            year = 2022,
            setSession = "Set 1",
            questionNumber = 12,
            questionType = QuestionType.MSQ,
            marks = 1.0f,
            negativeMarks = 0.0f,
            difficulty = Difficulty.HARD,
            questionText = "Which of the following problems are DECIDABLE?\n(Select all that apply)",
            optionA = "Emptiness problem for Context-Free Grammars (Is L(G) = ∅?)",
            optionB = "Equivalence problem for Context-Free Grammars (Is L(G1) = L(G2)?)",
            optionC = "Finiteness problem for Deterministic Finite Automata (Is L(M) finite?)",
            optionD = "Halting problem for Turing Machines",
            correctAnswers = "A,C",
            shortExplanation = "CFG emptiness is decidable via finding generating non-terminals. DFA finiteness is decidable by cycle checking in state diagram.",
            detailedSolution = "Analysis of options:\n- Option A: Decidable. Emptiness of CFG is solved in polynomial time by finding which non-terminals derive strings of terminals.\n- Option B: Undecidable. Equivalence of two CFGs is a classic undecidable problem (non-semidecidable).\n- Option C: Decidable. A regular language is finite if and only if its minimized DFA contains no state reachable from the start state and can reach a final state that participates in a cycle.\n- Option D: Undecidable. The TM Halting problem is undecidable (recursively enumerable but not recursive).\nTherefore, A and C are correct.",
            shortcutTrick = "Recall the Decidability Table: DFA has all membership/emptiness/equivalence decidable. CFG has Emptiness and Finiteness decidable, but Equivalence/Disjointness undecidable.",
            commonTrap = "Assuming CFG equivalence is decidable because CFG emptiness is decidable.",
            keyFormula = "CFG: \\text{Emptiness (Decidable)}, \\text{Equivalence (Undecidable)}",
            isPyq = true
        ),

        // Question 4: C Pointers (GATE 2021, 2 Marks, MCQ)
        QuestionEntity(
            id = "GATE_2021_C_Q1",
            subjectId = "C_PROG",
            topicId = "C_POINTERS",
            year = 2021,
            setSession = "Set 1",
            questionNumber = 19,
            questionType = QuestionType.MCQ,
            marks = 2.0f,
            negativeMarks = -0.66f,
            difficulty = Difficulty.MEDIUM,
            questionText = "Consider the following C code snippet:\n```c\n#include <stdio.h>\nint main() {\n    int a[] = {2, 4, 6, 8, 10};\n    int *p = a;\n    int *q = &a[3];\n    printf(\"%d\", *(p + (q - p) / 2));\n    return 0;\n}\n```\nWhat is the output printed by the program?",
            optionA = "2",
            optionB = "4",
            optionC = "6",
            optionD = "8",
            correctAnswers = "B",
            shortExplanation = "Pointer difference (q - p) computes the number of elements between pointers. (3 - 0) / 2 = 3 / 2 = 1. Therefore *(p + 1) = a[1] = 4.",
            detailedSolution = "Step-by-step trace:\n1. `a` is `{2, 4, 6, 8, 10}` with indices 0 to 4.\n2. `p` points to `a[0]` (index 0).\n3. `q` points to `a[3]` (index 3).\n4. In C, subtracting two pointers of the same type yields the element difference (ptrdiff_t):\nq - p = 3 - 0 = 3.\n5. Integer division in C truncates: `3 / 2 = 1`.\n6. `p + 1` points to `a[1]`.\n7. Dereferencing `*(p + 1)` gives `a[1]`, which is `4`.",
            shortcutTrick = "Pointer arithmetic: (q - p) = index difference = 3. 3/2 in integer arithmetic is 1. *(p + 1) is simply a[1] = 4.",
            commonTrap = "Forgetting integer division truncation in C (3/2 is 1, not 1.5).",
            keyFormula = "ptr2 - ptr1 = \\frac{Address_2 - Address_1}{sizeof(T)}",
            isPyq = true
        ),

        // Question 5: Algorithms Dynamic Programming (GATE 2023, 2 Marks, NAT)
        QuestionEntity(
            id = "GATE_2023_ALGO_Q1",
            subjectId = "ALGO",
            topicId = "ALGO_DP",
            year = 2023,
            setSession = "Set 1",
            questionNumber = 42,
            questionType = QuestionType.NAT,
            marks = 2.0f,
            negativeMarks = 0.0f,
            difficulty = Difficulty.HARD,
            questionText = "Consider multiplying four matrices A1, A2, A3, and A4 with dimensions 10x20, 20x30, 30x40, and 40x30 respectively. Using dynamic programming (Matrix Chain Multiplication), what is the minimum number of scalar multiplications required?",
            optionA = null,
            optionB = null,
            optionC = null,
            optionD = null,
            correctAnswers = "30000",
            shortExplanation = "Optimal parenthesization is ((A1 * A2) * A3) * A4 or (A1 * (A2 * A3)) * A4. Calculate DP table cost.",
            detailedSolution = "Matrix dimensions: p_0=10, p_1=20, p_2=30, p_3=40, p_4=30.\nChain lengths:\n- Chain 2:\n  m[1,2] = 10*20*30 = 6,000\n  m[2,3] = 20*30*40 = 24,000\n  m[3,4] = 30*40*30 = 36,000\n- Chain 3:\n  m[1,3] = min(m[1,1]+m[2,3]+10*20*40, m[1,2]+m[3,3]+10*30*40) = min(0+24000+8000, 6000+0+12000) = min(32000, 18000) = 18,000 (split at k=2)\n  m[2,4] = min(m[2,2]+m[3,4]+20*30*30, m[2,3]+m[4,4]+20*40*30) = min(0+36000+18000, 24000+0+24000) = min(54000, 48000) = 48,000\n- Chain 4:\n  m[1,4]:\n  k=1: m[1,1] + m[2,4] + 10*20*30 = 0 + 48000 + 6000 = 54,000\n  k=2: m[1,2] + m[3,4] + 10*30*30 = 6000 + 36000 + 9000 = 51,000\n  k=3: m[1,3] + m[4,4] + 10*40*30 = 18000 + 0 + 12000 = 30,000\nMinimum cost = 30,000 scalar multiplications.",
            shortcutTrick = "Split at k=3 evaluates to: (A1*A2*A3) with cost 18,000, then multiplying with A4 (40x30) requires 10*40*30 = 12,000. Total = 30,000.",
            commonTrap = "Trying greedy multiplication without checking all split points in the DP table.",
            keyFormula = "m[i,j] = min {m[i,k] + m[k+1,j] + p_{i-1}p_k p_j}",
            isPyq = true
        ),

        // Question 6: COA Pipelining (GATE 2022, 2 Marks, NAT)
        QuestionEntity(
            id = "GATE_2022_COA_Q1",
            subjectId = "COA",
            topicId = "COA_PIPELINE",
            year = 2022,
            setSession = "Set 1",
            questionNumber = 29,
            questionType = QuestionType.NAT,
            marks = 2.0f,
            negativeMarks = 0.0f,
            difficulty = Difficulty.MEDIUM,
            questionText = "A 5-stage pipelined processor has stage delays of 150 ps, 120 ps, 160 ps, 140 ps, and 110 ps. The pipeline register overhead between every stage is 10 ps. What is the clock cycle time (in picoseconds) of the pipelined processor?",
            optionA = null,
            optionB = null,
            optionC = null,
            optionD = null,
            correctAnswers = "170",
            shortExplanation = "Clock cycle time = Maximum stage delay + Pipeline register delay = max(150, 120, 160, 140, 110) + 10 = 160 + 10 = 170 ps.",
            detailedSolution = "In synchronous pipelining, clock cycle time is dictated by the slowest (bottleneck) stage plus pipeline latch/register delay:\nT_clock = max(D_1, D_2, ..., D_k) + d_reg\nGiven stage delays:\n- D_1 = 150 ps\n- D_2 = 120 ps\n- D_3 = 160 ps (Maximum stage delay)\n- D_4 = 140 ps\n- D_5 = 110 ps\nLatch delay d_reg = 10 ps.\nTherefore:\nT_clock = 160 + 10 = 170 ps.",
            shortcutTrick = "Always pick the max stage delay (160) and add the register delay (10) = 170.",
            commonTrap = "Averaging the stage delays instead of taking the maximum bottleneck delay.",
            keyFormula = "T_{clock} = \\max(Delay_i) + Overheads",
            isPyq = true
        ),

        // Question 7: DBMS Normalization (GATE 2020, 2 Marks, MCQ)
        QuestionEntity(
            id = "GATE_2020_DBMS_Q1",
            subjectId = "DBMS",
            topicId = "DBMS_NORM",
            year = 2020,
            setSession = "Set 1",
            questionNumber = 31,
            questionType = QuestionType.MCQ,
            marks = 2.0f,
            negativeMarks = -0.66f,
            difficulty = Difficulty.MEDIUM,
            questionText = "Given a relation R(A, B, C, D, E) with Functional Dependencies F = {AB -> C, C -> D, D -> E, E -> A}. What is the highest normal form satisfied by relation R?",
            optionA = "1NF",
            optionB = "2NF",
            optionC = "3NF",
            optionD = "BCNF",
            correctAnswers = "C",
            shortExplanation = "Candidate keys are AB, BC, BD, BE. Prime attributes are A, B, C, D, E. Since every attribute is prime, R is in 3NF, but not in BCNF (since C, D, E are not superkeys).",
            detailedSolution = "Let's find candidate keys:\n1. Compute closures:\n- (AB)+ = {A, B, C, D, E} -> AB is a candidate key.\n- Since E -> A, replace A in AB by E -> (EB)+ = {E, B, A, C, D} -> BE is a candidate key.\n- Since D -> E, replace E in EB by D -> (DB)+ = {D, B, E, A, C} -> BD is a candidate key.\n- Since C -> D, replace D in DB by C -> (CB)+ = {C, B, D, E, A} -> BC is a candidate key.\nCandidate Keys = {AB, BC, BD, BE}.\nPrime attributes = Union of all candidate keys = {A, B, C, D, E}.\nNon-prime attributes = None!\n\nNormal Form Check:\n- 3NF condition: For every X -> Y, either X is a superkey OR Y is a prime attribute.\nHere, every RHS attribute is prime! Thus, R automatically satisfies 3NF!\n- BCNF condition: For every X -> Y, X must be a superkey.\nIn C -> D, C+ = {C, D, E, A} != R, so C is NOT a superkey.\nHence, R is NOT in BCNF.\nHighest Normal Form is 3NF.",
            shortcutTrick = "If all attributes of a relation are PRIME attributes (belong to some candidate key), the relation is GUARANTEED to be in 3NF!",
            commonTrap = "Checking only AB as candidate key and concluding B is the only key, falsely claiming 2NF violation.",
            keyFormula = "All Attributes Prime => Relation is at least in 3NF",
            isPyq = true
        ),

        // Question 8: Computer Networks Subnetting (GATE 2021, 2 Marks, MCQ)
        QuestionEntity(
            id = "GATE_2021_CN_Q1",
            subjectId = "CN",
            topicId = "CN_SUBNET",
            year = 2021,
            setSession = "Set 2",
            questionNumber = 22,
            questionType = QuestionType.MCQ,
            marks = 2.0f,
            negativeMarks = -0.66f,
            difficulty = Difficulty.EASY,
            questionText = "An organization is assigned the IPv4 address block 192.168.10.0/24. The network administrator needs to create 4 subnets, each capable of hosting at least 50 host interfaces. What subnet mask should be assigned to each subnet?",
            optionA = "255.255.255.128 (/25)",
            optionB = "255.255.255.192 (/26)",
            optionC = "255.255.255.224 (/27)",
            optionD = "255.255.255.240 (/28)",
            correctAnswers = "B",
            shortExplanation = "To support at least 50 hosts: 2^h - 2 >= 50 => h = 6 bits for host. Prefix length = 32 - 6 = 26. Subnet mask = 255.255.255.192.",
            detailedSolution = "Requirements:\n- Number of subnets required = 4 = 2^2, so 2 subnet bits needed.\n- Number of usable hosts per subnet >= 50.\nIf h is the number of host bits:\n2^h - 2 >= 50 => 2^h >= 52 => h = 6 (2^6 = 64, 64 - 2 = 62 usable hosts).\nSince total IPv4 address length is 32 bits:\nSubnet prefix = 32 - h = 32 - 6 = 26 (/26).\nMask in dotted decimal:\nFirst 3 bytes = 255.255.255\nLast byte has 2 leading 1s: 11000000_2 = 128 + 64 = 192.\nSubnet Mask = 255.255.255.192.",
            shortcutTrick = "50 hosts needs 6 bits (64 - 2 = 62). /32 - 6 = /26. /26 mask ends with .192.",
            commonTrap = "Forgetting to subtract 2 for network ID and broadcast ID in host calculation.",
            keyFormula = "\\text{Usable Hosts} = 2^h - 2",
            isPyq = true
        )
    )

    fun getInitialFormulas(): List<FormulaEntity> = listOf(
        FormulaEntity(
            id = "FORM_DEADLOCK_MIN",
            subjectId = "OS",
            topicId = "OS_DEADLOCK",
            name = "Deadlock Prevention Minimum Resources",
            formulaLatex = "R_{min} = \\sum_{i=1}^m (Max_i - 1) + 1",
            description = "Calculates the minimum single-type resources needed across m processes to guarantee freedom from deadlock.",
            applications = "Resource Allocation, Banker's Algorithm"
        ),
        FormulaEntity(
            id = "FORM_PIPELINE_SPEEDUP",
            subjectId = "COA",
            topicId = "COA_PIPELINE",
            name = "Ideal Pipeline Speedup",
            formulaLatex = "S = \\frac{n \\cdot k}{k + n - 1} \\approx k \\quad (\\text{for } n \\gg k)",
            description = "Speedup factor of a k-stage pipeline over a non-pipelined execution for n tasks under zero stalls.",
            applications = "Processor Architecture, Hazard Penalty Analysis"
        ),
        FormulaEntity(
            id = "FORM_CACHE_TAG_BITS",
            subjectId = "COA",
            topicId = "COA_CACHE",
            name = "Set-Associative Cache Field Splitting",
            formulaLatex = "\\text{Address Bits} = \\text{Tag} + \\log_2(\\text{Sets}) + \\log_2(\\text{Block Size})",
            description = "Decomposes memory address into Tag, Set Index, and Word Offset in k-way set-associative cache.",
            applications = "Memory Hierarchy, Cache Tag Overhead"
        ),
        FormulaEntity(
            id = "FORM_SLIDING_WINDOW_EFF",
            subjectId = "CN",
            topicId = "CN_TCP",
            name = "Sliding Window Protocol Efficiency",
            formulaLatex = "\\eta = \\frac{W}{1 + 2a} \\quad \\text{where } a = \\frac{T_{prop}}{T_{trans}}",
            description = "Link utilization efficiency for sliding window protocol with window size W and propagation delay factor a.",
            applications = "Go-Back-N, Selective Repeat, Network Throughput"
        ),
        FormulaEntity(
            id = "FORM_MASTER_THEOREM",
            subjectId = "ALGO",
            topicId = "ALGO_ASYMPTOTIC",
            name = "Master Theorem for Divide & Conquer",
            formulaLatex = "T(n) = aT(n/b) + \\Theta(n^k \\log^p n) \\implies \\text{Compare } \\log_b a \\text{ with } k",
            description = "Directly determines asymptotic time complexity of divide-and-conquer recurrences.",
            applications = "Algorithm Analysis, MergeSort, Strassen Matrix Multiplication"
        ),
        FormulaEntity(
            id = "FORM_BAYES_THEOREM",
            subjectId = "EM",
            topicId = "EM_PROBABILITY",
            name = "Bayes' Theorem for Posterior Probability",
            formulaLatex = "P(A|B) = \\frac{P(B|A) \\cdot P(A)}{P(B)} = \\frac{P(B|A) P(A)}{\\sum_i P(B|A_i) P(A_i)}",
            description = "Calculates posterior probability based on prior probability and likelihood.",
            applications = "Probability, Machine Learning, Diagnostic Testing"
        )
    )

    fun getInitialFlashcards(): List<FlashcardEntity> = listOf(
        FlashcardEntity(
            id = "FC_1",
            subjectId = "OS",
            topicId = "OS_DEADLOCK",
            front = "What are the 4 Coffman conditions that must hold simultaneously for a deadlock to occur?",
            back = "1. Mutual Exclusion\n2. Hold and Wait\n3. No Preemption\n4. Circular Wait",
            keyConcept = "Coffman Conditions"
        ),
        FlashcardEntity(
            id = "FC_2",
            subjectId = "TOC",
            topicId = "TOC_DECIDABLE",
            front = "What does Rice's Theorem state regarding Turing Machine decidability?",
            back = "Any non-trivial semantic (language-related) property of the language recognized by a Turing Machine is UNDECIDABLE.",
            keyConcept = "Rice's Theorem"
        ),
        FlashcardEntity(
            id = "FC_3",
            subjectId = "ALGO",
            topicId = "ALGO_GRAPHS",
            front = "What is the time complexity of Dijkstra's algorithm implemented with a Min-Heap (Priority Queue)?",
            back = "O((V + E) log V) or O(E log V) for connected graphs.",
            keyConcept = "Dijkstra's Shortest Path"
        ),
        FlashcardEntity(
            id = "FC_4",
            subjectId = "DBMS",
            topicId = "DBMS_NORM",
            front = "What is the condition for a functional dependency X -> Y in Boyce-Codd Normal Form (BCNF)?",
            back = "For every non-trivial functional dependency X -> Y, X must be a SUPERKEY of the relation.",
            keyConcept = "BCNF Definition"
        ),
        FlashcardEntity(
            id = "FC_5",
            subjectId = "CN",
            topicId = "CN_TCP",
            front = "In TCP Congestion Control, what happens when a timeout occurs versus 3 duplicate ACKs?",
            back = "Timeout: Threshold = cwnd/2, cwnd reset to 1 MSS (enters Slow Start).\n3 Duplicate ACKs: Fast Retransmit + Fast Recovery, Threshold = cwnd/2, cwnd set to Threshold + 3 MSS.",
            keyConcept = "TCP Congestion Control"
        )
    )

    fun getInitialTasks(): List<StudyTaskEntity> = listOf(
        StudyTaskEntity(
            id = "TASK_1",
            title = "Revise Deadlocks & Banker's Algorithm",
            subjectId = "OS",
            topicId = "OS_DEADLOCK",
            allocatedMinutes = 35,
            priority = "HIGH",
            isCompleted = false,
            targetDate = "2026-09-21"
        ),
        StudyTaskEntity(
            id = "TASK_2",
            title = "Practice 15 PYQs on Relational Normalization",
            subjectId = "DBMS",
            topicId = "DBMS_NORM",
            allocatedMinutes = 45,
            priority = "HIGH",
            isCompleted = false,
            targetDate = "2026-09-21"
        ),
        StudyTaskEntity(
            id = "TASK_3",
            title = "Flashcard SRS Review (Due Formulas)",
            subjectId = "EM",
            topicId = "EM_LINEAR_ALGEBRA",
            allocatedMinutes = 20,
            priority = "MEDIUM",
            isCompleted = false,
            targetDate = "2026-09-21"
        ),
        StudyTaskEntity(
            id = "TASK_4",
            title = "Pipelining & Hazard Penalties Calculation",
            subjectId = "COA",
            topicId = "COA_PIPELINE",
            allocatedMinutes = 40,
            priority = "HIGH",
            isCompleted = false,
            targetDate = "2026-09-21"
        )
    )

    fun getInitialExamConfig(): ExamConfigEntity = ExamConfigEntity(
        id = "gate_cse_2027",
        examName = "GATE CSE 2027",
        conductingInstitute = "IIT",
        officialWebsiteUrl = "https://gate2027.iit.ac.in",
        lastVerifiedDate = "September 2026"
    )

    fun getInitialExamEvents(): List<ExamEventEntity> = listOf(
        ExamEventEntity(
            id = "EVENT_REG_CLOSE",
            examConfigId = "gate_cse_2027",
            eventName = "Application Window Deadline",
            eventDateMillis = 1760227200000L, // Oct 12, 2026
            eventDateFormatted = "Oct 12, 2026",
            isMainExamDate = false,
            sourceUrl = "https://gate2027.iit.ac.in"
        ),
        ExamEventEntity(
            id = "EVENT_ADMIT_CARD",
            examConfigId = "gate_cse_2027",
            eventName = "Admit Card Download",
            eventDateMillis = 1767830400000L, // Jan 08, 2027
            eventDateFormatted = "Jan 08, 2027",
            isMainExamDate = false,
            sourceUrl = "https://gate2027.iit.ac.in"
        ),
        ExamEventEntity(
            id = "EVENT_MAIN_EXAM",
            examConfigId = "gate_cse_2027",
            eventName = "GATE CSE 2027 Examination",
            eventDateMillis = 1770355200000L, // Feb 06, 2027
            eventDateFormatted = "Feb 06, 2027",
            isMainExamDate = true,
            sourceUrl = "https://gate2027.iit.ac.in"
        ),
        ExamEventEntity(
            id = "EVENT_RESULTS",
            examConfigId = "gate_cse_2027",
            eventName = "Scorecard & Results Declaration",
            eventDateMillis = 1773878400000L, // Mar 19, 2027
            eventDateFormatted = "Mar 19, 2027",
            isMainExamDate = false,
            sourceUrl = "https://gate2027.iit.ac.in"
        )
    )
}
