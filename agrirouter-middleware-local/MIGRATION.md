# MySQL to MongoDB Migration Guide

## Overview

Starting from version 12.0.0, the agrirouter-middleware uses MongoDB exclusively and no longer requires MySQL/MariaDB. This guide helps you migrate your existing data from MySQL to MongoDB.

## ⚠️ Important Notes

- **Backup First**: Always create backups of your MySQL and MongoDB databases before starting the migration.
- **Breaking Change**: This is a major version update with breaking changes in the data model.
- **Manual Steps Required**: Due to significant schema changes, some manual intervention may be required.
- **Downtime Required**: Plan for application downtime during the migration.

## Prerequisites

- MySQL/MariaDB database with existing data
- MongoDB instance (version 6.0 or later recommended)
- `mysql` command-line client
- `mongosh` or `mongo` command-line client
- Sufficient disk space for exported data

## Migration Steps

### 1. Preparation

1. **Stop the agrirouter-middleware application**
   ```bash
   docker-compose down
   ```

2. **Backup your MySQL database**
   ```bash
   mysqldump -u root -p agriroutermiddleware > backup_mysql_$(date +%Y%m%d).sql
   ```

3. **Backup your MongoDB database** (if you have existing data)
   ```bash
   mongodump --uri="mongodb://mongouser:changeit@localhost:27017/agriroutermiddleware" --out=backup_mongo_$(date +%Y%m%d)
   ```

### 2. Export Data from MySQL

Run the provided migration script:

```bash
cd agrirouter-middleware-local
export MYSQL_PASSWORD=your_mysql_password
export MONGO_PASSWORD=your_mongo_password
./migrate-mysql-to-mongodb.sh
```

This script will export your MySQL data to JSON files in `/tmp/migration_*.json`.

### 3. Data Transformation

**Important**: The data model has changed significantly. The following transformations are required:

#### Entity Relationship Changes

| Entity | Old (MySQL) | New (MongoDB) |
|--------|-------------|---------------|
| `Application` | Has `OneToMany` to `Endpoint` | Has `endpointIds` (Set<String>) |
| `Application` | Has `ManyToOne` to `Tenant` | Has `tenantId` (String) |
| `Endpoint` | Embedded in `Application` | Separate document with `applicationId` |
| `Endpoint` | Has `connectedVirtualEndpoints` | Has `connectedVirtualEndpointIds` |
| `Tenant` | Has `applications` collection | Has `applicationIds` (Set<String>) |
| `BaseEntity` | `id` is `long` | `id` is `String` |

#### Field Changes

- All `@Lob` fields are now regular `String` fields
- All `@Enumerated` fields are stored as strings
- Foreign key relationships replaced with ID references
- Auto-generated IDs changed from numeric to MongoDB ObjectId strings

### 4. Import Data to MongoDB

Due to the complexity of the schema changes, we recommend one of the following approaches:

#### Option A: Custom Migration Script (Recommended)

Create a custom migration script in your preferred language (Python, Node.js, Java) that:

1. Reads the exported JSON files
2. Transforms the data to match the new schema
3. Establishes proper ID references
4. Imports into MongoDB

Example Python outline:
```python
import pymongo
import mysql.connector
from bson import ObjectId

# Connect to databases
mysql_conn = mysql.connector.connect(...)
mongo_client = pymongo.MongoClient(...)
mongo_db = mongo_client['agriroutermiddleware']

# Migrate tenants first
# Migrate applications and create endpoint ID references
# Migrate endpoints with application ID references
# Migrate other entities
```

#### Option B: Manual Migration for Small Datasets

For small datasets, you can manually transform and import the data using MongoDB Compass or Studio 3T:

1. Open the exported JSON files
2. Transform the structure to match the new schema
3. Use the GUI to import the data

#### Option C: Start Fresh

If you don't have critical historical data:

1. Skip the migration
2. Start with a clean MongoDB database
3. Re-onboard your applications and endpoints

### 5. Update Configuration

Update your environment variables:

```bash
# Remove MySQL configuration
# MYSQL_URL=...

# Ensure MongoDB configuration is correct
MONGODB_URI=mongodb://mongouser:changeit@localhost:27017/agriroutermiddleware
```

Update `docker-compose.yml` (if not already done):
- Remove the `mysql` service
- Remove MySQL dependency from `agrirouter-middleware` service

### 6. Start the Application

```bash
docker-compose up -d
```

### 7. Verify Migration

1. Check application logs for any errors
2. Verify that endpoints are accessible
3. Test message sending and receiving
4. Verify tenant and application data

## Troubleshooting

### Common Issues

1. **Connection Errors**
   - Verify MongoDB is running: `docker-compose ps`
   - Check MongoDB logs: `docker-compose logs mongo`
   - Verify connection string in environment variables

2. **Missing Data**
   - Check MongoDB collections: `db.getCollectionNames()`
   - Verify data was imported: `db.<collection>.count()`

3. **ID Reference Issues**
   - Ensure all entity IDs were properly converted to MongoDB ObjectIds
   - Verify foreign key relationships are maintained through ID fields

### Rollback Procedure

If you need to rollback:

1. Stop the application
2. Restore MySQL backup:
   ```bash
   mysql -u root -p agriroutermiddleware < backup_mysql_YYYYMMDD.sql
   ```
3. Restore MongoDB backup:
   ```bash
   mongorestore --uri="mongodb://mongouser:changeit@localhost:27017/agriroutermiddleware" backup_mongo_YYYYMMDD/agriroutermiddleware
   ```
4. Revert to previous version of agrirouter-middleware

## Support

For issues or questions:
- Create an issue in the GitHub repository
- Check the documentation at https://github.com/agrirouter-middleware/agrirouter-middleware

## Checklist

- [ ] MySQL database backed up
- [ ] MongoDB database backed up (if applicable)
- [ ] Application stopped
- [ ] Data exported from MySQL
- [ ] Data transformed to new schema
- [ ] Data imported to MongoDB
- [ ] Configuration updated
- [ ] Application started
- [ ] Migration verified
- [ ] Old backups cleaned up (after verification)
