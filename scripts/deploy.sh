#!/bin/bash
##############################################################################
#  PDS Fair Price Backend — EC2 Deployment Script
#  Called by GitHub Actions after SCP of the new binary.
#  Performs: backup → swap binary → restart systemd service
##############################################################################

set -euo pipefail

APP_NAME="pds-backend"
DEPLOY_DIR="/opt/pds-backend"
BINARY_PATH="${DEPLOY_DIR}/${APP_NAME}"
BACKUP_DIR="${DEPLOY_DIR}/backups"
SERVICE_NAME="pds-backend.service"
TMP_BINARY="/tmp/${APP_NAME}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "═══════════════════════════════════════════"
echo "  🚀 PDS Fair Price Backend Deployment"
echo "  📅 $(date '+%Y-%m-%d %H:%M:%S %Z')"
echo "═══════════════════════════════════════════"

# ─── Step 1: Verify new binary exists ──────────────────
if [ ! -f "$TMP_BINARY" ]; then
  echo "❌ ERROR: New binary not found at $TMP_BINARY"
  exit 1
fi

echo "✅ New binary found at $TMP_BINARY"

# ─── Step 2: Create directories if needed ──────────────
mkdir -p "$DEPLOY_DIR"
mkdir -p "$BACKUP_DIR"

# ─── Step 3: Backup current binary ────────────────────
if [ -f "$BINARY_PATH" ]; then
  echo "📦 Backing up current binary → ${BACKUP_DIR}/${APP_NAME}.${TIMESTAMP}"
  cp "$BINARY_PATH" "${BACKUP_DIR}/${APP_NAME}.${TIMESTAMP}"

  # Keep only last 5 backups
  ls -t "${BACKUP_DIR}/${APP_NAME}."* 2>/dev/null | tail -n +6 | xargs -r rm -f
  echo "🧹 Old backups cleaned (keeping last 5)"
else
  echo "ℹ️  No existing binary found — fresh deployment"
fi

# ─── Step 4: Move new binary into place ───────────────
echo "📥 Installing new binary → $BINARY_PATH"
mv "$TMP_BINARY" "$BINARY_PATH"
chmod +x "$BINARY_PATH"
chown root:root "$BINARY_PATH"

# ─── Step 5: Restart the systemd service ──────────────
echo "🔄 Restarting $SERVICE_NAME..."

# Reload in case service file was updated
systemctl daemon-reload

# Restart
systemctl restart "$SERVICE_NAME"

# Wait for startup
sleep 3

# ─── Step 6: Verify service is running ────────────────
if systemctl is-active --quiet "$SERVICE_NAME"; then
  echo "✅ $SERVICE_NAME is running!"
  systemctl status "$SERVICE_NAME" --no-pager -l | head -15
else
  echo "❌ $SERVICE_NAME failed to start!"
  echo ""
  echo "📋 Last 30 log lines:"
  journalctl -u "$SERVICE_NAME" -n 30 --no-pager
  echo ""
  echo "🔁 Rolling back to previous version..."

  # Rollback: restore the most recent backup
  LATEST_BACKUP=$(ls -t "${BACKUP_DIR}/${APP_NAME}."* 2>/dev/null | head -1)
  if [ -n "$LATEST_BACKUP" ]; then
    cp "$LATEST_BACKUP" "$BINARY_PATH"
    chmod +x "$BINARY_PATH"
    systemctl restart "$SERVICE_NAME"
    sleep 3

    if systemctl is-active --quiet "$SERVICE_NAME"; then
      echo "✅ Rollback successful — running previous version"
    else
      echo "❌ Rollback also failed — manual intervention required"
    fi
  else
    echo "⚠️  No backup available for rollback"
  fi
  exit 1
fi

echo ""
echo "═══════════════════════════════════════════"
echo "  ✅ Deployment completed successfully!"
echo "═══════════════════════════════════════════"
