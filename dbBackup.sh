#!/bin/sh
vacuumlo -U java -h localhost -v portfoliomgr
sudo -u postgres psql -d portfoliomgr -c "VACUUM FULL pg_largeobject;"
pg_dump -U java -h localhost -d portfoliomgr -Fc --large-objects -f portfoliomgr_FULL_backup.dump
#pg_dump -U java -h localhost -d portfoliomgr -Fc  --large-objects | gzip > mydb_$(date +%F).dump.gz
# Decompress and restore
gunzip -c mydb_2026-09-18.dump.gz | pg_restore -U java -d portfoliomgr -c