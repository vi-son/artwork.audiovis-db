# audiovis i/o API backend
### Storing artwork creations in a MongoDB via Clojure

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.
[leiningen]: https://github.com/technomancy/leiningen

---

## Running

To start a web server for the application, run:
`lein ring server`

---

### Working with the database
- *Access the MongoDB database*
`mongo --username $USER --password $PWD`

- *Describe the database*
```
show dbs
use audiovisio
show collections
```

- *Delete all items*
`db.collectionName.remove({})`

- *List all items*
`db.collectionName.find()`

- *List a specific item*
`db.collectionName.find({session: "oensirdaeinso"})`
