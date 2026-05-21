# TARGET DIRECTORY: docs/03-architecture/c4/
# TARGET FILENAME: C4_CONTEXT.md (single file)

# C4 Context Diagram for {{system}}

```mermaid
C4Context
  title System Context - {{system}}

  Person(actor1, "Actor 1", "Description")
  Person(actor2, "Actor 2", "Description")

  System(system, "{{system}}", "System description")

  System_Ext(ext1, "External System 1", "Description")

  Rel(actor1, system, "Uses", "HTTPS/JSON")
  Rel(actor2, system, "Uses", "HTTPS/JSON")
  Rel(system, ext1, "Calls", "Protocol")

  UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```
