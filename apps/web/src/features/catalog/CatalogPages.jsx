import { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import useResource from '../../shared/hooks/useResource';
import useMutation from '../../shared/hooks/useMutation';
import { imageUrl, request } from '../../shared/api/client';
import { auctionState, canBid, dateTime, label, minimumBid, money } from '../../shared/lib/format';
import {
  Badge,
  Empty,
  Feedback,
  Field,
  Notice,
  PageHeading,
  PendingAccount,
  ResourceState,
} from '../../shared/components/UI';

function artworkFor(product) {
  const text = `${product.title || ''} ${product.categoryName || ''}`.toLowerCase();
  if (/camera|photograph|lens/.test(text)) return { kind: 'camera', icon: 'camera2' };
  if (/watch|clock|timepiece/.test(text)) return { kind: 'watch', icon: 'watch' };
  if (/lamp|light|living|furniture/.test(text)) return { kind: 'lighting', icon: 'lamp' };
  if (/audio|headphone|speaker|music/.test(text)) return { kind: 'audio', icon: 'headphones' };
  if (/book|print|literature/.test(text)) return { kind: 'book', icon: 'book' };
  if (/art|painting|sculpture/.test(text)) return { kind: 'art', icon: 'palette' };
  return { kind: 'object', icon: 'box-seam' };
}

function collectionIcon(name = '') {
  const text = name.toLowerCase();
  if (/electronic|tech|camera/.test(text)) return 'camera2';
  if (/collect|watch|jewel/.test(text)) return 'gem';
  if (/home|living|furniture/.test(text)) return 'lamp';
  if (/art|print/.test(text)) return 'palette';
  if (/fashion|cloth/.test(text)) return 'bag';
  if (/book/.test(text)) return 'book';
  if (/audio|music/.test(text)) return 'headphones';
  return 'collection';
}

function ProductArtwork({ product, large = false, showcase = false }) {
  const image = product.imageUrls?.[0] || product.images?.[0]?.imageUrl;
  const artwork = artworkFor(product);
  return (
    <div
      className={`product-art art-${artwork.kind} ${large ? 'product-art-large' : ''} ${showcase ? 'product-art-showcase' : ''}`}
    >
      {image ? (
        <img src={imageUrl(image)} alt={product.title} loading="lazy" />
      ) : (
        <>
          <span className="art-orbit" aria-hidden="true" />
          <span className="art-orbit art-orbit-inner" aria-hidden="true" />
          <span className="product-object" aria-hidden="true">
            <i className={`bi bi-${artwork.icon}`} />
          </span>
          <span className="art-caption">{label(product.productCondition)} condition</span>
        </>
      )}
    </div>
  );
}
export function ProductCard({ product, categories = [], index }) {
  const state = auctionState(product);
  return (
    <article className="product-card">
      <div className="product-card-topline" aria-hidden="true">
        <span className="product-sequence">
          {index === undefined
            ? 'BIDPRO SELECTION'
            : `SELECTION ${String(index + 1).padStart(2, '0')}`}
        </span>
        <i className="bi bi-arrow-up-right" />
      </div>
      <Link
        className="art-link"
        tabIndex={-1}
        aria-hidden="true"
        to={`/products/${product.productID}`}
      >
        <ProductArtwork product={product} />
      </Link>
      <div className="product-card-content">
        <div className="d-flex justify-content-between gap-2 align-items-center">
          <span className="category-label">
            {categories.find((category) => category.categoryID === product.categoryID)?.name ||
              'Auction item'}
          </span>
          <Badge value={state} />
        </div>
        <h3>
          <Link to={`/products/${product.productID}`}>{product.title}</Link>
        </h3>
        <p className="product-description">{product.description}</p>
        <div className="d-flex justify-content-between align-items-end gap-2 mt-auto">
          <div>
            <span className="small text-secondary d-block">
              {product.bidCount > 0 ? 'Current bid' : 'Starting price'}
            </span>
            <strong className="price">
              {money(Number(product.currentBid) > 0 ? product.currentBid : product.startingPrice)}
            </strong>
          </div>
          <Link className="btn btn-outline-primary btn-sm" to={`/products/${product.productID}`}>
            View auction <i className="bi bi-arrow-up-right" aria-hidden="true" />
          </Link>
        </div>
        <div className="product-deadline">
          <span>
            <i className="bi bi-clock me-1" aria-hidden="true" />
            {state === 'Upcoming'
              ? 'Starts'
              : ['Ended', 'Sold'].includes(state)
                ? 'Ended'
                : 'Ends'}{' '}
            {dateTime(state === 'Upcoming' ? product.auctionStartDate : product.auctionEndDate)}
          </span>
          <span className="product-bid-count">
            <i className="bi bi-hammer" aria-hidden="true" /> {product.bidCount || 0}{' '}
            {product.bidCount === 1 ? 'bid' : 'bids'}
          </span>
        </div>
      </div>
    </article>
  );
}
export function HomePage() {
  const liveProducts = useResource('/products/search?status=Live&sort=ending&page=0&size=6');
  const upcomingProducts = useResource(
    '/products/search?status=Upcoming&sort=ending&page=0&size=6',
  );
  const categories = useResource('/categories');
  const [view, setView] = useState('All');
  const reloadLive = liveProducts.reload;
  const reloadUpcoming = upcomingProducts.reload;
  useEffect(() => {
    const id = setInterval(() => {
      reloadLive();
      reloadUpcoming();
    }, 30000);
    return () => clearInterval(id);
  }, [reloadLive, reloadUpcoming]);
  const live = liveProducts.data?.items || [];
  const upcoming = upcomingProducts.data?.items || [];
  const featured = live[0] || upcoming[0];
  const active = [...live, ...upcoming];
  const products =
    view === 'Live'
      ? liveProducts
      : view === 'Upcoming'
        ? upcomingProducts
        : {
            data: liveProducts.data && upcomingProducts.data ? active : null,
            loading: liveProducts.loading || upcomingProducts.loading,
            error: liveProducts.error || upcomingProducts.error,
            reload: () => {
              reloadLive();
              reloadUpcoming();
            },
          };
  const visible = (view === 'Live' ? live : view === 'Upcoming' ? upcoming : active).slice(0, 6);
  const total =
    view === 'Live'
      ? liveProducts.data?.totalItems || 0
      : view === 'Upcoming'
        ? upcomingProducts.data?.totalItems || 0
        : (liveProducts.data?.totalItems || 0) + (upcomingProducts.data?.totalItems || 0);
  const collections = categories.data || [];
  return (
    <>
      <section className="collector-hero" aria-label="The BidPro editorial collection">
        <div className="collector-hero-media" aria-hidden="true">
          <img src="/images/collector-editorial.png" alt="" decoding="async" />
        </div>
        <div className="collector-hero-top">
          <span className="collector-edition">
            <span className="live-dot" /> THE BIDPRO EDIT
          </span>
          <span className="collector-editorial-label">
            <i className="bi bi-stars" aria-hidden="true" /> A little out of the ordinary
          </span>
        </div>
        <div className="collector-hero-content">
          <p className="eyebrow">FOR THE EYE THAT KNOWS.</p>
          <h1 className="collector-title">
            Great finds.
            <br />
            <span>Worth keeping.</span>
          </h1>
          <p className="collector-description">
            Objects with character. Auctions with possibility. Find the piece that speaks to you,
            and make your move.
          </p>
          <div className="collector-actions">
            <Link className="btn btn-primary btn-lg" to="/auctions">
              Explore auctions <i className="bi bi-arrow-up-right" aria-hidden="true" />
            </Link>
            <Link className="collector-secondary-action" to="/register">
              Start selling <i className="bi bi-arrow-up-right" aria-hidden="true" />
            </Link>
          </div>
        </div>
        <div className="collector-hero-bottom">
          <div className="collector-metrics" aria-label="Current marketplace activity">
            <div className="collector-stat">
              <strong>
                {liveProducts.loading || liveProducts.error
                  ? '\u2014'
                  : String(liveProducts.data?.totalItems || 0).padStart(2, '0')}
              </strong>
              <span>Live auctions</span>
            </div>
            <div className="collector-stat">
              <strong>
                {categories.loading || categories.error
                  ? '\u2014'
                  : String(collections.length).padStart(2, '0')}
              </strong>
              <span>Collections to explore</span>
            </div>
            <div className="collector-assurance">
              <i className="bi bi-shield-check" aria-hidden="true" />
              <span>
                Reviewed listings.
                <br />
                Every bid recorded.
              </span>
            </div>
          </div>
          {featured ? (
            <Link className="collector-featured" to={`/products/${featured.productID}`}>
              <div className="collector-featured-art">
                <ProductArtwork product={featured} showcase />
              </div>
              <div className="collector-featured-copy">
                <span className="eyebrow">
                  <span className="live-dot" />{' '}
                  {auctionState(featured) === 'Live' ? 'ON THE FLOOR NOW' : 'COMING TO THE FLOOR'}
                </span>
                <h2>{featured.title}</h2>
                <span>
                  {featured.bidCount > 0
                    ? `${featured.bidCount} ${featured.bidCount === 1 ? 'bid' : 'bids'} placed`
                    : 'Be part of its next chapter'}
                </span>
              </div>
              <div className="collector-featured-price">
                <span>{featured.bidCount > 0 ? 'Current bid' : 'Starting at'}</span>
                <strong>{money(featured.currentBid || featured.startingPrice)}</strong>
                <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </div>
            </Link>
          ) : liveProducts.loading ||
            upcomingProducts.loading ||
            liveProducts.error ||
            upcomingProducts.error ? (
            <div className="collector-featured collector-featured-empty" role="status">
              <i className="bi bi-box-seam" aria-hidden="true" />
              <div>
                <span className="eyebrow">THE CURRENT COLLECTION</span>
                <h2>
                  {liveProducts.error || upcomingProducts.error
                    ? 'Explore the auction floor'
                    : 'Finding the next great piece…'}
                </h2>
              </div>
            </div>
          ) : (
            <Link className="collector-featured collector-featured-empty" to="/register">
              <i className="bi bi-box-seam" aria-hidden="true" />
              <div>
                <span className="eyebrow">THE NEXT COLLECTION STARTS HERE</span>
                <h2>Your objects. Their next chapter.</h2>
              </div>
              <i className="bi bi-arrow-up-right" aria-hidden="true" />
            </Link>
          )}
        </div>
      </section>
      {collections.length > 0 && (
        <section className="collection-rail" aria-label="Browse collections">
          <div className="collection-rail-label">
            <span className="eyebrow">FIND YOUR NEXT FAVOURITE</span>
            <h2>A world to explore.</h2>
            <Link className="section-link" to="/categories">
              All collections <i className="bi bi-arrow-right" aria-hidden="true" />
            </Link>
          </div>
          <div className="collection-rail-items">
            {collections.slice(0, 5).map((category) => (
              <Link
                className="collection-link"
                key={category.categoryID}
                to={`/auctions?category=${encodeURIComponent(category.categoryID)}`}
              >
                <span className="collection-link-icon" aria-hidden="true">
                  <i className={`bi bi-${collectionIcon(category.name)}`} />
                </span>
                <span className="collection-link-copy">
                  <strong>{category.name}</strong>
                  <span>Explore collection</span>
                </span>
                <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </Link>
            ))}
          </div>
        </section>
      )}
      <section className="auction-section section-space">
        <div className="auction-section-heading">
          <PageHeading eyebrow="THE CURRENT SELECTION" title="On the auction floor">
            A closer look. A considered bid. Something worth making yours.
          </PageHeading>
          <Link className="section-link" to="/auctions">
            View all auctions <i className="bi bi-arrow-up-right" aria-hidden="true" />
          </Link>
        </div>
        <div className="auction-toolbar">
          <div className="auction-tabs" aria-label="Filter featured auctions">
            {[
              ['All', 'All selections'],
              ['Live', 'Live now'],
              ['Upcoming', 'Coming soon'],
            ].map(([value, text]) => (
              <button
                key={value}
                type="button"
                className={view === value ? 'active' : ''}
                aria-pressed={view === value}
                onClick={() => setView(value)}
              >
                {value === 'Live' && <span className="live-dot" />}
                {text}
              </button>
            ))}
          </div>
          <span className="auction-result-note" aria-live="polite">
            {products.error
              ? 'Selections are temporarily unavailable.'
              : products.loading
                ? 'Finding your next discovery\u2026'
                : total > visible.length
                  ? `Showing ${visible.length} of ${total} selections`
                  : `${visible.length} ${visible.length === 1 ? 'selection' : 'selections'} on the floor`}
          </span>
        </div>
        <ResourceState
          resource={products}
          isEmpty={!visible.length}
          empty={
            view === 'Upcoming'
              ? 'No upcoming auctions yet'
              : view === 'Live'
                ? 'No live auctions right now'
                : 'No live auctions yet'
          }
        >
          <div className="product-grid">
            {visible.map((product, index) => (
              <ProductCard
                key={product.productID}
                product={product}
                categories={collections}
                index={index}
              />
            ))}
          </div>
        </ResourceState>
      </section>
      <section className="collector-principles section-space" aria-label="The BidPro experience">
        <div className="collector-principles-intro">
          <p className="eyebrow">A BETTER WAY TO BID</p>
          <h2>
            Good objects.
            <br />
            <span>Clearer possibilities.</span>
          </h2>
          <Link className="section-link" to="/help">
            How it works <i className="bi bi-arrow-up-right" aria-hidden="true" />
          </Link>
        </div>
        {[
          [
            '01',
            'shield-check',
            'Considered before they go live',
            'Accounts and listings are reviewed before the first bid.',
          ],
          [
            '02',
            'activity',
            'Every move, in the open',
            'See the current price, required increment and auction schedule.',
          ],
          [
            '03',
            'receipt',
            'The whole story, together',
            'Track your listings, bids and downloadable invoices in one workspace.',
          ],
        ].map(([number, icon, title, text]) => (
          <article key={number}>
            <div className="principle-top">
              <span className="principle-index">{number}</span>
              <i className={`bi bi-${icon}`} aria-hidden="true" />
            </div>
            <h3>{title}</h3>
            <p>{text}</p>
          </article>
        ))}
      </section>
    </>
  );
}
export function CatalogPage({ title = 'Find your next great thing' }) {
  const [params, setParams] = useSearchParams();
  const search = params.get('q') || '';
  const category = params.get('category') || '';
  const status = params.get('status') || '';
  const sort = ['ending', 'newest', 'price-low', 'price-high'].includes(params.get('sort'))
    ? params.get('sort')
    : 'ending';
  const page = /^\d+$/.test(params.get('page') || '')
    ? Math.min(Number(params.get('page')), 100000)
    : 0;
  const query = new URLSearchParams({
    q: search,
    category,
    status,
    sort,
    page: String(page),
    size: '12',
  });
  const resource = useResource(`/products/search?${query}`);
  const categories = useResource('/categories');
  useEffect(() => {
    const id = setInterval(resource.reload, 30000);
    return () => clearInterval(id);
  }, [resource.reload]);
  const changeFilter = (name, value) => {
    const next = new URLSearchParams(params);
    if (value) next.set(name, value);
    else next.delete(name);
    next.delete('page');
    setParams(next, { replace: true });
  };
  const products = resource.data?.items || [];
  const total = resource.data?.totalItems || 0;
  const totalPages = resource.data?.totalPages || 0;
  const correctingPage = Boolean(resource.data && page > Math.max(0, totalPages - 1));
  useEffect(() => {
    if (resource.loading || resource.error || !correctingPage) return;
    const next = new URLSearchParams(params);
    const lastPage = Math.max(0, totalPages - 1);
    if (lastPage) next.set('page', String(lastPage));
    else next.delete('page');
    setParams(next, { replace: true });
  }, [correctingPage, page, params, resource.error, resource.loading, setParams, totalPages]);
  const changePage = (nextPage) => {
    const next = new URLSearchParams(params);
    if (nextPage) next.set('page', String(nextPage));
    else next.delete('page');
    setParams(next);
  };
  const hasFilters = Boolean(search || category || status);
  return (
    <div className="catalog-page">
      <PageHeading
        eyebrow="THE MARKETPLACE"
        title={title}
        action={
          <button className="btn btn-outline-secondary" onClick={resource.reload}>
            <i className="bi bi-arrow-clockwise me-2" aria-hidden="true" /> Refresh
          </button>
        }
      >
        A considered collection. A clear view of every bid. Find the piece that speaks to you.
      </PageHeading>
      <div className="surface filter-bar">
        <Field label="Search listings" name="search">
          <div className="input-group">
            <span className="input-group-text bg-white">
              <i className="bi bi-search" />
            </span>
            <input
              id="search"
              className="form-control"
              type="search"
              placeholder="Search by title or description"
              maxLength={200}
              value={search}
              onChange={(event) => changeFilter('q', event.target.value)}
            />
          </div>
        </Field>
        <Field label="Category" name="category">
          <select
            id="category"
            className="form-select"
            value={category}
            onChange={(event) => changeFilter('category', event.target.value)}
          >
            <option value="">All categories</option>
            {(categories.data || []).map((item) => (
              <option key={item.categoryID} value={item.categoryID}>
                {item.name}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Auction status" name="status">
          <select
            id="status"
            className="form-select"
            value={status}
            onChange={(event) => changeFilter('status', event.target.value)}
          >
            <option value="">All auctions</option>
            {['Live', 'Upcoming', 'Ended', 'Sold'].map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
        </Field>
      </div>
      <div className="catalog-results-toolbar">
        <div className="catalog-results-summary" aria-live="polite">
          <span className="catalog-result-icon" aria-hidden="true">
            <i className="bi bi-grid" />
          </span>
          <span>
            {resource.loading || correctingPage ? (
              'Updating listings…'
            ) : (
              <>
                <strong>{total}</strong> {total === 1 ? 'listing' : 'listings'} to discover
              </>
            )}
          </span>
          {hasFilters && (
            <button
              className="catalog-clear"
              onClick={() => setParams(sort === 'ending' ? {} : { sort })}
            >
              Clear filters <i className="bi bi-x" aria-hidden="true" />
            </button>
          )}
        </div>
        <div className="catalog-sort">
          <label htmlFor="catalog-sort">Sort by</label>
          <select
            id="catalog-sort"
            className="form-select"
            value={sort}
            onChange={(event) => changeFilter('sort', event.target.value)}
          >
            <option value="ending">Ending soonest</option>
            <option value="newest">Recently listed</option>
            <option value="price-low">Price: low to high</option>
            <option value="price-high">Price: high to low</option>
          </select>
        </div>
      </div>
      <ResourceState
        resource={
          correctingPage && !resource.error ? { ...resource, data: null, loading: true } : resource
        }
        isEmpty={!products.length}
        empty="No listings match your search"
      >
        <div className="product-grid">
          {products.map((product, index) => (
            <ProductCard
              key={product.productID}
              product={product}
              categories={categories.data || []}
              index={index}
            />
          ))}
        </div>
      </ResourceState>
      {!resource.loading && !resource.error && !correctingPage && totalPages > 1 && (
        <nav className="catalog-pagination" aria-label="Catalog pages">
          <button
            className="btn btn-outline-secondary"
            disabled={page === 0}
            onClick={() => changePage(Math.max(0, page - 1))}
          >
            <i className="bi bi-arrow-left me-2" aria-hidden="true" />
            Previous page
          </button>
          <span aria-live="polite">
            Page <strong>{page + 1}</strong> of <strong>{totalPages}</strong>
          </span>
          <button
            className="btn btn-outline-secondary"
            disabled={page + 1 >= totalPages}
            onClick={() => changePage(page + 1)}
          >
            Next page
            <i className="bi bi-arrow-right ms-2" aria-hidden="true" />
          </button>
        </nav>
      )}
    </div>
  );
}
export function CategoriesPage() {
  const resource = useResource('/categories');
  return (
    <>
      <PageHeading eyebrow="BROWSE YOUR INTERESTS" title="Explore categories">
        A little curiosity can lead to a great find.
      </PageHeading>
      <ResourceState
        resource={resource}
        isEmpty={!resource.data?.length}
        empty="No categories available yet"
      >
        <div className="category-grid">
          {(resource.data || []).map((category) => (
            <Link
              className="surface category-card"
              key={category.categoryID}
              to={`/auctions?category=${encodeURIComponent(category.categoryID)}`}
            >
              <i className={`bi bi-${collectionIcon(category.name)}`} aria-hidden="true" />
              <h2>{category.name}</h2>
              <p>{category.description || 'Explore listings in this category.'}</p>
              <span>
                Explore auctions <i className="bi bi-arrow-right" />
              </span>
            </Link>
          ))}
        </div>
      </ResourceState>
    </>
  );
}
export function ProductPage() {
  const { productID } = useParams();
  const { user } = useAuth();
  const productResource = useResource(`/products/${encodeURIComponent(productID)}`);
  const publicListing = ['APPROVED', 'SOLD'].includes(productResource.data?.productStatus);
  const bidsResource = useResource(
    publicListing ? `/bids/product/${encodeURIComponent(productID)}` : null,
  );
  const categories = useResource('/categories');
  const reloadProduct = productResource.reload;
  const reloadBids = bidsResource.reload;
  const mutation = useMutation();
  const [amount, setAmount] = useState('');
  useEffect(() => {
    const id = setInterval(() => {
      reloadProduct();
      reloadBids();
    }, 15000);
    return () => clearInterval(id);
  }, [reloadProduct, reloadBids]);
  const product = productResource.data;
  const bids = (bidsResource.data || [])
    .slice()
    .sort((a, b) => Number(b.amount) - Number(a.amount));
  if (!product)
    return (
      <ResourceState resource={productResource}>
        <Empty title="Listing not found" />
      </ResourceState>
    );
  const state = auctionState(product);
  const min = minimumBid(product, bids);
  const eligible = canBid(user) && user.status === 'APPROVED' && user.userID !== product.sellerID;
  const placeBid = async (event) => {
    event.preventDefault();
    const result = await mutation.run(
      () => request('/bids', { method: 'POST', body: { productID, amount: Number(amount) } }),
      'Your bid has been placed.',
    );
    if (result.ok) {
      setAmount('');
      productResource.reload();
      bidsResource.reload();
    }
  };
  return (
    <>
      <Link to="/auctions" className="back-link">
        <i className="bi bi-arrow-left" /> Back to auctions
      </Link>
      <ResourceState resource={productResource}>
        <div className="product-detail">
          <div className="product-gallery">
            <ProductArtwork product={product} large />
            <div className="surface product-about mt-4">
              <h2>About this item</h2>
              <p className="preserve-lines mb-0">{product.description}</p>
            </div>
          </div>
          <div className="surface product-summary">
            <div className="d-flex gap-2 mb-3">
              <Badge value={state} />
              <Badge value={product.productCondition} />
            </div>
            <p className="eyebrow">
              {(categories.data || []).find((item) => item.categoryID === product.categoryID)
                ?.name || 'AUCTION ITEM'}
            </p>
            <h1>{product.title}</h1>
            <p className="small text-secondary listing-reference">Listing {product.productID}</p>
            <div className="bid-price">
              <i className="bi bi-graph-up-arrow bid-price-icon" aria-hidden="true" />
              <span>{bids.length ? 'Highest bid' : 'Starting price'}</span>
              <strong>{money(bids[0]?.amount ?? product.startingPrice)}</strong>
              <span>
                {bids.length} {bids.length === 1 ? 'bid' : 'bids'} · Increment{' '}
                {money(product.incrementGap)}
              </span>
            </div>
            <dl className="detail-list">
              <div>
                <dt>Starts</dt>
                <dd>{dateTime(product.auctionStartDate)}</dd>
              </div>
              <div>
                <dt>Ends</dt>
                <dd>{dateTime(product.auctionEndDate)}</dd>
              </div>
            </dl>
            <Feedback mutation={mutation} />
            {user && <PendingAccount user={user} />}
            {state === 'Live' && eligible ? (
              <form className="bid-form" onSubmit={placeBid}>
                <Field
                  label={`Your bid · minimum ${money(min)}`}
                  name="bidAmount"
                  type="number"
                  value={amount}
                  onChange={(event) => setAmount(event.target.value)}
                  min={min}
                  step="0.01"
                  required
                  placeholder={String(min)}
                />
                <button
                  className="btn btn-primary btn-bid w-100"
                  disabled={mutation.pending || bidsResource.loading || Boolean(bidsResource.error)}
                >
                  {mutation.pending ? 'Placing bid…' : 'Place bid'}
                </button>
                <p className="small text-secondary mt-2 mb-0">
                  Bids are binding. The highest valid bid wins when the auction ends.
                </p>
              </form>
            ) : !user && state === 'Live' ? (
              <Link
                className="btn btn-primary w-100"
                to="/login"
                state={{ from: `/products/${productID}` }}
              >
                Sign in to bid
              </Link>
            ) : (
              <Notice type="light">
                {state !== 'Live'
                  ? state === 'Upcoming'
                    ? 'Bidding opens at the scheduled start time.'
                    : 'Bidding is closed for this listing.'
                  : user?.userID === product.sellerID
                    ? 'This is your listing. You cannot bid on your own item.'
                    : 'An approved bidder or combined account is needed to bid.'}
              </Notice>
            )}
          </div>
        </div>
      </ResourceState>
      <section className="surface bid-history mt-4">
        <div className="d-flex justify-content-between align-items-center">
          <h2>Bid history</h2>
          <button className="btn btn-outline-secondary btn-sm" onClick={bidsResource.reload}>
            Refresh bids
          </button>
        </div>
        <ResourceState resource={bidsResource} isEmpty={!bids.length} empty="Be the first to bid">
          <div className="table-responsive">
            <table className="table align-middle">
              <thead>
                <tr>
                  <th scope="col">Bidder</th>
                  <th scope="col">Amount</th>
                  <th scope="col">Placed</th>
                </tr>
              </thead>
              <tbody>
                {bids.map((bid) => (
                  <tr key={bid.bidID}>
                    <td>
                      {bid.bidderID === user?.userID
                        ? 'You'
                        : `Bidder …${bid.bidderID?.slice(-6) || ''}`}
                    </td>
                    <td className="fw-semibold">{money(bid.amount)}</td>
                    <td>{dateTime(bid.timestamp)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </ResourceState>
      </section>
    </>
  );
}
