#!/bin/bash
# MySQL to MongoDB Migration Script for Agrirouter Middleware
# This script migrates data from MySQL/MariaDB to MongoDB

set -e

# Configuration
MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD}"
MYSQL_DATABASE="${MYSQL_DATABASE:-agriroutermiddleware}"

MONGO_HOST="${MONGO_HOST:-localhost}"
MONGO_PORT="${MONGO_PORT:-27017}"
MONGO_USER="${MONGO_USER:-mongouser}"
MONGO_PASSWORD="${MONGO_PASSWORD}"
MONGO_DATABASE="${MONGO_DATABASE:-agriroutermiddleware}"

echo "========================================="
echo "MySQL to MongoDB Migration Script"
echo "========================================="
echo ""
echo "This script will migrate your data from MySQL to MongoDB."
echo "Please ensure both databases are accessible and you have created backups."
echo ""
echo "MySQL Configuration:"
echo "  Host: $MYSQL_HOST"
echo "  Port: $MYSQL_PORT"
echo "  Database: $MYSQL_DATABASE"
echo ""
echo "MongoDB Configuration:"
echo "  Host: $MONGO_HOST"
echo "  Port: $MONGO_PORT"
echo "  Database: $MONGO_DATABASE"
echo ""
read -p "Do you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Migration cancelled."
    exit 0
fi

echo ""
echo "Starting migration..."
echo ""

# Check if required tools are installed
command -v mysql >/dev/null 2>&1 || { echo "Error: mysql client is required but not installed. Aborting." >&2; exit 1; }
command -v mongosh >/dev/null 2>&1 || command -v mongo >/dev/null 2>&1 || { echo "Error: mongosh or mongo client is required but not installed. Aborting." >&2; exit 1; }

# Set mongo command (use mongosh if available, otherwise mongo)
if command -v mongosh >/dev/null 2>&1; then
    MONGO_CMD="mongosh"
else
    MONGO_CMD="mongo"
fi

echo "Step 1: Exporting data from MySQL..."
echo "--------------------------------------"

# Export MySQL data to JSON files
export_mysql_table() {
    local table=$1
    local output_file="/tmp/migration_${table}.json"
    
    echo "Exporting $table..."
    mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" \
        -e "SELECT * FROM $table" --batch --skip-column-names \
        | awk 'BEGIN {FS="\t"; print "["} 
               {gsub(/"/, "\\\""); 
                if (NR>1) print ","; 
                printf "{"; 
                for(i=1; i<=NF; i++) {
                    if (i>1) printf ","; 
                    printf "\"field%d\":\"%s\"", i, $i
                } 
                printf "}"
               } 
               END {print "]"}' > "$output_file"
    
    echo "  ✓ Exported to $output_file"
}

# Export all tables
tables=(
    "tenant"
    "application"
    "application_settings"
    "endpoint"
    "router_device"
    "authentication"
    "connection_criteria"
    "connection_state"
    "content_message"
    "content_message_metadata"
    "unprocessed_message"
    "supported_technical_message_type"
    "ddi_combination_to_subscribe_for"
)

for table in "${tables[@]}"; do
    if mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" \
        -e "SHOW TABLES LIKE '$table'" | grep -q "$table"; then
        export_mysql_table "$table"
    else
        echo "  ℹ Table $table not found, skipping..."
    fi
done

echo ""
echo "Step 2: Importing data to MongoDB..."
echo "--------------------------------------"
echo ""
echo "⚠ IMPORTANT: Due to the complexity of the data model changes,"
echo "this automated script provides a basic migration framework."
echo "You will need to manually adjust the MongoDB import to handle:"
echo "  - Converting JPA relationships to MongoDB references (using IDs)"
echo "  - Updating field names and structure"
echo "  - Handling embedded documents vs references"
echo ""
echo "The exported JSON files are located in /tmp/migration_*.json"
echo "Please review the data structure and create appropriate MongoDB import scripts."
echo ""
echo "Example MongoDB import command:"
echo "  $MONGO_CMD \"mongodb://$MONGO_USER:$MONGO_PASSWORD@$MONGO_HOST:$MONGO_PORT/$MONGO_DATABASE\" \\"
echo "    --eval 'db.tenant.insertMany(<your transformed data>)'"
echo ""
echo "For a complete migration, consider:"
echo "  1. Writing a custom migration script in Python or Node.js"
echo "  2. Using a data migration tool like Studio 3T or MongoDB Compass"
echo "  3. Manually transforming the data to match the new schema"
echo ""
echo "Migration preparation complete!"
echo "Review the exported files in /tmp/migration_*.json"
