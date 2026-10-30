# Pearl collection interface

BidPro uses a light collector-gallery design with pearl backgrounds, luminous
glass controls, sage details and emerald actions. Editorial typography and
layered white surfaces give the interface depth while keeping text and controls
clear. The homepage pairs its headline with a framed editorial still life in a
split layout that stacks on mobile. Actual marketplace counts, a featured auction,
category navigation and working live/upcoming filters connect it to the catalog.
The compact introduction gives collections more space, with icon-led category
cards and stronger product title, status, value and action hierarchy. The featured
auction dock appears on larger screens; mobile keeps the hero focused on browsing.

The catalog supports ending-soonest, newest and price sorting through URL
parameters. Clearing search/category/status filters preserves the selected sort.
Navigation, labels and supporting details use larger text, and mobile form inputs
use 16px text to avoid automatic zoom. The footer provides useful marketplace,
account and support routes with an invitation suited to the signed-in role.

The generated editorial image is decorative and does not claim to show an actual
auction listing. Seller-uploaded photos remain the source of product imagery;
category illustrations cover missing photos. See [the saved artwork prompt and
provenance](asset-prompts.md).

Authenticated auction workspaces retain a persistent role-specific sidebar and
rich dashboard layouts. The administrator overview includes real marketplace
metrics, recent-listing status breakdowns and live auction records. Seller metrics
come from the seller's own listings. Tables, profile forms, conversations,
notifications, recovery and checkout share the same light material and spacing
system. On small screens, role navigation scrolls horizontally and dense tables
scroll within their panels.

The material hierarchy follows [Apple's materials guidance](https://developer.apple.com/design/human-interface-guidelines/materials).
Navigation and floating controls use blur and translucent edges; data surfaces
remain clear. The interface declares a light color scheme, keeps native form
controls readable and respects reduced motion, reduced transparency and increased
contrast. The reduced-transparency fallback uses solid light surfaces. Content
panels avoid filtered ancestors so checkout dialogs stay fixed to the viewport.

Theme tokens and responsive styles live in `apps/web/src/app/styles.css`.
The shell and role navigation live in `Layout.jsx`, while `App.jsx` wraps
workspaces in the sidebar/content layout.

Run `npm.cmd run test:visual` against the local app to capture 17 desktop/mobile
views in ignored `test-results/glass/`. It checks image decoding, auction filters,
overflow, catalog sorting and filter reset, mobile sign-in readability and touch
targets, browser errors, API failures, actual glass blur, reduced motion and the
solid reduced-transparency fallback. The auction, local checkout and
communications/recovery browser workflows pass after this redesign.
