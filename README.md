# Distributed Name Service

A multi-threaded, Java-based client-server Distributed Name Service. This project acts as a registry that maps service names to IP addresses, supporting concurrent connections.

## Prerequisites

- Java Development Kit (JDK) 25 (as specified in `pom.xml`, or compatible)
- Apache Maven

## Compilation

Navigate to the `Distributed_Name_Service_Low_Level` directory in your terminal:

```bash
cd Distributed_Name_Service_Low_Level
```

Then, compile the `.java` files using the standard Java compiler:

```bash
javac *.java
```

## Running the Project

You need to run the Server (Registry) in one terminal window, and you can run one or more Clients in separate terminal windows. Make sure you are in the `src/main/java/org/example` directory for all terminal sessions.

### 1. Start the Server (Registry)

Open a terminal and start the server:

```bash
java NameServiceServer.java
```

By default, the server starts on port `5000`. You can optionally specify a custom port by passing it as an argument:

```bash
java NameServiceServer.java 8080
```

### 2. Start the Client

Open a new terminal window, navigate to ``, and start the client:

```bash
java NameServiceClient.java
```

The client will automatically attempt to connect to `localhost` on port `5000`.

## Client Commands

Once the client is running and connected to the server, you can type the following commands into the terminal:

- **REGISTER <serviceName> <ipAddress>**
  Registers a new service name with its associated IP address.
  _Example:_ `REGISTER web_server 192.168.1.100`

- **RESOLVE <serviceName>**
  Looks up the IP address for a given service name.
  _Example:_ `RESOLVE web_server`

- **DEREGISTER <serviceName>**
  Removes a service from the registry.
  _Example:_ `DEREGISTER web_server`

- **EXIT**
  Disconnects from the server and closes the client application.
