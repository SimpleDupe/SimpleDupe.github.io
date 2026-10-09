const releaseApi = "https://api.github.com/repos/SimpleDupe/SimpleDupe.github.io/releases/latest";
const releaseFallback = "https://github.com/SimpleDupe/SimpleDupe.github.io/releases/latest";
const numberFormat = new Intl.NumberFormat();

function setText(selector, value) {
  document.querySelectorAll(selector).forEach(element => { element.textContent = value; });
}

function formatBytes(bytes) {
  if (!Number.isFinite(bytes) || bytes <= 0) return "Size unavailable";
  const units = ["B", "KB", "MB", "GB"];
  const unitIndex = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  const amount = bytes / (1024 ** unitIndex);
  return `${amount.toFixed(unitIndex === 0 ? 0 : 1)}${units[unitIndex]}`;
}

function cleanMarkdown(value) {
  return value
    .replace(/\[([^\]]+)\]\([^)]+\)/g, "$1")
    .replace(/(`{1,3})(.*?)\1/g, "$2")
    .replace(/(\*\*|__)(.*?)\1/g, "$2")
    .replace(/[*_~]/g, "")
    .trim();
}

function renderReleaseNotes(markdown) {
  const container = document.getElementById("release-notes-content");
  if (!container) return;

  container.replaceChildren();
  let paragraph = [];
  let list = null;

  function flushParagraph() {
    if (!paragraph.length) return;
    const node = document.createElement("p");
    node.textContent = cleanMarkdown(paragraph.join(" "));
    container.append(node);
    paragraph = [];
  }

  for (const rawLine of markdown.split(/\r?\n/)) {
    const line = rawLine.trim();
    const heading = line.match(/^#{1,3}\s+(.+)$/);
    const bullet = line.match(/^[-*]\s+(.+)$/);

    if (!line || /^-{3,}$/.test(line)) {
      flushParagraph();
      list = null;
      continue;
    }
    if (heading) {
      flushParagraph();
      list = null;
      const node = document.createElement("h3");
      node.textContent = cleanMarkdown(heading[1]);
      container.append(node);
      continue;
    }
    if (bullet) {
      flushParagraph();
      if (!list) {
        list = document.createElement("ul");
        container.append(list);
      }
      const item = document.createElement("li");
      item.textContent = cleanMarkdown(bullet[1]);
      list.append(item);
      continue;
    }
    list = null;
    paragraph.push(line);
  }
  flushParagraph();

  if (!container.childElementCount) {
    const empty = document.createElement("p");
    empty.className = "release-notes-empty";
    empty.textContent = "No release notes have been published yet.";
    container.append(empty);
  }
}

async function loadLatestRelease() {
  try {
    const response = await fetch(releaseApi, {
      headers: { Accept: "application/vnd.github+json" },
      cache: "no-store"
    });
    if (!response.ok) throw new Error(`GitHub returned ${response.status}`);

    const release = await response.json();
    const assets = Array.isArray(release.assets) ? release.assets : [];
    const jar = assets.find(asset => (asset.name || "").toLowerCase().endsWith(".jar"));
    const version = release.tag_name || "Latest release";
    const releaseUrl = release.html_url || releaseFallback;
    const downloadCount = jar ? Number(jar.download_count) || 0 : 0;
    const published = release.published_at
      ? new Intl.DateTimeFormat(undefined, { dateStyle: "medium" }).format(new Date(release.published_at))
      : "Not provided";

    setText("[data-release-version]", version);
    setText("[data-release-downloads]", numberFormat.format(downloadCount));
    setText("[data-release-date]", published);
    setText("[data-jar-size]", jar ? formatBytes(jar.size) : "JAR not published");

    document.querySelectorAll("[data-download-jar]").forEach(link => {
      link.href = jar ? jar.browser_download_url : releaseUrl;
      link.textContent = jar ? `Download ${version} JAR` : "View release on GitHub";
    });
    document.querySelectorAll("[data-release-link]").forEach(link => { link.href = releaseUrl; });
    document.querySelectorAll("[data-release-status]").forEach(status => {
      status.textContent = jar ? "Latest JAR is ready to download." : "No JAR asset found; view the release on GitHub.";
    });
    renderReleaseNotes(release.body || "");
  } catch (error) {
    setText("[data-release-version]", "Latest release");
    setText("[data-release-downloads]", "Unavailable");
    setText("[data-release-date]", "Unavailable");
    setText("[data-jar-size]", "Unavailable");

    document.querySelectorAll("[data-download-jar]").forEach(link => {
      link.href = releaseFallback;
      link.textContent = "View downloads on GitHub";
    });
    document.querySelectorAll("[data-release-link]").forEach(link => { link.href = releaseFallback; });
    document.querySelectorAll("[data-release-status]").forEach(status => {
      status.dataset.state = "error";
      status.textContent = "Release details could not load. The GitHub release page is still available.";
    });
    renderReleaseNotes("");
  }
}

function initScrollReveals() {
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (reduceMotion || !("IntersectionObserver" in window)) return;

  const revealElements = document.querySelectorAll(
    ".page-intro, .content-section, .control-story, .control-list .control-row, .release-bar, .metrics, .feature-grid .feature, .hub-grid .hub-link, .download-layout > *, .data-table tbody tr"
  );
  if (!revealElements.length) return;

  document.documentElement.classList.add("motion-ready");
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add("is-visible");
      observer.unobserve(entry.target);
    });
  }, { threshold: .12, rootMargin: "0px 0px -32px 0px" });

  revealElements.forEach(element => {
    element.classList.add("scroll-reveal");
    observer.observe(element);
  });
}

const legacyPage = {
  "#features": "features.html",
  "#download": "download.html",
  "#setup": "setup.html",
  "#release-notes": "download.html"
}[window.location.hash];

if (legacyPage && (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("/index.html"))) {
  window.location.replace(legacyPage);
} else {
  initScrollReveals();
  loadLatestRelease();
}
