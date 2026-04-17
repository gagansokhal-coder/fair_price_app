#!/bin/bash
##############################################################################
#  PDS Fair Price — EC2 One-Time Setup Script
#  Run this ONCE on your EC2 Ubuntu instance to prepare it for deployments.
#  Usage: sudo bash ec2-setup.sh
##############################################################################

set -euo pipefail

echo "═══════════════════════════════════════════"
echo "  🛠️  PDS Fair Price — EC2 Initial Setup"
echo "═══════════════════════════════════════════"

# ─── Step 1: Create service user ──────────────────────
echo ""
echo "👤 Creating 'pds' system user..."
if id "pds" &>/dev/null; then
  echo "  → User 'pds' already exists"
else
  useradd --system --no-create-home --shell /usr/sbin/nologin pds
  echo "  ✅ User 'pds' created"
fi

# ─── Step 2: Create deployment directories ────────────
echo ""
echo "📁 Creating deployment directories..."
mkdir -p /opt/pds-backend/backups
chown -R pds:pds /opt/pds-backend
echo "  ✅ /opt/pds-backend created"

# ─── Step 3: Create .env file template ────────────────
echo ""
if [ ! -f /opt/pds-backend/.env ]; then
  echo "📝 Creating .env template..."
  cat > /opt/pds-backend/.env << 'ENVEOF'
# ─── PDS Fair Price Backend Environment ───────────────
# Fill in your actual values below.

# Database
DATABASE_URL=postgres://pds_admin:YOUR_PASSWORD@YOUR_SUPABASE_HOST:5432/postgres

# Supabase
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_KEY=YOUR_SUPABASE_ANON_KEY
SUPABASE_JWT_SECRET=YOUR_JWT_SECRET

# Server
PORT=8080
GIN_MODE=release
ENVEOF

  chown pds:pds /opt/pds-backend/.env
  chmod 600 /opt/pds-backend/.env
  echo "  ✅ .env template created at /opt/pds-backend/.env"
  echo "  ⚠️  IMPORTANT: Edit this file with your actual credentials!"
  echo "     sudo nano /opt/pds-backend/.env"
else
  echo "  → .env already exists, skipping"
fi

# ─── Step 4: Install systemd service ─────────────────
echo ""
echo "⚙️  Installing systemd service..."
cp /tmp/pds-backend.service /etc/systemd/system/pds-backend.service 2>/dev/null || \
  echo "  ⚠️  Copy the pds-backend.service file to /etc/systemd/system/ manually"

systemctl daemon-reload
systemctl enable pds-backend.service
echo "  ✅ pds-backend.service enabled (will start on boot)"

# ─── Step 5: Configure firewall ──────────────────────
echo ""
echo "🔥 Configuring UFW firewall..."
if command -v ufw &>/dev/null; then
  ufw allow 22/tcp   comment 'SSH'       2>/dev/null || true
  ufw allow 8080/tcp comment 'PDS API'   2>/dev/null || true
  echo "  ✅ Port 8080 allowed"
else
  echo "  ⚠️  UFW not installed. Make sure port 8080 is open in AWS Security Group."
fi

echo ""
echo "═══════════════════════════════════════════"
echo "  ✅ EC2 Setup Complete!"
echo ""
echo "  Next steps:"
echo "  1. Edit /opt/pds-backend/.env with real credentials"
echo "  2. Copy pds-backend.service to /etc/systemd/system/"
echo "  3. Add GitHub Secrets (see CI/CD docs)"
echo "  4. Push to 'main' to trigger first deployment"
echo "═══════════════════════════════════════════"
