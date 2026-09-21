from fastapi import APIRouter, Query
from typing import List, Optional
from backend.app.schemas.resource import ResourceDTO

router = APIRouter(prefix="/resources", tags=["resources"])

CURATED_RESOURCES: List[ResourceDTO] = [
    ResourceDTO(
        id="RES_OS_GALVIN",
        subject_id="OS",
        topic_id="OS_DEADLOCK",
        title="Operating System Concepts (Silberschatz, Galvin & Gagne)",
        provider="Galvin / Wiley",
        resource_type="BOOK",
        description="Standard textbook for GATE OS. Detailed treatment of Deadlock Characterization, Banker's Algorithm, and Resource Allocation Graphs.",
        url="https://www.os-book.com",
        recommended_chapters="Chapters 7 & 8",
        is_free=False
    ),
    ResourceDTO(
        id="RES_OS_GATE_SMASHERS",
        subject_id="OS",
        topic_id="OS_DEADLOCK",
        title="Deadlocks in Operating System Complete Playlist - Gate Smashers",
        provider="Gate Smashers (Varun Singla)",
        resource_type="PLAYLIST",
        description="High-yield visual lectures covering Banker's algorithm numericals, safe state calculations, and deadlock prevention techniques.",
        url="https://www.youtube.com/playlist?list=GateSmashers_OS",
        is_free=True
    ),
    ResourceDTO(
        id="RES_OS_KNOWLEDGE_GATE",
        subject_id="OS",
        topic_id="OS_DEADLOCK",
        title="Operating Systems Deadlocks Mastery - Knowledge Gate",
        provider="Knowledge Gate (Sanchit Jain)",
        resource_type="PLAYLIST",
        description="Step-by-step problem solving for resource allocation matrix queries and wait-for graph cycle detection.",
        url="https://www.youtube.com/playlist?list=KnowledgeGate_OS",
        is_free=True
    ),
    ResourceDTO(
        id="RES_OS_NPTEL",
        subject_id="OS",
        topic_id=None,
        title="Operating Systems Rigorous Lectures - NPTEL (IIT Delhi)",
        provider="NPTEL (Prof. Sorav Bansal)",
        resource_type="NPTEL",
        description="Academic deep dive into concurrency, process synchronization, virtualization, and kernel lock implementations.",
        url="https://nptel.ac.in/courses/106106144",
        is_free=True
    ),
    ResourceDTO(
        id="RES_TOC_AMIT_KHURANA",
        subject_id="TOC",
        topic_id="TOC_DFA",
        title="Theory of Computation Complete Course - Amit Khurana",
        provider="Amit Khurana",
        resource_type="PLAYLIST",
        description="Renowned comprehensive playlist with rigorous proofs of regular language closure properties and minimal DFA construction.",
        url="https://www.youtube.com/playlist?list=AmitKhurana_TOC",
        is_free=True
    ),
    ResourceDTO(
        id="RES_DM_DEEPAK_POONIA",
        subject_id="DM",
        topic_id="DM_GRAPH",
        title="Discrete Mathematics Complete - Deepak Poonia / GO Classes",
        provider="Deepak Poonia (GO Classes)",
        resource_type="COURSE",
        description="Rigorous mathematical foundations for Graph Theory, Combinatorics, Logic, and Proof techniques.",
        url="https://www.goclasses.in",
        is_free=True
    ),
    ResourceDTO(
        id="RES_COA_VISHVADEEP_GOTHI",
        subject_id="COA",
        topic_id="COA_PIPE",
        title="Computer Architecture & Pipelining - Vishvadeep Gothi",
        provider="Vishvadeep Gothi",
        resource_type="PLAYLIST",
        description="Expert coverage of instruction pipelining, pipeline stalls/hazards, cache miss penalties, and branch prediction.",
        url="https://www.youtube.com/playlist?list=VishvadeepGothi_COA",
        is_free=True
    ),
    ResourceDTO(
        id="RES_ALGO_CLRS",
        subject_id="ALGO",
        topic_id="ALGO_ASYMPTOTIC",
        title="Introduction to Algorithms (CLRS - Cormen, Leiserson, Rivest, Stein)",
        provider="Cormen (CLRS) / MIT Press",
        resource_type="BOOK",
        description="Gold standard algorithm compendium. Master Theorem proofs, recurrence trees, and asymptotic upper/lower bounds.",
        url="https://mitpress.mit.edu/books/introduction-algorithms",
        recommended_chapters="Chapters 3 & 4",
        is_free=False
    ),
    ResourceDTO(
        id="RES_GATE_OVERFLOW_PYQ",
        subject_id="ALL",
        topic_id=None,
        title="GATE Overflow Topicwise PYQs (1987-2026)",
        provider="GateOverflow & GatePlus",
        resource_type="PRACTICE_SET",
        description="Over 35 years of authentic GATE CSE questions with verified, community-reviewed faculty discussions.",
        url="https://gateoverflow.in",
        is_free=True
    )
]

@router.get("", response_model=List[ResourceDTO])
async def get_resources(
    subject_id: Optional[str] = Query(None),
    resource_type: Optional[str] = Query(None),
    provider: Optional[str] = Query(None)
):
    results = CURATED_RESOURCES
    if subject_id and subject_id != "ALL":
        results = [r for r in results if r.subject_id == subject_id or r.subject_id == "ALL"]
    if resource_type:
        results = [r for r in results if r.resource_type == resource_type]
    if provider:
        results = [r for r in results if provider.lower() in r.provider.lower()]
    return results
