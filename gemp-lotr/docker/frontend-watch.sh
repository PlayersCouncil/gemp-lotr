#!/bin/sh
# Keeps the Angular app (the deck builder and the pages ported after it) served by the Java server in step with its
# sources, so a `git pull` on the server is enough to deploy a frontend change (as it is for the legacy jQuery pages).
#
# Run by the `frontend` service in docker-compose.yml. Every POLL_SECONDS it fingerprints the frontend sources and the
# legacy card data files the card images are generated from; when they change (and have stopped changing), it:
#   1. builds into a staging folder next to the live one, while the live folder keeps being served untouched;
#   2. swaps it in: live -> old, staging -> live (two renames on the same file system), then deletes old.
# A failed build leaves the live folder as it was. A changed package-lock.json reruns `npm ci` first.

set -u

FRONTEND_DIR=${FRONTEND_DIR:-/src/gemp-lotr-frontend}
NG_DIR=${NG_DIR:-/src/gemp-lotr-async/src/main/web/ng}   # served by the Java server as /gemp-lotr/ng/
POLL_SECONDS=${POLL_SECONDS:-5}

LIVE="$NG_DIR"
# Next to the live folder (the same file system, so the swap is two renames). Dot-prefixed, so the web server never
# serves them (WebRequestHandler rejects paths with "/.")
WEB_DIR=$(dirname "$NG_DIR")
STAGE="$WEB_DIR/.ng-build"
OLD="$WEB_DIR/.ng-old"

log() { echo "$(date '+%Y-%m-%d %H:%M:%S') $*"; }

# The legacy files the card image tables are generated from (see scripts/extract-card-data.mjs)
LEGACY_CARD_DATA="../gemp-lotr-async/src/main/web/js/gemp-022"
LEGACY_CARD_DATA_FILES="$LEGACY_CARD_DATA/PC_Cards.js $LEGACY_CARD_DATA/hobbit.js $LEGACY_CARD_DATA/set40.js
    $LEGACY_CARD_DATA/cards/CardImages.js $LEGACY_CARD_DATA/cards/PackImages.js"

# Hash of the file contents that affect the build (a checkout that doesn't change content doesn't rebuild).
# src/generated is left out: the build writes it.
sources_fingerprint() {
    cd "$FRONTEND_DIR" &&
        # shellcheck disable=SC2086 # the file list is split on purpose
        find src public scripts angular.json package.json package-lock.json tsconfig*.json $LEGACY_CARD_DATA_FILES \
            -path src/generated -prune -o -type f -exec md5sum {} + 2>/dev/null |
        sort | md5sum
}

lock_fingerprint() {
    md5sum "$FRONTEND_DIR/package-lock.json"
}

install_dependencies() {
    log "Installing dependencies (npm ci)"
    (cd "$FRONTEND_DIR" && npm ci --no-audit --no-fund)
}

build_and_swap() {
    rm -rf "$STAGE" "$OLD"
    log "Building the app"
    # Step 1: build next to the live folder. With --output-path, Angular writes the served files to $STAGE/browser.
    # The card data first (the same npm script as prebuild): `npx ng build` doesn't run npm's prebuild script
    if ! (cd "$FRONTEND_DIR" && npm run --silent card-data &&
        npx ng build --configuration production --output-path="$STAGE"); then
        log "Build FAILED, still serving the previous build"
        rm -rf "$STAGE"
        return 1
    fi
    mv "$STAGE/3rdpartylicenses.txt" "$STAGE/browser/" 2>/dev/null

    # Step 2: swap. The live folder is missing only between the two renames, not during a delete.
    [ -d "$LIVE" ] && mv "$LIVE" "$OLD"
    mv "$STAGE/browser" "$LIVE"
    rm -rf "$OLD" "$STAGE"
    log "Now serving the new build"
}

mkdir -p "$WEB_DIR"
# Leftovers from an interrupted run
rm -rf "$STAGE" "$OLD"

lock=""
built=""
while true; do
    current=$(sources_fingerprint)
    if [ "$current" != "$built" ]; then
        # Wait until a `git pull` has finished writing files: the fingerprint must hold for one poll
        sleep "$POLL_SECONDS"
        if [ "$(sources_fingerprint)" = "$current" ]; then
            if [ "$(lock_fingerprint)" != "$lock" ] || [ ! -d "$FRONTEND_DIR/node_modules/@angular" ]; then
                install_dependencies && lock=$(lock_fingerprint)
            fi
            build_and_swap
            # Also on failure, so a broken commit isn't rebuilt every few seconds; the next change retries
            built=$current
        fi
        continue
    fi
    sleep "$POLL_SECONDS"
done
