# ChatServer

A simple line-based TCP chat server. Requires **Java 25** or newer.

## Run

A ready-to-use jar is in `dist/`:

```
java -jar dist/ChatServer.jar [port]
```

The default port is `33456`. Type `q` in the server console to shut it down.

## Build

```
mvn package
```

This produces `target/ChatServer.jar`.

## Protocol

Each message is one line of text. Client → server:

| Command            | Meaning                                    |
|--------------------|--------------------------------------------|
| `n <name>`         | Register with a name (server answers `s`)  |
| `m <user> <text>`  | Send `<text>` to `<user>`                  |
| `t`                | List online users                          |
| `x`                | Leave the chat                             |

Server → client:

| Message               | Meaning                          |
|-----------------------|----------------------------------|
| `s`                   | Registration successful          |
| `m <from> <text>`     | Incoming message                 |
| `t <user> <user> ...` | List of online users             |
| `e <error>`           | Error                            |
| `x <reason>`          | Server closed the connection     |
