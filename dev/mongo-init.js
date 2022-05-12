db.createUser({
  user: 'mongouser',
  pwd: 'changeit',
  roles: [
    {
      role: 'dbOwner',
      db: 'agriroutermiddleware',
    },
  ],
});