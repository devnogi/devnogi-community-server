# Public sitemap projection — v1

Provider: Community. Consumer: React server-side XML routes. Fixed plan: `search-seo-v1-20261113`, revision 1 (`15F78F576F0F51CD2EBED1DAE13244D10E5C36077F9B48917A060B415CE87DB8`).

## GET endpoints

- `/api/posts/sitemap` (Gateway `/dcs/api/posts/sitemap`)
- `/api/outfits/sitemap` (Gateway `/dcs/api/outfits/sitemap`)

Both accept optional `cursor` and `limit`. Missing cursor starts at zero; a supplied cursor is a positive canonical decimal signed-64-bit ID (no whitespace, sign, leading zeros or zero). Limit is 1..1000, defaults to 1000. Invalid input returns HTTP 400 with `INVALID_PAGE_REQUEST_EXCEPTION`. The endpoints need no login and do not accept mutations.

The normal success envelope contains `{items:[{id,updatedAt}],nextCursor,hasNext}`. See [the synthetic JSON fixture](../contracts/public-sitemap.example.json). Rows are sorted by ascending ID and strictly after the supplied cursor. A query fetches limit+1 rows; the response contains at most limit rows. `nextCursor` is the last emitted ID only when another row exists; otherwise null. An empty tail is a successful empty array with `hasNext:false` and `nextCursor:null`.

Posts exclude deleted, blocked and draft records. Outfits exclude deleted records. SQL projects only ID and modification time: no content hydration, joins to authors or reactions, MinIO access or signed URLs. DB failures return a server error rather than a successful empty page. Responses use `Cache-Control: no-store`; React owns its bounded XML caching policy.

`updatedAt` is the actual modification timestamp as an ISO offset string, or null when no timestamp is available. Legacy post timestamps use the existing Asia/Seoul storage convention; outfit timestamps use UTC, matching their writer. Null timestamps must be omitted from XML `lastmod`, not replaced with the current time.

Latest public feed queries additionally sort equal creation times by ID descending. Explicit ID sorting and popularity ranking remain unchanged. No schema migration or new dependency is required.

## Verification

- `PublicSitemapRepositoryTest`: executes projection SQL against isolated H2 tables, checks hidden rows, gaps, bounded lookahead, timestamp conventions and immediate deletion/blocking changes.
- `PublicSitemapControllerTest`: verifies anonymous envelope/fixture, invalid-input HTTP 400, database-failure HTTP 500, and GET-only behavior.
- `PostServiceTest`: verifies the latest-feed tie breaker and preservation of explicit sorting.
- `PostRepositoryTest`: traverses pages of records with identical creation times without duplicates or omissions.
- Standard gate: `gradlew.bat verify --no-daemon`; workflow contract: `node --test scripts/workflow-contract.test.mjs`.
