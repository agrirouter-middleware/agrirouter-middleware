print('##############################################################################################################')
print('# Creating user for database');
print('##############################################################################################################')

db = db.getSiblingDB(process.env["MONGO_DATABASE"]);

db.createUser({
    user: process.env["MONGO_USER"],
    pwd: process.env["MONGO_PASSWORD"],
    roles: [
        {
            role: "dbOwner",
            db: process.env["MONGO_DATABASE"]
        }
    ]
});

print('##############################################################################################################')
print('# User for database has been created.')
print('##############################################################################################################')
