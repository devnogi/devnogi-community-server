# Outfit snap — revision 2

The provider is Community `/api/outfits`, reached through Gateway `/dcs/api/outfits`.
React owns the same-origin BFF and screens. Existing post tables and endpoints are unchanged.

## Content and slots

`HEAD`, `ACCESSORY_1`, `ACCESSORY_2`, `FACE`, `CLOTHING`, `TAIL`, `HANDS`, `FEET`,
`WEAPON`, `ROBE`, `MOTION` are the accepted slots. Clothing, hands and feet are required.
Each slot appears at most once. `inputType: CATALOG` stores the exact
`name`, `topCategory`, `subCategory` triple. Only `MOTION` accepts `inputType: CUSTOM`;
its name has at most 100 Unicode code points. Custom motions are not added to Batch's catalog.

Posts have optional plain text (1,000 code points), up to ten tags (20 code points each),
and one to five JPEG/PNG/WebP/GIF images (5 MiB each). There is no title or video.
POST and PUT use multipart `data` (JSON) and repeated `media` files. JSON contains
`content`, `tags`, `items` and `mediaOrder`. Each media order entry has exactly one of
`imageId` (an existing image belonging to this outfit) or `uploadIndex` (zero-based
index in the uploaded files). Every new file must appear exactly once; omitted existing
images are removed. The first entry is the cover.

## Reading and searching

All responses use the existing `ApiResponse` envelope, except empty 204 responses and
image redirects. Timestamps are ISO UTC instants with `Z`. Dynamic endpoints use `no-store`.

GET `/api/outfits` accepts `sort=LATEST|POPULAR`, `size` (1–50, default 24), `page`
(zero-based, popular only), `cursor` (latest only), `items` (JSON array of item references),
repeated `tags`, and optional `motion` (normalized exact motion name). In item filters,
`slot` may be absent for any-slot matching. Conditions are ANDed. Accessory filters ignore
the two slot numbers but two filters must match two different equipped rows.

The response data is `{content, nextCursor, hasNext}`. Latest uses `(created_at,id)`
keysets. Popular uses posts from the last seven days, ordered by current like count,
created time and ID descending. Ranking may change between pages; React deduplicates IDs
and restarts after refresh/filter/own-reaction changes.

GET `/items?slot&query` offers Batch-backed catalog candidates; `/motions?query` offers
registered custom-motion names. `/mine` and `/bookmarks` require authentication and use
latest cursors. `/viewer-state?ids=1,2` returns `{outfitId,liked,bookmarked}` for at most
50 IDs. Public outfit data does not include viewer state.

## Writes and concurrency

Authentication comes only from the existing Gateway-verified user context. Only owners
can edit/delete outfits and comments; there is no administrator ownership bypass.
Catalog validation happens before upload and before the write transaction. Unchanged
catalog selections on edits are not revalidated. Writes recheck ownership and active
status while holding the outfit row lock. Likes and bookmarks use explicit idempotent
PUT/DELETE endpoints, unique keys and transactionally updated counts.

Comments accept `{content,parentId?}` and have one reply level. GET `/ID/comments`
returns roots; `parentId` selects replies. The comment page uses `page`, `size` and
`{content,nextCursor:null,hasNext}`. Deleted parents retain a blank placeholder while
live replies exist. Comment count includes only active comments and replies.

POST `/ID/shares` accepts `{requestId: UUID, channel: NATIVE|CLIPBOARD}`. Clients send it
only after share/copy success, not cancellation; retries use the same UUID. Counts mean
successful browser share/copy actions, not confirmed external delivery.

Uploads are cleaned after rollback. Removed media are deleted only after commit; failed
cleanup is logged. Deleted outfits disappear from all public views and reject reactions.
GET `/ID/images/IMAGE_ID` verifies active outfit/image ownership, then returns a fresh
presigned URL in a no-store 302 for stable sharing metadata.

## Catalog and verification

`OUTFIT_CATALOG_BASE_URL` is the Gateway URL ending in `/oab`; no private Redis keys or
Batch DB are accessed. Batch owns the one-hour summary TTL, cache-miss DB load, Redis
error fallback and sync eviction. Community makes best-effort startup/hourly category
GETs; cache hits do not renew TTL. Validation uses at most three concurrent calls, a
two-second connection timeout, three-second read timeout and eight-second overall budget.
Unavailable upstreams return 503 rather than an empty successful catalog. Feed and custom
motion queries use SQL indexes, not a new Redis cache.

Run `gradlew.bat verify --no-daemon` for unit/in-memory/MVC tests and the application build.
Run `gradlew.bat outfitMysqlTest --no-daemon` with `TEST_MYSQL_URL`, `TEST_MYSQL_USER`,
`TEST_MYSQL_PASSWORD` against a disposable localhost MySQL 8 instance. This task cleans
only its `outfit_snap_fresh_test` and `outfit_snap_upgrade_test` schemas, validates fresh
and upgrade Flyway paths, preserves a pre-upgrade marker, then repeats behavior tests
against real MySQL. Never point this task at production or a shared developer database.
The dedicated PR workflow provisions a disposable MySQL service for this check.
